import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { DeviceInfo, SessionInfo, SecurityEvent, SecurityAlert } from '../models/auth.models';
import { SessionManagementService } from './session-management.service';
import { AuthService } from './auth.service';

/**
 * Session Security Service
 * Handles device tracking, suspicious activity detection,
 * and security alerts for user sessions
 */
@Injectable({ providedIn: 'root' })
export class SessionSecurityService {
    
    private securityEventsSubject = new BehaviorSubject<SecurityEvent[]>([]);
    private securityAlertsSubject = new BehaviorSubject<SecurityAlert[]>([]);
    private trustedDevicesSubject = new BehaviorSubject<DeviceInfo[]>([]);
    
    // Security thresholds
    private readonly MAX_FAILED_ATTEMPTS = 3;
    private readonly SUSPICIOUS_LOGIN_WINDOW = 5 * 60 * 1000; // 5 minutes
    private readonly MAX_CONCURRENT_SESSIONS = 3;
    
    // Tracking data
    private failedAttempts: Map<string, number> = new Map();
    private loginAttempts: Array<{timestamp: Date, ip?: string, success: boolean}> = [];
    private deviceFingerprint: string | null = null;
    
    constructor(
        private sessionService: SessionManagementService,
        private authService: AuthService
    ) {
        this.initializeSecurityMonitoring();
    }
    
    // ================================================
    // PUBLIC OBSERVABLES
    // ================================================
    
    get securityEvents$(): Observable<SecurityEvent[]> {
        return this.securityEventsSubject.asObservable();
    }
    
    get securityAlerts$(): Observable<SecurityAlert[]> {
        return this.securityAlertsSubject.asObservable();
    }
    
    get trustedDevices$(): Observable<DeviceInfo[]> {
        return this.trustedDevicesSubject.asObservable();
    }
    
    // ================================================
    // SECURITY MONITORING
    // ================================================
    
    /**
     * Initialize security monitoring
     */
    private initializeSecurityMonitoring(): void {
        // Generate device fingerprint
        this.generateDeviceFingerprint();
        
        // Load trusted devices from storage
        this.loadTrustedDevices();
        
        // Monitor auth events
        this.authService.loginState$.subscribe(loginState => {
            if (loginState.isLoggedIn) {
                this.handleSuccessfulLogin();
            }
        });
        
        console.log('Session security monitoring initialized');
    }
    
    /**
     * Generate unique device fingerprint
     */
    private generateDeviceFingerprint(): void {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        
        if (ctx) {
            ctx.textBaseline = 'top';
            ctx.font = '14px Arial';
            ctx.fillText('Device fingerprint', 2, 2);
        }
        
        const fingerprint = {
            userAgent: navigator.userAgent,
            language: navigator.language,
            platform: navigator.platform,
            screenResolution: `${screen.width}x${screen.height}`,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            canvasFingerprint: canvas.toDataURL(),
            webglVendor: this.getWebGLVendor(),
            plugins: this.getPluginsList(),
            fonts: this.getAvailableFonts()
        };
        
        this.deviceFingerprint = btoa(JSON.stringify(fingerprint)).substr(0, 32);
        
        console.log('Device fingerprint generated:', this.deviceFingerprint);
    }
    
    /**
     * Get WebGL vendor information
     */
    private getWebGLVendor(): string {
        try {
            const canvas = document.createElement('canvas');
            const gl = canvas.getContext('webgl') as WebGLRenderingContext;
            if (gl) {
                const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
                if (debugInfo) {
                    return gl.getParameter((debugInfo as any).UNMASKED_VENDOR_WEBGL);
                }
            }
        } catch (e) {
            // Ignore errors
        }
        return 'Unknown';
    }
    
    /**
     * Get list of browser plugins
     */
    private getPluginsList(): string[] {
        const plugins = [];
        for (let i = 0; i < navigator.plugins.length; i++) {
            plugins.push(navigator.plugins[i].name);
        }
        return plugins;
    }
    
    /**
     * Detect available fonts (simplified)
     */
    private getAvailableFonts(): string[] {
        const testFonts = ['Arial', 'Helvetica', 'Times', 'Courier', 'Verdana', 'Georgia', 'Impact'];
        const availableFonts: string[] = [];
        
        testFonts.forEach(font => {
            if (this.isFontAvailable(font)) {
                availableFonts.push(font);
            }
        });
        
        return availableFonts;
    }
    
    /**
     * Check if font is available
     */
    private isFontAvailable(font: string): boolean {
        const testString = 'mmmmmmmmmmlli';
        const testSize = '72px';
        const baseline = 'monospace';
        
        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        
        if (!context) return false;
        
        context.font = testSize + ' ' + baseline;
        const baselineWidth = context.measureText(testString).width;
        
        context.font = testSize + ' ' + font + ', ' + baseline;
        const fontWidth = context.measureText(testString).width;
        
        return fontWidth !== baselineWidth;
    }
    
    // ================================================
    // LOGIN SECURITY
    // ================================================
    
    /**
     * Handle successful login event
     */
    private handleSuccessfulLogin(): void {
        const currentDevice = this.getCurrentDeviceInfo();
        
        // Record login attempt
        this.recordLoginAttempt(true);
        
        // Check if device is trusted
        if (!this.isDeviceTrusted(currentDevice)) {
            this.createSecurityAlert('NEW_DEVICE_LOGIN', 'Login from new device detected', {
                device: currentDevice,
                timestamp: new Date()
            });
        }
        
        // Check for suspicious timing
        this.checkSuspiciousLoginTiming();
        
        // Record security event
        this.recordSecurityEvent('LOGIN_SUCCESS', 'User logged in successfully');
        
        // Clear failed attempts on successful login
        this.clearFailedAttempts();
    }
    
    /**
     * Handle failed login attempt
     */
    handleFailedLogin(email: string): void {
        this.recordLoginAttempt(false);
        
        // Increment failed attempts
        const attempts = (this.failedAttempts.get(email) || 0) + 1;
        this.failedAttempts.set(email, attempts);
        
        // Check for brute force attack
        if (attempts >= this.MAX_FAILED_ATTEMPTS) {
            this.createSecurityAlert('BRUTE_FORCE_ATTEMPT', 
                `Multiple failed login attempts detected for ${email}`, {
                attempts: attempts,
                timestamp: new Date()
            });
        }
        
        this.recordSecurityEvent('LOGIN_FAILED', `Failed login attempt for ${email}`);
    }
    
    /**
     * Record login attempt
     */
    private recordLoginAttempt(success: boolean): void {
        this.loginAttempts.push({
            timestamp: new Date(),
            success: success
        });
        
        // Keep only last 10 attempts
        if (this.loginAttempts.length > 10) {
            this.loginAttempts = this.loginAttempts.slice(-10);
        }
    }
    
    /**
     * Check for suspicious login timing patterns
     */
    private checkSuspiciousLoginTiming(): void {
        const now = Date.now();
        const recentAttempts = this.loginAttempts.filter(
            attempt => now - attempt.timestamp.getTime() < this.SUSPICIOUS_LOGIN_WINDOW
        );
        
        // Check for rapid login attempts
        if (recentAttempts.length > 3) {
            this.createSecurityAlert('RAPID_LOGIN_ATTEMPTS', 
                'Unusually rapid login attempts detected', {
                attempts: recentAttempts.length,
                timeWindow: this.SUSPICIOUS_LOGIN_WINDOW / 1000 + ' seconds'
            });
        }
    }
    
    /**
     * Clear failed login attempts
     */
    private clearFailedAttempts(): void {
        this.failedAttempts.clear();
    }
    
    // ================================================
    // DEVICE MANAGEMENT
    // ================================================
    
    /**
     * Get current device information
     */
    private getCurrentDeviceInfo(): DeviceInfo {
        const userAgent = navigator.userAgent;
        
        return {
            userAgent: userAgent,
            platform: navigator.platform,
            language: navigator.language,
            screenResolution: `${screen.width}x${screen.height}`,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            browserName: this.getBrowserName(userAgent),
            browserVersion: this.getBrowserVersion(userAgent),
            isMobile: this.isMobileDevice(userAgent),
            fingerprint: this.deviceFingerprint || 'unknown'
        };
    }
    
    /**
     * Check if device is in trusted list
     */
    private isDeviceTrusted(device: DeviceInfo): boolean {
        const trustedDevices = this.trustedDevicesSubject.value;
        return trustedDevices.some(trusted => 
            trusted.fingerprint === device.fingerprint ||
            (trusted.userAgent === device.userAgent && trusted.platform === device.platform)
        );
    }
    
    /**
     * Add device to trusted list
     */
    addTrustedDevice(device: DeviceInfo): void {
        const trustedDevices = this.trustedDevicesSubject.value;
        
        // Avoid duplicates
        if (!this.isDeviceTrusted(device)) {
            const updatedDevices = [...trustedDevices, { ...device, trustedAt: new Date() }];
            this.trustedDevicesSubject.next(updatedDevices);
            this.saveTrustedDevices(updatedDevices);
            
            this.recordSecurityEvent('DEVICE_TRUSTED', 'Device added to trusted list');
        }
    }
    
    /**
     * Remove device from trusted list
     */
    removeTrustedDevice(fingerprint: string): void {
        const trustedDevices = this.trustedDevicesSubject.value;
        const updatedDevices = trustedDevices.filter(device => device.fingerprint !== fingerprint);
        
        this.trustedDevicesSubject.next(updatedDevices);
        this.saveTrustedDevices(updatedDevices);
        
        this.recordSecurityEvent('DEVICE_UNTRUSTED', 'Device removed from trusted list');
    }
    
    /**
     * Load trusted devices from localStorage
     */
    private loadTrustedDevices(): void {
        try {
            const saved = localStorage.getItem('trusted_devices');
            if (saved) {
                const devices = JSON.parse(saved);
                this.trustedDevicesSubject.next(devices);
            }
        } catch (error) {
            console.error('Failed to load trusted devices:', error);
        }
    }
    
    /**
     * Save trusted devices to localStorage
     */
    private saveTrustedDevices(devices: DeviceInfo[]): void {
        try {
            localStorage.setItem('trusted_devices', JSON.stringify(devices));
        } catch (error) {
            console.error('Failed to save trusted devices:', error);
        }
    }
    
    // ================================================
    // SECURITY EVENTS & ALERTS
    // ================================================
    
    /**
     * Record a security event
     */
    private recordSecurityEvent(type: string, description: string, metadata?: any): void {
        const event: SecurityEvent = {
            id: this.generateEventId(),
            type: type,
            description: description,
            timestamp: new Date(),
            severity: this.getEventSeverity(type),
            metadata: metadata || {},
            deviceFingerprint: this.deviceFingerprint || 'unknown'
        };
        
        const events = this.securityEventsSubject.value;
        const updatedEvents = [event, ...events].slice(0, 100); // Keep last 100 events
        
        this.securityEventsSubject.next(updatedEvents);
        
        console.log('Security event recorded:', event);
    }
    
    /**
     * Create a security alert
     */
    private createSecurityAlert(type: string, message: string, details?: any): void {
        const alert: SecurityAlert = {
            id: this.generateAlertId(),
            type: type,
            message: message,
            severity: this.getAlertSeverity(type),
            timestamp: new Date(),
            dismissed: false,
            details: details || {}
        };
        
        const alerts = this.securityAlertsSubject.value;
        const updatedAlerts = [alert, ...alerts].slice(0, 50); // Keep last 50 alerts
        
        this.securityAlertsSubject.next(updatedAlerts);
        
        console.warn('Security alert created:', alert);
        
        // Show browser notification for high severity alerts
        if (alert.severity === 'HIGH') {
            this.showBrowserNotification(alert);
        }
    }
    
    /**
     * Dismiss security alert
     */
    dismissAlert(alertId: string): void {
        const alerts = this.securityAlertsSubject.value;
        const updatedAlerts = alerts.map(alert => 
            alert.id === alertId ? { ...alert, dismissed: true } : alert
        );
        
        this.securityAlertsSubject.next(updatedAlerts);
    }
    
    /**
     * Get event severity based on type
     */
    private getEventSeverity(type: string): 'LOW' | 'MEDIUM' | 'HIGH' {
        switch (type) {
            case 'LOGIN_FAILED':
            case 'LOGOUT':
                return 'MEDIUM';
            case 'LOGIN_SUCCESS':
            case 'DEVICE_TRUSTED':
                return 'LOW';
            default:
                return 'MEDIUM';
        }
    }
    
    /**
     * Get alert severity based on type
     */
    private getAlertSeverity(type: string): 'LOW' | 'MEDIUM' | 'HIGH' {
        switch (type) {
            case 'BRUTE_FORCE_ATTEMPT':
            case 'RAPID_LOGIN_ATTEMPTS':
                return 'HIGH';
            case 'NEW_DEVICE_LOGIN':
                return 'MEDIUM';
            default:
                return 'LOW';
        }
    }
    
    /**
     * Show browser notification for security alerts
     */
    private showBrowserNotification(alert: SecurityAlert): void {
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification('Security Alert', {
                body: alert.message,
                icon: '/assets/icons/security-alert.png'
            });
        }
    }
    
    // ================================================
    // UTILITY METHODS
    // ================================================
    
    private generateEventId(): string {
        return 'evt_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }
    
    private generateAlertId(): string {
        return 'alt_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }
    
    private getBrowserName(userAgent: string): string {
        if (userAgent.includes('Chrome')) return 'Chrome';
        if (userAgent.includes('Firefox')) return 'Firefox';
        if (userAgent.includes('Safari')) return 'Safari';
        if (userAgent.includes('Edge')) return 'Edge';
        return 'Unknown';
    }
    
    private getBrowserVersion(userAgent: string): string {
        const match = userAgent.match(/(Chrome|Firefox|Safari|Edge)\/(\d+\.\d+)/);
        return match ? match[2] : 'Unknown';
    }
    
    private isMobileDevice(userAgent: string): boolean {
        return /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(userAgent);
    }
    
    // ================================================
    // PUBLIC API
    // ================================================
    
    /**
     * Get current device fingerprint
     */
    getCurrentDeviceFingerprint(): string | null {
        return this.deviceFingerprint;
    }
    
    /**
     * Get security events
     */
    getSecurityEvents(): SecurityEvent[] {
        return this.securityEventsSubject.value;
    }
    
    /**
     * Get security alerts
     */
    getSecurityAlerts(): SecurityAlert[] {
        return this.securityAlertsSubject.value;
    }
    
    /**
     * Get trusted devices
     */
    getTrustedDevices(): DeviceInfo[] {
        return this.trustedDevicesSubject.value;
    }
    
    /**
     * Trust current device
     */
    trustCurrentDevice(): void {
        const currentDevice = this.getCurrentDeviceInfo();
        this.addTrustedDevice(currentDevice);
    }
    
    /**
     * Request notification permission
     */
    async requestNotificationPermission(): Promise<boolean> {
        if ('Notification' in window) {
            const permission = await Notification.requestPermission();
            return permission === 'granted';
        }
        return false;
    }
}