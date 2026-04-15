import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  User,
  Etudiant,
  Tuteur,
  Admin,
  CreateStudentRequest,
  CreateTutorRequest,
  CreateAdminRequest,
  UserManagement,
  UserSearchParams,
  UserStatistics,
  UserStatus,
  UserRole,
  UserStatusChangeRequest,
  UserWithRolesDTO,
  UpdateUserRequest
} from '../models/user-management.model';

@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private readonly baseUrl = `${environment.BASE_URL}/api/v1/users`;
  private readonly tutorProfileUrl = `${environment.BASE_URL}/api/tutors/profile`;

  // State management
  private usersSubject = new BehaviorSubject<UserManagement[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private statisticsSubject = new BehaviorSubject<UserStatistics | null>(null);

  public users$ = this.usersSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public statistics$ = this.statisticsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUsers();
  }

  // HTTP Headers with authentication
  private getHeaders(): HttpHeaders {
    const token = this.getAuthToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    });
  }

  private getAuthToken(): string | null {
    // Return mock token for testing without authentication
    return 'mock-token-for-testing';
    
    // Original implementation commented out:
    // const authTokens = localStorage.getItem('auth_tokens');
    // if (authTokens) {
    //   try {
    //     const tokens = JSON.parse(authTokens);
    //     return tokens.access_token;
    //   } catch (error) {
    //     console.error('Error parsing auth tokens:', error);
    //     return null;
    //   }
    // }
    // return null;
  }

  // Load all users
  loadUsers(): void {
    this.loadingSubject.next(true);
    this.getAllUsers().subscribe({
      next: (users) => {
        this.usersSubject.next(users);
        this.loadingSubject.next(false);
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.loadingSubject.next(false);
      }
    });
  }

  // Get all users
  getAllUsers(): Observable<UserManagement[]> {
    return this.http.get<UserWithRolesDTO[]>(this.baseUrl, { headers: this.getHeaders() })
      .pipe(
        map(users => this.transformUsersWithRolesToManagement(users)),
        catchError(() => {
          console.log('API call failed, returning mock data for testing');
          return of(this.getMockUsers());
        })
      );
  }

  // Mock data for testing without backend
  private getMockUsers(): UserManagement[] {
    return [
      {
        id: 1,
        fullName: 'John Doe',
        email: 'john.doe@example.com',
        role: UserRole.ADMIN,
        status: UserStatus.ACTIVE,
        dateInscription: new Date('2023-01-15'),
        lastLogin: new Date('2024-09-18'),
        isActive: true,
        enabled: true,
        phone: '+1234567890'
      },
      {
        id: 2,
        fullName: 'Jane Smith',
        email: 'jane.smith@example.com',
        role: UserRole.TUTOR,
        status: UserStatus.ACTIVE,
        dateInscription: new Date('2023-02-20'),
        lastLogin: new Date('2024-09-17'),
        isActive: true,
        enabled: true,
        phone: '+1234567891'
      },
      {
        id: 3,
        fullName: 'Bob Johnson',
        email: 'bob.johnson@example.com',
        role: UserRole.STUDENT,
        status: UserStatus.INACTIVE,
        dateInscription: new Date('2023-03-10'),
        lastLogin: new Date('2024-09-10'),
        isActive: false,
        enabled: false,
        phone: '+1234567892'
      }
    ];
  }

  // Get user by ID
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`, { headers: this.getHeaders() })
      .pipe(
        catchError(this.handleError<User>('getUserById'))
      );
  }

  // Get user by email
  getUserByEmail(email: string): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/email/${email}`, { headers: this.getHeaders() })
      .pipe(
        catchError(this.handleError<User>('getUserByEmail'))
      );
  }

  // Search users
  searchUsers(params: UserSearchParams): Observable<UserManagement[]> {
    // Use getAllUsers() to get all users with proper roles, then filter client-side
    return this.getAllUsers().pipe(
      map(users => this.filterUsersByParams(users, params)),
      catchError(this.handleError<UserManagement[]>('searchUsers', []))
    );
  }

  // Get users by status
  getUsersByStatus(status: UserStatus): Observable<UserWithRolesDTO[]> {
    return this.http.get<UserWithRolesDTO[]>(
      `${this.baseUrl}/status/${status}`, 
      { headers: this.getHeaders() }
    ).pipe(
      catchError(error => {
        console.error('Error fetching users by status:', error);
        return of([]);
      })
    );
  }

  // Create student
  createStudent(request: CreateStudentRequest): Observable<Etudiant> {
    return this.http.post<Etudiant>(`${this.baseUrl}/students`, request, { headers: this.getHeaders() })
      .pipe(
        tap(() => this.loadUsers()),
        catchError(this.handleError<Etudiant>('createStudent'))
      );
  }

  // Create tutor
  createTutor(request: CreateTutorRequest): Observable<Tuteur> {
    return this.http.post<Tuteur>(`${this.baseUrl}/tutors`, request, { headers: this.getHeaders() })
      .pipe(
        tap(() => this.loadUsers()),
        catchError(this.handleError<Tuteur>('createTutor'))
      );
  }

  // Create admin using auth endpoint
  registerAdmin(request: CreateAdminRequest): Observable<any> {
    return this.http.post<any>(`${environment.BASE_URL}/api/auth/register/admin`, request, { headers: this.getHeaders() })
      .pipe(
        tap(() => this.loadUsers()),
        catchError(this.handleError<any>('registerAdmin'))
      );
  }

  // Create admin
  createAdmin(request: CreateAdminRequest): Observable<Admin> {
    return this.http.post<Admin>(`${this.baseUrl}/admins`, request, { headers: this.getHeaders() })
      .pipe(
        tap(() => this.loadUsers()),
        catchError(this.handleError<Admin>('createAdmin'))
      );
  }

  // Update user status (enable/disable)
  updateUserStatus(userId: number, enabled: boolean): Observable<any> {
    const body = { enabled };
    return this.http.put(`${environment.BASE_URL}/${userId}/status`, body, { headers: this.getHeaders() })
      .pipe(
        tap(() => this.loadUsers()),
        catchError(this.handleError('updateUserStatus'))
      );
  }

  // Get user count
  getUsersCount(): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/count`, { headers: this.getHeaders() })
      .pipe(
        catchError(this.handleError<number>('getUsersCount', 0))
      );
  }

  // Get statistics
  getUserStatistics(): Observable<UserStatistics> {
    return this.http.get<UserStatistics>(`${this.baseUrl}/statistics`, { headers: this.getHeaders() })
      .pipe(
        tap(stats => this.statisticsSubject.next(stats)),
        catchError(error => {
          console.error('Error fetching user statistics:', error);
          // Return default statistics in case of error
          const defaultStats: UserStatistics = {
            totalUsers: 0,
            activeUsers: 0,
            blockedUsers: 0,
            totalStudents: 0,
            totalTutors: 0,
            activeUsersPercentage: 0,
            blockedUsersPercentage: 0
          };
          return of(defaultStats);
        })
      );
  }

  // Utility methods
  private transformUsersToManagement(users: User[]): UserManagement[] {
    return users.map(user => ({
      id: user.id!,
      fullName: `${user.prenom} ${user.nom}`,
      email: user.email,
      role: this.determineUserRole(user),
      status: user.statut,
      dateInscription: user.dateInscription ? new Date(user.dateInscription) : new Date(),
      lastLogin: undefined, // Would come from separate API
      isActive: user.statut === UserStatus.ACTIVE,
      enabled: user.statut === UserStatus.ACTIVE,
      phone: user.telephone
    }));
  }

  private transformUsersWithRolesToManagement(users: UserWithRolesDTO[]): UserManagement[] {
    return users.map(user => ({
      id: user.id,
      fullName: `${user.prenom} ${user.nom}`,
      email: user.email,
      role: this.determineUserRoleFromDTO(user),
      status: user.statut,
      dateInscription: user.dateInscription ? new Date(user.dateInscription) : new Date(),
      lastLogin: user.lastLogin ? new Date(user.lastLogin) : undefined,
      isActive: user.statut === UserStatus.ACTIVE,
      enabled: user.statut === UserStatus.ACTIVE,
      phone: user.telephone
    }));
  }

  private determineUserRole(user: User): UserRole {
    // This would be determined by checking user type or role field
    // For now, using a simple heuristic based on available fields
    if ('filiere' in user) return UserRole.STUDENT;
    if ('experience' in user && 'tarif' in user) return UserRole.TUTOR;
    if ('permissions' in user) return UserRole.ADMIN;
    return UserRole.STUDENT; // Default
  }

  private determineUserRoleFromDTO(user: UserWithRolesDTO): UserRole {
    // First try to use the userType field which is more reliable
    if (user.userType) {
      const userType = user.userType.toLowerCase();
      if (userType.includes('admin')) {
        return UserRole.ADMIN;
      }
      if (userType.includes('tuteur')) {
        return UserRole.TUTOR;
      }
      if (userType.includes('etudiant')) {
        return UserRole.STUDENT;
      }
    }

    // Fallback to primaryRole field
    if (user.primaryRole) {
      const primaryRole = user.primaryRole.toLowerCase();
      if (primaryRole.includes('admin')) {
        return UserRole.ADMIN;
      }
      if (primaryRole.includes('tutor') || primaryRole.includes('tuteur')) {
        return UserRole.TUTOR;
      }
      if (primaryRole.includes('student') || primaryRole.includes('etudiant')) {
        return UserRole.STUDENT;
      }
    }

    // Fallback to roles array
    if (user.roles && user.roles.length > 0) {
      // Check for admin role first (highest priority)
      if (user.roles.some(role => role.toLowerCase().includes('admin'))) {
        return UserRole.ADMIN;
      }
      // Check for tutor role
      if (user.roles.some(role => role.toLowerCase().includes('tuteur') || role.toLowerCase().includes('tutor'))) {
        return UserRole.TUTOR;
      }
      // Check for student role
      if (user.roles.some(role => role.toLowerCase().includes('etudiant') || role.toLowerCase().includes('student'))) {
        return UserRole.STUDENT;
      }
    }
    
    // Default to student if no roles found
    return UserRole.STUDENT;
  }

  private filterUsersByParams(users: UserManagement[], params: UserSearchParams): UserManagement[] {
    let filteredUsers = [...users];

    // Pattern-based search
    if (params.pattern) {
      const pattern = params.pattern.toLowerCase();
      filteredUsers = filteredUsers.filter(user => 
        user.fullName.toLowerCase().includes(pattern) ||
        user.email.toLowerCase().includes(pattern) ||
        (user.phone && user.phone.toLowerCase().includes(pattern))
      );
    }

    if (params.status) {
      filteredUsers = filteredUsers.filter(user => user.status === params.status);
    }

    if (params.role) {
      filteredUsers = filteredUsers.filter(user => user.role === params.role);
    }

    // Sorting
    if (params.sortBy) {
      filteredUsers.sort((a, b) => {
        const aValue = (a as any)[params.sortBy!];
        const bValue = (b as any)[params.sortBy!];
        
        if (params.sortDirection === 'desc') {
          return bValue > aValue ? 1 : -1;
        }
        return aValue > bValue ? 1 : -1;
      });
    }

    // Pagination
    if (params.page !== undefined && params.size !== undefined) {
      const startIndex = params.page * params.size;
      const endIndex = startIndex + params.size;
      filteredUsers = filteredUsers.slice(startIndex, endIndex);
    }

    return filteredUsers;
  }

  private calculateStatistics(users: UserManagement[]): UserStatistics {
    const totalUsers = users.length;
    const activeUsers = users.filter(u => u.status === UserStatus.ACTIVE).length;
    const blockedUsers = users.filter(u => u.status === UserStatus.SUSPENDED).length;

    return {
      totalUsers,
      activeUsers,
      blockedUsers,
      totalStudents: users.filter(u => u.role === UserRole.STUDENT).length,
      totalTutors: users.filter(u => u.role === UserRole.TUTOR).length,
      activeUsersPercentage: totalUsers > 0 ? (activeUsers * 100.0) / totalUsers : 0,
      blockedUsersPercentage: totalUsers > 0 ? (blockedUsers * 100.0) / totalUsers : 0
    };
  }

  private getEmptyStatistics(): UserStatistics {
    return {
      totalUsers: 0,
      activeUsers: 0,
      blockedUsers: 0,
      totalStudents: 0,
      totalTutors: 0,
      activeUsersPercentage: 0,
      blockedUsersPercentage: 0
    };
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }

  // Public getters for current state
  getCurrentUsers(): UserManagement[] {
    return this.usersSubject.value;
  }

  getCurrentStatistics(): UserStatistics | null {
    return this.statisticsSubject.value;
  }

  refreshData(): void {
    this.loadUsers();
    this.getUserStatistics().subscribe();
  }

  // Block user
  blockUser(userId: number, reason?: string): Observable<UserWithRolesDTO> {
    const requestBody: UserStatusChangeRequest = { reason: reason || 'No reason provided' };
    
    return this.http.post<UserWithRolesDTO>(
      `${this.baseUrl}/${userId}/block`, 
      requestBody, 
      { headers: this.getHeaders() }
    ).pipe(
      tap(() => {
        // Refresh the users list after blocking
        this.loadUsers();
      }),
      catchError(error => {
        console.error('Error blocking user:', error);
        throw error;
      })
    );
  }

  // Unblock user
  unblockUser(userId: number, reason?: string): Observable<UserWithRolesDTO> {
    const requestBody: UserStatusChangeRequest = { reason: reason || 'No reason provided' };
    
    return this.http.post<UserWithRolesDTO>(
      `${this.baseUrl}/${userId}/unblock`, 
      requestBody, 
      { headers: this.getHeaders() }
    ).pipe(
      tap(() => {
        // Refresh the users list after unblocking
        this.loadUsers();
      }),
      catchError(error => {
        console.error('Error unblocking user:', error);
        throw error;
      })
    );
  }

  // Update user information
  updateUser(userId: number, updateRequest: UpdateUserRequest): Observable<UserWithRolesDTO> {
    return this.http.put<UserWithRolesDTO>(`${this.baseUrl}/${userId}`, updateRequest, { headers: this.getHeaders() })
      .pipe(
        tap(() => {
          // Refresh the user list after successful update
          this.loadUsers();
        }),
        catchError(error => {
          console.error('Error updating user:', error);
          throw error;
        })
      );
  }

  // Get all users with roles (for admin interface)
  getAllUsersWithRoles(): Observable<UserWithRolesDTO[]> {
    return this.http.get<UserWithRolesDTO[]>(this.baseUrl, { headers: this.getHeaders() })
      .pipe(
        catchError(error => {
          console.error('Error fetching users with roles:', error);
          return of([]);
        })
      );
  }
}