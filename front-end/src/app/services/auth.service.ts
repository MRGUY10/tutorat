import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { map, tap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { 
  LoginRequest, 
  AuthResponse, 
  StudentRegisterRequest, 
  TutorRegisterRequest,
  UserRole,
  UserType,
  CurrentUser,
  LoginState,
  DecodedToken,
  ApiError
} from '../core/models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
    private currentUserInfoSubject = new BehaviorSubject<CurrentUser | null>(null);
    private loginStateSubject = new BehaviorSubject<LoginState>(this.getInitialState());
    private apiUrl = environment.BASE_URL + '/api/auth';

    constructor(private http: HttpClient, private router: Router) {
        // Initialize from localStorage if available
        this.initializeFromStorage();
    }

    // ================================================
    // OBSERVABLE STREAMS
    // ================================================
    
    get currentUser$(): Observable<AuthResponse | null> {
        return this.currentUserSubject.asObservable();
    }

    get currentUserInfo$(): Observable<CurrentUser | null> {
        return this.currentUserInfoSubject.asObservable();
    }

    get loginState$(): Observable<LoginState> {
        return this.loginStateSubject.asObservable();
    }

    get isAuthenticated$(): Observable<boolean> {
        return this.loginStateSubject.pipe(
            map(state => state.isLoggedIn && !this.isTokenExpired())
        );
    }

    // ================================================
    // AUTHENTICATION METHODS
    // ================================================

    login(email: string, password: string): Observable<AuthResponse> {
        const request: LoginRequest = { email, motDePasse: password };
        
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
            tap(response => {
                // Check if user has USER role - not allowed to login
                if (response.role === 'USER') {
                    this.clearAuthData();
                    throw new Error('Users with role USER are not allowed to login');
                }
                
                // Store authentication data
                this.storeAuthData(response);
                
                // Update user info from token
                const userInfo = this.extractUserInfoFromResponse(response);
                this.currentUserInfoSubject.next(userInfo);
                
                // Update login state
                this.updateLoginState(response, userInfo);
                
                console.log('Login successful:', userInfo);
            }),
            catchError(error => {
                console.error('Login error:', error);
                this.clearAuthData();
                return throwError(() => error);
            })
        );
    }

    registerStudent(request: StudentRegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register/student`, request).pipe(
            tap(response => {
                this.storeAuthData(response);
                const userInfo = this.extractUserInfoFromResponse(response);
                this.currentUserInfoSubject.next(userInfo);
                this.updateLoginState(response, userInfo);
            }),
            catchError(error => {
                console.error('Student registration error:', error);
                return throwError(() => error);
            })
        );
    }

    registerTutor(request: TutorRegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register/tutor`, request).pipe(
            tap(response => {
                this.storeAuthData(response);
                const userInfo = this.extractUserInfoFromResponse(response);
                this.currentUserInfoSubject.next(userInfo);
                this.updateLoginState(response, userInfo);
            }),
            catchError(error => {
                console.error('Tutor registration error:', error);
                return throwError(() => error);
            })
        );
    }

    logout(): void {
        // Clear all stored data
        this.clearAuthData();
        
        // Reset observables
        this.currentUserSubject.next(null);
        this.currentUserInfoSubject.next(null);
        this.loginStateSubject.next(this.getInitialState());
        
        // Navigate to login page
        this.router.navigate(['/login']);
        
        console.log('User logged out successfully');
    }

    refreshToken(): Observable<AuthResponse> {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            return throwError(() => new Error('No refresh token available'));
        }

        return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
            tap(response => {
                this.storeAuthData(response);
                const userInfo = this.extractUserInfoFromResponse(response);
                this.currentUserInfoSubject.next(userInfo);
                this.updateLoginState(response, userInfo);
            }),
            catchError(error => {
                console.error('Token refresh failed:', error);
                this.logout();
                return throwError(() => error);
            })
        );
    }

    // ================================================
    // TOKEN AND STATE MANAGEMENT
    // ================================================

    getToken(): string | null {
        return this.currentUserSubject.value?.token ?? null;
    }

    getRefreshToken(): string | null {
        return this.currentUserSubject.value?.refreshToken ?? null;
    }

    isAuthenticated(): boolean {
        const token = this.getToken();
        return !!token && !this.isTokenExpired();
    }

    isLoggedIn(): boolean {
        return this.isAuthenticated();
    }

    isTokenExpired(): boolean {
        const decodedToken = this.getDecodedToken();
        if (!decodedToken || !decodedToken.exp) return true;
        
        const currentTime = Date.now() / 1000;
        return decodedToken.exp < currentTime;
    }

    storeAuthData(authResponse: AuthResponse): void {
        // Store authentication data in localStorage
        const authData = {
            token: authResponse.token,
            refreshToken: authResponse.refreshToken,
            expiresIn: authResponse.expiresIn,
            email: authResponse.email,
            role: authResponse.role,
            userId: authResponse.userId,
            tokenType: 'Bearer',
            issuedAt: new Date().toISOString(),
            expiresAt: new Date(Date.now() + (authResponse.expiresIn * 1000)).toISOString()
        };
        
        localStorage.setItem('auth_tokens', JSON.stringify(authData));
        localStorage.setItem('user_role', authResponse.role);
        localStorage.setItem('user_id', authResponse.userId.toString());
        localStorage.setItem('user_email', authResponse.email);
        
        this.currentUserSubject.next(authResponse);
        
        console.log('Auth data stored successfully:', {
            email: authResponse.email,
            role: authResponse.role,
            expiresAt: authData.expiresAt
        });
    }

    getDecodedToken(): DecodedToken | null {
        const token = this.getToken();
        if (!token) return null;
        
        try {
            const payload = token.split('.')[1];
            const decoded = JSON.parse(atob(payload));
            return decoded as DecodedToken;
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    }

    // ================================================
    // ROLE-BASED ACCESS CONTROL
    // ================================================

    getCurrentUser(): CurrentUser | null {
        return this.currentUserInfoSubject.value;
    }

    getUserRole(): UserRole | null {
        const decodedToken = this.getDecodedToken();
        return decodedToken?.role || null;
    }

    getUserType(): UserType | null {
        const decodedToken = this.getDecodedToken();
        return decodedToken?.userType || null;
    }

    hasRole(role: UserRole): boolean {
        const userRole = this.getUserRole();
        return userRole === role;
    }

    hasAnyRole(roles: UserRole[]): boolean {
        const userRole = this.getUserRole();
        return userRole ? roles.includes(userRole) : false;
    }

    hasAllRoles(roles: UserRole[]): boolean {
        const userRole = this.getUserRole();
        // For single role system, user can only have all roles if it's one role
        return roles.length === 1 && userRole === roles[0];
    }

    isStudent(): boolean {
        return this.hasRole(UserRole.STUDENT);
    }

    isTutor(): boolean {
        return this.hasRole(UserRole.TUTOR);
    }

    isAdmin(): boolean {
        return this.hasRole(UserRole.ADMIN);
    }

    canAccess(requiredRoles: UserRole[]): boolean {
        if (!this.isAuthenticated()) return false;
        if (!requiredRoles || requiredRoles.length === 0) return true;
        return this.hasAnyRole(requiredRoles);
    }

    // ================================================
    // USER PROFILE AND INFO
    // ================================================

    get userInfo(): CurrentUser | null {
        return this.getCurrentUser();
    }

    getUserProfile(): Observable<any> {
        return this.http.get(`${this.apiUrl}/profile`);
    }

    getUserId(): number | null {
        const decodedToken = this.getDecodedToken();
        return decodedToken?.userId || null;
    }

    getUserEmail(): string | null {
        const decodedToken = this.getDecodedToken();
        return decodedToken?.email || null;
    }

    // ================================================
    // HELPER METHODS
    // ================================================

    private initializeFromStorage(): void {
        const saved = localStorage.getItem('auth_tokens');
        if (saved) {
            try {
                const authData = JSON.parse(saved);
                this.currentUserSubject.next(authData);
                
                // Check if token is still valid
                if (!this.isTokenExpired()) {
                    const userInfo = this.extractUserInfoFromResponse(authData);
                    this.currentUserInfoSubject.next(userInfo);
                    this.updateLoginState(authData, userInfo);
                } else {
                    // Token expired, clear storage
                    this.clearAuthData();
                }
            } catch (error) {
                console.error('Error parsing stored auth data:', error);
                this.clearAuthData();
            }
        }
    }

    private extractUserInfoFromResponse(response: AuthResponse): CurrentUser {
        const decodedToken = this.getDecodedTokenFromString(response.token);
        
        return {
            id: response.userId,
            nom: response.userInfo?.nom || '',
            prenom: response.userInfo?.prenom || '',
            email: response.email,
            role: decodedToken?.role || UserRole.STUDENT,
            userType: decodedToken?.userType || UserType.STUDENT,
            roles: [decodedToken?.role || UserRole.STUDENT],
            isAuthenticated: true,
            profileComplete: true
        };
    }

    private getDecodedTokenFromString(token: string): DecodedToken | null {
        try {
            const payload = token.split('.')[1];
            return JSON.parse(atob(payload)) as DecodedToken;
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    }

    private updateLoginState(response: AuthResponse, userInfo: CurrentUser): void {
        const decodedToken = this.getDecodedTokenFromString(response.token);
        const expiryTime = decodedToken ? new Date(decodedToken.exp * 1000) : null;
        
        const loginState: LoginState = {
            isLoggedIn: true,
            user: userInfo,
            token: response.token,
            refreshToken: response.refreshToken,
            loginTime: new Date(),
            expiryTime: expiryTime
        };
        
        this.loginStateSubject.next(loginState);
    }

    private clearAuthData(): void {
        // Remove all authentication-related data from localStorage
        localStorage.removeItem('auth_tokens');
        localStorage.removeItem('user_role');
        localStorage.removeItem('user_id');
        localStorage.removeItem('user_email');
        
        console.log('All auth data cleared from localStorage');
    }

    private getInitialState(): LoginState {
        return {
            isLoggedIn: false,
            user: null,
            token: null,
            refreshToken: null,
            loginTime: null,
            expiryTime: null
        };
    }

    // ================================================
    // NAVIGATION HELPERS
    // ================================================

    getDefaultRoute(): string {
        const userRole = this.getUserRole();
        
        switch (userRole) {
            case UserRole.ADMIN:
                return '/tutor-verification';
            case UserRole.TUTOR:
                return '/dashboard';
            case UserRole.STUDENT:
                return '/dashboard';
            default:
                return '/login';
        }
    }

    redirectAfterLogin(): void {
        const defaultRoute = this.getDefaultRoute();
        this.router.navigate([defaultRoute]);
    }

    // ================================================
    // AUTO LOGOUT ON TOKEN EXPIRY
    // ================================================

    checkTokenExpiry(): void {
        if (this.isAuthenticated() && this.isTokenExpired()) {
            console.warn('Token expired, logging out automatically');
            this.logout();
        }
    }

    // ================================================
    // COMPATIBILITY METHODS (for existing components)
    // ================================================

    hasAnyRoleString(roles: string[]): boolean {
        const userRole = this.getUserRole();
        return userRole ? roles.includes(userRole) : false;
    }

    // ================================================
    // ENHANCED LOCALSTORAGE UTILITIES
    // ================================================

    /**
     * Get stored user role directly from localStorage
     */
    getStoredUserRole(): string | null {
        return localStorage.getItem('user_role');
    }

    /**
     * Get stored user ID directly from localStorage
     */
    getStoredUserId(): number | null {
        const userId = localStorage.getItem('user_id');
        return userId ? parseInt(userId, 10) : null;
    }

    /**
     * Get stored user email directly from localStorage
     */
    getStoredUserEmail(): string | null {
        return localStorage.getItem('user_email');
    }

    /**
     * Check if authentication data exists in localStorage
     */
    hasStoredAuthData(): boolean {
        return !!localStorage.getItem('auth_tokens');
    }

    /**
     * Get complete auth data from localStorage
     */
    getStoredAuthData(): any | null {
        try {
            const authData = localStorage.getItem('auth_tokens');
            return authData ? JSON.parse(authData) : null;
        } catch (error) {
            console.error('Error parsing stored auth data:', error);
            this.clearAuthData();
            return null;
        }
    }

    /**
     * Validate stored token expiry
     */
    isStoredTokenValid(): boolean {
        const authData = this.getStoredAuthData();
        if (!authData || !authData.expiresAt) return false;
        
        const expiryTime = new Date(authData.expiresAt);
        const now = new Date();
        
        return expiryTime > now;
    }
}