import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, interval, timer, fromEvent } from 'rxjs';
import { AuthService } from './auth.service';
import { CurrentUser, LoginState, SessionInfo, SessionActivity, DeviceInfo } from '../models/auth.models';
import { takeUntil, filter, tap } from 'rxjs/operators';

/**
 * Comprehensive Session Management Service
 * Handles session persistence, automatic token refresh, idle timeout,
 * activity tracking, and session security features
 */
@Injectable({ providedIn: 'root' })
export class SessionManagementService {
    
    // Session state observables
    private sessionInfoSubject = new BehaviorSubject<SessionInfo | null>(null);
    private sessionActivitySubject = new BehaviorSubject<SessionActivity | null>(null);
    private isActiveSessionSubject = new BehaviorSubject<boolean>(false);
    
    // Configuration
    private readonly SESSION_CHECK_INTERVAL = 30000; // 30 seconds
    private readonly IDLE_WARNING_TIME = 25 * 60 * 1000; // 25 minutes
    private readonly IDLE_LOGOUT_TIME = 30 * 60 * 1000; // 30 minutes
    private readonly REFRESH_TOKEN_BUFFER = 5 * 60 * 1000; // 5 minutes before expiry
    
    // Activity tracking
    private lastActivityTime: Date = new Date();
    private sessionStartTime: Date | null = null;
    private activityTimer: any;
    private refreshTimer: any;
    
    constructor(private authService: AuthService) {
        this.initializeSessionManagement();
    }
    
    // ================================================
    // PUBLIC OBSERVABLES
    // ================================================
    
    get sessionInfo$(): Observable<SessionInfo | null> {
        return this.sessionInfoSubject.asObservable();
    }
    
    get sessionActivity$(): Observable<SessionActivity | null> {
        return this.sessionActivitySubject.asObservable();
    }
    
    get isActiveSession$(): Observable<boolean> {
        return this.isActiveSessionSubject.asObservable();
    }
    
    // ================================================
    // SESSION LIFECYCLE MANAGEMENT
    // ================================================
    
    /**
     * Initialize session management system
     */
    private initializeSessionManagement(): void {
        // Listen to auth state changes
        this.authService.loginState$.subscribe(loginState => {
            if (loginState.isLoggedIn) {
                this.startSessionManagement(loginState);
            } else {
                this.stopSessionManagement();
            }
        });
        
        // Track user activity
        this.setupActivityTracking();
        
        // Setup automatic token refresh
        this.setupAutomaticTokenRefresh();
        
        console.log('Session management initialized');
    }
    
    /**
     * Start session management when user logs in
     */
    private startSessionManagement(loginState: LoginState): void {
        this.sessionStartTime = new Date();
        this.lastActivityTime = new Date();
        
        // Create session info
        const sessionInfo: SessionInfo = {
            sessionId: this.generateSessionId(),
            userId: loginState.user?.id || 0,
            userEmail: loginState.user?.email || '',
            userRole: loginState.user?.role || '',
            loginTime: loginState.loginTime || new Date(),
            lastActivity: this.lastActivityTime,
            expiryTime: loginState.expiryTime || new Date(),
            isActive: true,
            deviceInfo: this.getDeviceInfo(),
            ipAddress: null, // Would be set by backend
            sessionDuration: 0
        };
        
        this.sessionInfoSubject.next(sessionInfo);
        this.isActiveSessionSubject.next(true);
        
        // Start session monitoring
        this.startSessionMonitoring();
        
        console.log('Session management started:', sessionInfo);
    }
    
    /**
     * Stop session management when user logs out
     */
    private stopSessionManagement(): void {
        // Clear timers
        if (this.activityTimer) {
            clearInterval(this.activityTimer);
        }
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
        }
        
        // Update session state
        this.sessionInfoSubject.next(null);
        this.sessionActivitySubject.next(null);
        this.isActiveSessionSubject.next(false);
        
        this.sessionStartTime = null;
        
        console.log('Session management stopped');
    }
    
    // ================================================
    // ACTIVITY TRACKING
    // ================================================
    
    /**
     * Setup activity tracking for user interactions
     */
    private setupActivityTracking(): void {
        // Track mouse movements, clicks, and keyboard events
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
        
        events.forEach(event => {
            document.addEventListener(event, () => {
                this.recordActivity(event);
            }, { passive: true });
        });
        
        // Track page visibility changes
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden) {
                this.recordActivity('page-visible');
            }
        });
    }
    
    /**
     * Record user activity
     */
    private recordActivity(activityType: string): void {
        if (!this.isActiveSessionSubject.value) return;
        
        this.lastActivityTime = new Date();
        
        const activity: SessionActivity = {
            timestamp: this.lastActivityTime,
            type: activityType,
            page: window.location.pathname,
            idleTime: this.calculateIdleTime(),
            sessionDuration: this.calculateSessionDuration()
        };
        
        this.sessionActivitySubject.next(activity);
        
        // Update session info with latest activity
        const currentSession = this.sessionInfoSubject.value;
        if (currentSession) {
            currentSession.lastActivity = this.lastActivityTime;
            currentSession.sessionDuration = activity.sessionDuration;
            this.sessionInfoSubject.next(currentSession);
        }
    }
    
    /**
     * Calculate idle time in milliseconds
     */
    private calculateIdleTime(): number {
        return Date.now() - this.lastActivityTime.getTime();
    }
    
    /**
     * Calculate total session duration in milliseconds
     */
    private calculateSessionDuration(): number {
        if (!this.sessionStartTime) return 0;
        return Date.now() - this.sessionStartTime.getTime();
    }
    
    // ================================================
    // SESSION MONITORING
    // ================================================
    
    /**
     * Start monitoring session for idle timeout and token expiry
     */
    private startSessionMonitoring(): void {
        // Check session status every 30 seconds
        this.activityTimer = setInterval(() => {
            this.checkSessionStatus();
        }, this.SESSION_CHECK_INTERVAL);
    }
    
    /**
     * Check session status for idle timeout and token expiry
     */
    private checkSessionStatus(): void {
        const idleTime = this.calculateIdleTime();
        const currentSession = this.sessionInfoSubject.value;
        
        if (!currentSession || !this.authService.isAuthenticated()) {
            this.stopSessionManagement();
            return;
        }
        
        // Check for idle timeout warning (25 minutes)
        if (idleTime >= this.IDLE_WARNING_TIME && idleTime < this.IDLE_LOGOUT_TIME) {
            this.showIdleWarning(idleTime);
        }
        
        // Check for idle logout (30 minutes)
        if (idleTime >= this.IDLE_LOGOUT_TIME) {
            console.warn('Session expired due to inactivity');
            this.logoutDueToInactivity();
            return;
        }
        
        // Check token expiry and refresh if needed
        this.checkTokenExpiryAndRefresh();
    }
    
    /**
     * Show idle timeout warning
     */
    private showIdleWarning(idleTime: number): void {
        const remainingTime = this.IDLE_LOGOUT_TIME - idleTime;
        const minutes = Math.floor(remainingTime / (60 * 1000));
        
        console.warn(`Session will expire in ${minutes} minutes due to inactivity`);
        
        // You could emit an event or show a notification here
        // this.notificationService.showIdleWarning(minutes);
    }
    
    /**
     * Logout user due to inactivity
     */
    private logoutDueToInactivity(): void {
        console.log('Logging out user due to inactivity');
        this.authService.logout();
        
        // Show notification about automatic logout
        // this.notificationService.showInactivityLogout();
    }
    
    // ================================================
    // AUTOMATIC TOKEN REFRESH
    // ================================================
    
    /**
     * Setup automatic token refresh before expiry
     */
    private setupAutomaticTokenRefresh(): void {
        this.authService.loginState$.subscribe(loginState => {
            if (loginState.isLoggedIn && loginState.expiryTime) {
                this.scheduleTokenRefresh(loginState.expiryTime);
            }
        });
    }
    
    /**
     * Schedule token refresh before expiry
     */
    private scheduleTokenRefresh(expiryTime: Date): void {
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
        }
        
        const timeUntilRefresh = expiryTime.getTime() - Date.now() - this.REFRESH_TOKEN_BUFFER;
        
        if (timeUntilRefresh > 0) {
            this.refreshTimer = setTimeout(() => {
                this.performAutomaticTokenRefresh();
            }, timeUntilRefresh);
            
            console.log(`Token refresh scheduled in ${Math.floor(timeUntilRefresh / 60000)} minutes`);
        }
    }
    
    /**
     * Perform automatic token refresh
     */
    private performAutomaticTokenRefresh(): void {
        if (!this.authService.isAuthenticated()) {
            return;
        }
        
        console.log('Performing automatic token refresh');
        
        this.authService.refreshToken().subscribe({
            next: (response) => {
                console.log('Token refreshed automatically');
                // Schedule next refresh
                if (response.expiresIn) {
                    const nextExpiry = new Date(Date.now() + response.expiresIn * 1000);
                    this.scheduleTokenRefresh(nextExpiry);
                }
            },
            error: (error) => {
                console.error('Automatic token refresh failed:', error);
                // If refresh fails, logout the user
                this.authService.logout();
            }
        });
    }
    
    /**
     * Check token expiry and refresh if needed during session monitoring
     */
    private checkTokenExpiryAndRefresh(): void {
        if (!this.authService.isAuthenticated()) {
            return;
        }
        
        // Check if token will expire soon
        if (this.authService.isTokenExpired()) {
            console.warn('Token expired, attempting to refresh');
            this.performAutomaticTokenRefresh();
        }
    }
    
    // ================================================
    // SESSION UTILITIES
    // ================================================
    
    /**
     * Generate unique session ID
     */
    private generateSessionId(): string {
        return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }
    
    /**
     * Get device information
     */
    private getDeviceInfo(): DeviceInfo {
        const userAgent = navigator.userAgent;
        
        return {
            userAgent: userAgent,
            platform: navigator.platform,
            language: navigator.language,
            screenResolution: `${screen.width}x${screen.height}`,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            browserName: this.getBrowserName(userAgent),
            browserVersion: this.getBrowserVersion(userAgent),
            isMobile: this.isMobileDevice(userAgent)
        };
    }
    
    /**
     * Get browser name from user agent
     */
    private getBrowserName(userAgent: string): string {
        if (userAgent.includes('Chrome')) return 'Chrome';
        if (userAgent.includes('Firefox')) return 'Firefox';
        if (userAgent.includes('Safari')) return 'Safari';
        if (userAgent.includes('Edge')) return 'Edge';
        return 'Unknown';
    }
    
    /**
     * Get browser version from user agent
     */
    private getBrowserVersion(userAgent: string): string {
        const match = userAgent.match(/(Chrome|Firefox|Safari|Edge)\/(\d+\.\d+)/);
        return match ? match[2] : 'Unknown';
    }
    
    /**
     * Check if device is mobile
     */
    private isMobileDevice(userAgent: string): boolean {
        return /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(userAgent);
    }
    
    // ================================================
    // PUBLIC METHODS
    // ================================================
    
    /**
     * Get current session information
     */
    getCurrentSession(): SessionInfo | null {
        return this.sessionInfoSubject.value;
    }
    
    /**
     * Get last activity information
     */
    getLastActivity(): SessionActivity | null {
        return this.sessionActivitySubject.value;
    }
    
    /**
     * Check if session is currently active
     */
    isSessionActive(): boolean {
        return this.isActiveSessionSubject.value;
    }
    
    /**
     * Extend session by recording activity
     */
    extendSession(): void {
        this.recordActivity('manual-extension');
    }
    
    /**
     * Get session statistics
     */
    getSessionStats(): any {
        const session = this.sessionInfoSubject.value;
        const activity = this.sessionActivitySubject.value;
        
        if (!session) return null;
        
        return {
            sessionDuration: this.calculateSessionDuration(),
            idleTime: this.calculateIdleTime(),
            loginTime: session.loginTime,
            lastActivity: session.lastActivity,
            expiryTime: session.expiryTime,
            isActive: session.isActive,
            deviceInfo: session.deviceInfo
        };
    }
    
    /**
     * Force session refresh
     */
    forceTokenRefresh(): Observable<any> {
        return this.authService.refreshToken();
    }
}