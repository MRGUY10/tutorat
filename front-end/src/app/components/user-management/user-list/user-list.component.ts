import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, FormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, debounceTime, takeUntil, distinctUntilChanged } from 'rxjs';

// Angular Material Modules
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { UserManagementService } from '../../../services/user-management.service';
import {
  UserManagement,
  UserRole,
  UserStatus,
  UserSearchParams,
  UserStatistics,
  UpdateUserRequest
} from '../../../core/models/user-management.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
    MatCardModule,
    MatMenuModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // Data source
  dataSource = new MatTableDataSource<UserManagement>();
  users: UserManagement[] = [];
  filteredUsers: UserManagement[] = [];
  
  // Loading and error states
  isLoading = false;
  error: string | null = null;
  
  // Pagination
  totalUsers = 0;
  pageSize = 10;
  currentPage = 0;
  
  // Statistics
  statistics: UserStatistics = {
    totalUsers: 0,
    activeUsers: 0,
    blockedUsers: 0,
    totalStudents: 0,
    totalTutors: 0,
    activeUsersPercentage: 0,
    blockedUsersPercentage: 0
  };

  // Block/Unblock Modal States
  showBlockModal = false;
  showUnblockModal = false;
  selectedUser: UserManagement | null = null;
  blockReason = '';
  unblockReason = '';
  blockingUser = false;
  unblockingUser = false;

  // Edit Modal States
  showEditModal = false;
  editingUser = false;
  editForm: FormGroup;

  // Search and filters
  searchForm: FormGroup;
  filterForm: FormGroup;
  roleFilter = new FormControl();
  statusFilter = new FormControl();
  
  // Autocomplete suggestions
  searchSuggestions: string[] = [];
  showSuggestions = false;
  filteredSuggestions: string[] = [];
  selectedSuggestionIndex = -1;

  // User detail modal
  showUserDetailModal = false;
  selectedUserForDetail: UserManagement | null = null;
  
  // Table columns
  displayedColumns: string[] = [
    'fullName',
    'email',
    'role',
    'status',
    'dateInscription',
    'lastLogin',
    'actions'
  ];

  // Enums for template
  UserRole = UserRole;
  UserStatus = UserStatus;

  // Options for filters
  roleOptions = [
    { value: '', label: 'All Roles' },
    { value: UserRole.STUDENT, label: 'Students' },
    { value: UserRole.TUTOR, label: 'Tutors' },
    { value: UserRole.ADMIN, label: 'Administrators' }
  ];

  statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: UserStatus.ACTIVE, label: 'Active' },
    { value: UserStatus.INACTIVE, label: 'Inactive' },
    { value: UserStatus.PENDING, label: 'Pending' }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private userManagementService: UserManagementService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.searchForm = new FormGroup({
      query: new FormControl(''),
      role: new FormControl(''),
      status: new FormControl('')
    });
    
    this.filterForm = new FormGroup({
      searchQuery: new FormControl(''),
      roleFilter: new FormControl(''),
      statusFilter: new FormControl(''),
      sortBy: new FormControl('fullName'),
      dateFrom: new FormControl(''),
      dateTo: new FormControl(''),
      lastLoginDays: new FormControl('')
    });

    this.editForm = new FormGroup({
      prenom: new FormControl('', [Validators.required, Validators.minLength(2)]),
      nom: new FormControl('', [Validators.required, Validators.minLength(2)]),
      email: new FormControl('', [Validators.required, Validators.email]),
      telephone: new FormControl(''),
      statut: new FormControl('ACTIVE')
    });
  }

  ngOnInit(): void {
    this.initializeComponent();
    this.setupSearchSubscriptions();
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeComponent(): void {
    // Subscribe to service observables
    this.userManagementService.users$
      .pipe(takeUntil(this.destroy$))
      .subscribe((users: UserManagement[]) => {
        this.users = users;
        this.applyFilters();
      });

    this.userManagementService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe((loading: boolean) => {
        this.isLoading = loading;
      });

    this.userManagementService.statistics$
      .pipe(takeUntil(this.destroy$))
      .subscribe((statistics: UserStatistics | null) => {
        if (statistics) {
          this.statistics = statistics;
        }
      });
  }

  private setupSearchSubscriptions(): void {
    // Real-time search with autocomplete
    this.filterForm.get('searchQuery')?.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((searchValue: string) => {
        this.updateSearchSuggestions(searchValue);
        this.applyFilters();
      });

    // Filter changes
    this.filterForm.get('roleFilter')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });

    this.filterForm.get('statusFilter')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });

    // Advanced filter changes
    this.filterForm.get('dateFrom')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });

    this.filterForm.get('dateTo')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });

    this.filterForm.get('lastLoginDays')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });
  }

  private updateSearchSuggestions(searchValue: string): void {
    if (!searchValue || searchValue.length < 2) {
      this.showSuggestions = false;
      this.filteredSuggestions = [];
      return;
    }

    const query = searchValue.toLowerCase();
    const suggestions = new Set<string>();

    // Add name and email suggestions
    this.users.forEach(user => {
      if (user.fullName?.toLowerCase().includes(query)) {
        suggestions.add(user.fullName);
      }
      if (user.email?.toLowerCase().includes(query)) {
        suggestions.add(user.email);
      }
      // Add role suggestions
      if (user.role?.toLowerCase().includes(query)) {
        suggestions.add(user.role);
      }
    });

    this.filteredSuggestions = Array.from(suggestions).slice(0, 5);
    this.showSuggestions = this.filteredSuggestions.length > 0;
    this.selectedSuggestionIndex = -1;
  }

  selectSuggestion(suggestion: string): void {
    this.searchForm.patchValue({ searchQuery: suggestion });
    this.showSuggestions = false;
    this.applyFilters();
  }

  onSearchKeydown(event: KeyboardEvent): void {
    if (!this.showSuggestions) return;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.selectedSuggestionIndex = Math.min(
          this.selectedSuggestionIndex + 1,
          this.filteredSuggestions.length - 1
        );
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.selectedSuggestionIndex = Math.max(this.selectedSuggestionIndex - 1, -1);
        break;
      case 'Enter':
        event.preventDefault();
        if (this.selectedSuggestionIndex >= 0) {
          this.selectSuggestion(this.filteredSuggestions[this.selectedSuggestionIndex]);
        }
        break;
      case 'Escape':
        this.showSuggestions = false;
        this.selectedSuggestionIndex = -1;
        break;
    }
  }

  hideSuggestions(): void {
    setTimeout(() => {
      this.showSuggestions = false;
      this.selectedSuggestionIndex = -1;
    }, 200); // Delay to allow click events on suggestions
  }

  loadUsers(): void {
    this.userManagementService.loadUsers();
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.userManagementService.getUserStatistics().subscribe();
  }

  applyFilters(): void {
    let filtered = [...this.users];
    
    // Apply search filter
    const query = this.filterForm.get('searchQuery')?.value?.toLowerCase() || '';
    if (query) {
      filtered = filtered.filter(user =>
        user.fullName?.toLowerCase().includes(query) ||
        user.email?.toLowerCase().includes(query) ||
        user.phone?.toLowerCase().includes(query)
      );
    }

    // Apply role filter
    const role = this.filterForm.get('roleFilter')?.value;
    if (role) {
      filtered = filtered.filter(user => user.role === role);
    }

    // Apply status filter
    const status = this.filterForm.get('statusFilter')?.value;
    if (status) {
      filtered = filtered.filter(user => user.status === status);
    }

    // Apply date range filter (registration date)
    const dateFrom = this.filterForm.get('dateFrom')?.value;
    const dateTo = this.filterForm.get('dateTo')?.value;
    
    if (dateFrom) {
      const fromDate = new Date(dateFrom);
      filtered = filtered.filter(user => {
        const regDate = new Date(user.dateInscription);
        return regDate >= fromDate;
      });
    }
    
    if (dateTo) {
      const toDate = new Date(dateTo);
      toDate.setHours(23, 59, 59, 999); // Include the entire day
      filtered = filtered.filter(user => {
        const regDate = new Date(user.dateInscription);
        return regDate <= toDate;
      });
    }

    // Apply last login filter
    const lastLoginDays = this.filterForm.get('lastLoginDays')?.value;
    if (lastLoginDays) {
      const daysAgo = new Date();
      daysAgo.setDate(daysAgo.getDate() - parseInt(lastLoginDays));
      
      filtered = filtered.filter(user => {
        if (!user.lastLogin) return false;
        const lastLogin = new Date(user.lastLogin);
        return lastLogin >= daysAgo;
      });
    }

    this.filteredUsers = filtered;
    this.dataSource.data = filtered;
    this.totalUsers = filtered.length;

    if (this.paginator) {
      this.paginator.length = this.totalUsers;
    }
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.applyFilters();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.showSuggestions = false;
    this.applyFilters();
  }

  createUser(): void {
    this.router.navigate(['/users/create']);
  }

  editUser(user: UserManagement): void {
    this.closeUserDetailModal();
    this.selectedUser = user;
    this.editForm.patchValue({
      prenom: user.fullName.split(' ')[0] || '',
      nom: user.fullName.split(' ').slice(1).join(' ') || '',
      email: user.email,
      telephone: user.phone || '',
      statut: user.status
    });
    this.showEditModal = true;
  }

  viewUser(user: UserManagement): void {
    this.selectedUserForDetail = user;
    this.showUserDetailModal = true;
  }

  closeUserDetailModal(): void {
    this.showUserDetailModal = false;
    this.selectedUserForDetail = null;
  }

  deleteUser(user: UserManagement): void {
    // Implementation for delete confirmation dialog
    console.log('Delete user:', user);
  }

  toggleUserStatus(user: UserManagement): void {
    const newEnabled = !user.enabled;
    this.userManagementService.updateUserStatus(user.id!, newEnabled)
      .subscribe({
        next: () => {
          user.enabled = newEnabled;
          user.status = newEnabled ? UserStatus.ACTIVE : UserStatus.INACTIVE;
          this.snackBar.open(`User ${newEnabled ? 'enabled' : 'disabled'} successfully`, 'Close', {
            duration: 3000
          });
          this.loadUsers();
        },
        error: (error: any) => {
          this.snackBar.open('Error updating user status', 'Close', {
            duration: 3000
          });
        }
      });
  }

  exportUsers(): void {
    // Implementation for CSV export
    console.log('Export users');
  }

  refreshData(): void {
    this.loadUsers();
  }

  getStatusColor(status: UserStatus): string {
    switch (status) {
      case UserStatus.ACTIVE:
        return 'text-green-600';
      case UserStatus.INACTIVE:
        return 'text-red-600';
      case UserStatus.PENDING:
        return 'text-orange-600';
      case UserStatus.SUSPENDED:
        return 'text-red-800';
      default:
        return 'text-gray-600';
    }
  }

  getStatusIcon(status: UserStatus): string {
    switch (status) {
      case UserStatus.ACTIVE:
        return 'check_circle';
      case UserStatus.INACTIVE:
        return 'cancel';
      case UserStatus.PENDING:
        return 'schedule';
      case UserStatus.SUSPENDED:
        return 'block';
      default:
        return 'help';
    }
  }

  getRoleColor(role: UserRole): string {
    switch (role) {
      case UserRole.STUDENT:
        return 'bg-blue-100 text-blue-800';
      case UserRole.TUTOR:
        return 'bg-green-100 text-green-800';
      case UserRole.ADMIN:
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRoleIcon(role: UserRole): string {
    switch (role) {
      case UserRole.STUDENT:
        return 'school';
      case UserRole.TUTOR:
        return 'person';
      case UserRole.ADMIN:
        return 'admin_panel_settings';
      default:
        return 'person';
    }
  }

  getUserAvatar(user: UserManagement): string {
    if (user.fullName) {
      const names = user.fullName.split(' ');
      if (names.length >= 2) {
        return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
      }
      return user.fullName.charAt(0).toUpperCase();
    }
    return user.email?.charAt(0).toUpperCase() || '?';
  }

  // Block user modal methods
  openBlockModal(user: UserManagement): void {
    this.selectedUser = user;
    this.blockReason = '';
    this.showBlockModal = true;
  }

  cancelBlock(): void {
    this.showBlockModal = false;
    this.selectedUser = null;
    this.blockReason = '';
  }

  confirmBlockUser(): void {
    if (!this.selectedUser) return;

    this.blockingUser = true;
    
    this.userManagementService.blockUser(this.selectedUser.id, this.blockReason)
      .subscribe({
        next: (response) => {
          this.snackBar.open(
            `User ${this.selectedUser?.fullName} has been blocked successfully`, 
            'Close', 
            {
              duration: 3000,
              panelClass: ['success-snackbar']
            }
          );
          this.showBlockModal = false;
          this.selectedUser = null;
          this.blockReason = '';
          this.blockingUser = false;
          this.loadUsers(); // Refresh the user list
        },
        error: (error) => {
          console.error('Error blocking user:', error);
          this.snackBar.open(
            'Failed to block user. Please try again.', 
            'Close', 
            {
              duration: 3000,
              panelClass: ['error-snackbar']
            }
          );
          this.blockingUser = false;
        }
      });
  }

  // Unblock user modal methods
  openUnblockModal(user: UserManagement): void {
    this.selectedUser = user;
    this.unblockReason = '';
    this.showUnblockModal = true;
  }

  cancelUnblock(): void {
    this.showUnblockModal = false;
    this.selectedUser = null;
    this.unblockReason = '';
  }

  confirmUnblockUser(): void {
    if (!this.selectedUser) return;

    this.unblockingUser = true;
    
    this.userManagementService.unblockUser(this.selectedUser.id, this.unblockReason)
      .subscribe({
        next: (response) => {
          this.snackBar.open(
            `User ${this.selectedUser?.fullName} has been unblocked successfully`, 
            'Close', 
            {
              duration: 3000,
              panelClass: ['success-snackbar']
            }
          );
          this.showUnblockModal = false;
          this.selectedUser = null;
          this.unblockReason = '';
          this.unblockingUser = false;
          this.loadUsers(); // Refresh the user list
        },
        error: (error) => {
          console.error('Error unblocking user:', error);
          this.snackBar.open(
            'Failed to unblock user. Please try again.', 
            'Close', 
            {
              duration: 3000,
              panelClass: ['error-snackbar']
            }
          );
          this.unblockingUser = false;
        }
      });
  }

  // Helper methods
  canBlockUser(user: UserManagement): boolean {
    return user.status !== UserStatus.SUSPENDED && user.role !== UserRole.ADMIN;
  }

  canUnblockUser(user: UserManagement): boolean {
    return user.status === UserStatus.SUSPENDED;
  }

  getUserInitials(firstName: string, lastName: string): string {
    return ((firstName?.charAt(0) || '') + (lastName?.charAt(0) || '')).toUpperCase();
  }

  getRoleBadgeClass(role: string): string {
    switch (role?.toLowerCase()) {
      case 'student':
        return 'bg-blue-100 text-blue-800';
      case 'tutor':
        return 'bg-green-100 text-green-800';
      case 'admin':
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRoleDisplayName(role: string): string {
    switch (role?.toLowerCase()) {
      case 'student':
        return 'Student';
      case 'tutor':
        return 'Tutor';
      case 'admin':
        return 'Administrator';
      default:
        return role || 'Unknown';
    }
  }

  getStatusBadgeClass(status: any): string {
    switch (status) {
      case UserStatus.ACTIVE:
        return 'bg-green-100 text-green-800';
      case UserStatus.INACTIVE:
        return 'bg-gray-100 text-gray-800';
      case UserStatus.SUSPENDED:
        return 'bg-red-100 text-red-800';
      case UserStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  // Edit User Methods
  openEditModal(user: UserManagement): void {
    this.selectedUser = user;
    this.editForm.patchValue({
      prenom: user.fullName.split(' ')[0] || '',
      nom: user.fullName.split(' ').slice(1).join(' ') || '',
      email: user.email,
      telephone: user.phone || '',
      statut: user.status
    });
    this.showEditModal = true;
  }

  cancelEdit(): void {
    this.showEditModal = false;
    this.selectedUser = null;
    this.editForm.reset();
  }

  confirmEditUser(): void {
    if (!this.selectedUser || this.editForm.invalid) return;

    this.editingUser = true;
    
    const updateRequest: UpdateUserRequest = {
      prenom: this.editForm.get('prenom')?.value,
      nom: this.editForm.get('nom')?.value,
      email: this.editForm.get('email')?.value,
      telephone: this.editForm.get('telephone')?.value,
      statut: this.editForm.get('statut')?.value
    };

    this.userManagementService.updateUser(this.selectedUser.id, updateRequest)
      .subscribe({
        next: (response) => {
          this.snackBar.open(
            `User ${this.selectedUser?.fullName} has been updated successfully`, 
            'Close', 
            {
              duration: 3000,
              panelClass: ['success-snackbar']
            }
          );
          this.showEditModal = false;
          this.selectedUser = null;
          this.editForm.reset();
          this.editingUser = false;
          this.loadUsers(); // Refresh the user list
          this.loadStatistics(); // Refresh statistics
        },
        error: (error) => {
          console.error('Error updating user:', error);
          this.snackBar.open(
            'Failed to update user. Please try again.', 
            'Close', 
            {
              duration: 3000,
              panelClass: ['error-snackbar']
            }
          );
          this.editingUser = false;
        }
      });
  }
}