import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

// Angular Material Imports
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { UserManagementService } from '../../../services/user-management.service';
import {
  User,
  UserManagement,
  UserRole,
  UserStatus,
  Etudiant,
  Tuteur,
  Admin
} from '../../../core/models/user-management.model';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatListModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule
  ],
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Component state
  user: User | null = null;
  userManagement: UserManagement | null = null;
  loading = true;
  error = false;
  userId: number;

  // Enums for template
  UserRole = UserRole;
  UserStatus = UserStatus;

  constructor(
    private userManagementService: UserManagementService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.userId = +this.route.snapshot.params['id'];
  }

  ngOnInit(): void {
    this.loadUserData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private async loadUserData(): Promise<void> {
    this.loading = true;
    this.error = false;

    try {
      // Load user details
      const userData = await this.userManagementService.getUserById(this.userId).toPromise();
      this.user = userData || null;
      
      // Find user in management list for additional info
      const users = this.userManagementService.getCurrentUsers();
      this.userManagement = users.find(u => u.id === this.userId) || null;

      if (!this.user) {
        this.error = true;
        this.snackBar.open('User not found', 'Close', { duration: 3000 });
      }
    } catch (error) {
      console.error('Error loading user data:', error);
      this.error = true;
      this.snackBar.open('Error loading user data', 'Close', { duration: 3000 });
    } finally {
      this.loading = false;
    }
  }

  // Navigation methods
  goBack(): void {
    this.router.navigate(['/user-management']);
  }

  editUser(): void {
    this.router.navigate(['/user-management/edit', this.userId]);
  }

  // Action methods
  async toggleUserStatus(): Promise<void> {
    if (!this.userManagement) return;

    const action = this.userManagement.enabled ? 'disable' : 'enable';
    const confirmed = confirm(`Are you sure you want to ${action} ${this.userManagement.fullName}?`);
    
    if (!confirmed) return;

    try {
      await this.userManagementService.updateUserStatus(this.userId, !this.userManagement.enabled).toPromise();
      this.snackBar.open(`User ${action}d successfully`, 'Close', { duration: 3000 });
      await this.loadUserData(); // Reload data
    } catch (error) {
      console.error(`Error ${action}ing user:`, error);
      this.snackBar.open(`Error ${action}ing user`, 'Close', { duration: 3000 });
    }
  }

  sendEmail(): void {
    if (this.user?.email) {
      window.open(`mailto:${this.user.email}`, '_blank');
    }
  }

  callUser(): void {
    if (this.user?.telephone) {
      window.open(`tel:${this.user.telephone}`, '_blank');
    }
  }

  // Utility methods
  getUserRole(): UserRole {
    return this.userManagement?.role || UserRole.STUDENT;
  }

  getUserStatus(): UserStatus {
    return this.user?.statut || UserStatus.ACTIVE;
  }

  getStatusColor(): string {
    switch (this.getUserStatus()) {
      case UserStatus.ACTIVE: return 'text-green-600';
      case UserStatus.INACTIVE: return 'text-gray-600';
      case UserStatus.SUSPENDED: return 'text-red-600';
      case UserStatus.PENDING: return 'text-yellow-600';
      default: return 'text-gray-600';
    }
  }

  getStatusIcon(): string {
    switch (this.getUserStatus()) {
      case UserStatus.ACTIVE: return 'check_circle';
      case UserStatus.INACTIVE: return 'cancel';
      case UserStatus.SUSPENDED: return 'block';
      case UserStatus.PENDING: return 'schedule';
      default: return 'help';
    }
  }

  getRoleColor(): string {
    switch (this.getUserRole()) {
      case UserRole.STUDENT: return 'bg-blue-100 text-blue-800';
      case UserRole.TUTOR: return 'bg-green-100 text-green-800';
      case UserRole.ADMIN: return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getRoleIcon(): string {
    switch (this.getUserRole()) {
      case UserRole.STUDENT: return 'school';
      case UserRole.TUTOR: return 'person';
      case UserRole.ADMIN: return 'admin_panel_settings';
      default: return 'person';
    }
  }

  getUserInitials(): string {
    if (!this.user) return '?';
    const firstName = this.user.prenom || '';
    const lastName = this.user.nom || '';
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  getFullName(): string {
    if (!this.user) return 'Unknown User';
    return `${this.user.prenom} ${this.user.nom}`.trim();
  }

  formatDate(date: Date | string | undefined): string {
    if (!date) return 'N/A';
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return dateObj.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getStudentData(): Partial<Etudiant> | null {
    if (this.getUserRole() === UserRole.STUDENT && this.user) {
      return this.user as Etudiant;
    }
    return null;
  }

  getTutorData(): Partial<Tuteur> | null {
    if (this.getUserRole() === UserRole.TUTOR && this.user) {
      return this.user as Tuteur;
    }
    return null;
  }

  getAdminData(): Partial<Admin> | null {
    if (this.getUserRole() === UserRole.ADMIN && this.user) {
      return this.user as Admin;
    }
    return null;
  }

  refreshData(): void {
    this.loadUserData();
  }
}