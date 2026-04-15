import { Component, OnInit, OnDestroy } from '@angular/core';
import { SessionManagementService } from '../../services/session-management.service';
import { AuthService } from '../../services/auth.service';
import { SessionInfo, SessionActivity } from '../../core/models/auth.models';
import { Subject, takeUntil, interval } from 'rxjs';
import { NgIf, NgClass, DatePipe } from '@angular/common';

/**
 * Session Monitor Component
 * Displays real-time session information, activity tracking,
 * and idle timeout warnings
 */
@Component({
  selector: 'app-session-monitor',
  template: `
    <div *ngIf="sessionInfo" class="session-monitor">
      <!-- Session Status Bar -->
      <div class="session-status-bar" [ngClass]="getStatusClass()">
        <div class="flex items-center justify-between p-2 text-sm">
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-1">
              <div class="w-2 h-2 rounded-full" [ngClass]="getStatusIndicator()"></div>
              <span>{{ sessionInfo.isActive ? 'Active' : 'Inactive' }}</span>
            </div>
            <span>Session: {{ formatDuration(sessionInfo.sessionDuration) }}</span>
            <span *ngIf="lastActivity">Last activity: {{ formatTimeAgo(lastActivity.timestamp) }}</span>
          </div>
          
          <div class="flex items-center space-x-2">
            <span *ngIf="tokenExpiresIn > 0" class="text-xs">
              Token expires in: {{ formatDuration(tokenExpiresIn) }}
            </span>
            <button 
              (click)="extendSession()" 
              class="px-2 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600">
              Extend
            </button>
            <button 
              (click)="toggleDetails()" 
              class="px-2 py-1 text-xs bg-gray-500 text-white rounded hover:bg-gray-600">
              {{ showDetails ? 'Hide' : 'Details' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Idle Warning -->
      <div *ngIf="showIdleWarning" class="idle-warning bg-orange-100 border-l-4 border-orange-500 p-4 mb-4">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-orange-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-orange-800">Session Timeout Warning</h3>
            <p class="text-sm text-orange-700 mt-1">
              Your session will expire in {{ formatDuration(timeUntilTimeout) }} due to inactivity.
              <button (click)="extendSession()" class="underline font-semibold ml-1">Extend session</button>
            </p>
          </div>
        </div>
      </div>

      <!-- Session Details -->
      <div *ngIf="showDetails" class="session-details bg-gray-50 p-4 rounded-lg">
        <h4 class="font-semibold mb-3">Session Details</h4>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 text-sm">
          <!-- Basic Info -->
          <div>
            <h5 class="font-medium text-gray-700 mb-2">Basic Information</h5>
            <div class="space-y-1">
              <p><span class="font-medium">Session ID:</span> {{ sessionInfo.sessionId }}</p>
              <p><span class="font-medium">User:</span> {{ sessionInfo.userEmail }}</p>
              <p><span class="font-medium">Role:</span> {{ sessionInfo.userRole }}</p>
              <p><span class="font-medium">Login Time:</span> {{ sessionInfo.loginTime | date:'medium' }}</p>
            </div>
          </div>

          <!-- Timing Info -->
          <div>
            <h5 class="font-medium text-gray-700 mb-2">Timing</h5>
            <div class="space-y-1">
              <p><span class="font-medium">Duration:</span> {{ formatDuration(sessionInfo.sessionDuration) }}</p>
              <p><span class="font-medium">Last Activity:</span> {{ sessionInfo.lastActivity | date:'medium' }}</p>
              <p><span class="font-medium">Expires:</span> {{ sessionInfo.expiryTime | date:'medium' }}</p>
              <p><span class="font-medium">Idle Time:</span> {{ formatDuration(currentIdleTime) }}</p>
            </div>
          </div>

          <!-- Device Info -->
          <div>
            <h5 class="font-medium text-gray-700 mb-2">Device Information</h5>
            <div class="space-y-1">
              <p><span class="font-medium">Browser:</span> {{ sessionInfo.deviceInfo.browserName }} {{ sessionInfo.deviceInfo.browserVersion }}</p>
              <p><span class="font-medium">Platform:</span> {{ sessionInfo.deviceInfo.platform }}</p>
              <p><span class="font-medium">Screen:</span> {{ sessionInfo.deviceInfo.screenResolution }}</p>
              <p><span class="font-medium">Mobile:</span> {{ sessionInfo.deviceInfo.isMobile ? 'Yes' : 'No' }}</p>
            </div>
          </div>
        </div>

        <!-- Recent Activity -->
        <div *ngIf="lastActivity" class="mt-4">
          <h5 class="font-medium text-gray-700 mb-2">Recent Activity</h5>
          <div class="bg-white p-3 rounded border">
            <p><span class="font-medium">Type:</span> {{ lastActivity.type }}</p>
            <p><span class="font-medium">Page:</span> {{ lastActivity.page }}</p>
            <p><span class="font-medium">Time:</span> {{ lastActivity.timestamp | date:'medium' }}</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .session-monitor {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
      transition: all 0.3s ease;
    }

    .session-status-bar.active {
      background-color: #10b981;
      color: white;
    }

    .session-status-bar.warning {
      background-color: #f59e0b;
      color: white;
    }

    .session-status-bar.inactive {
      background-color: #ef4444;
      color: white;
    }

    .idle-warning {
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.8; }
    }
  `],
  standalone: true,
  imports: [NgIf, NgClass, DatePipe]
})
export class SessionMonitorComponent implements OnInit, OnDestroy {
  
  sessionInfo: SessionInfo | null = null;
  lastActivity: SessionActivity | null = null;
  showDetails = false;
  showIdleWarning = false;
  currentIdleTime = 0;
  tokenExpiresIn = 0;
  timeUntilTimeout = 0;
  
  private destroy$ = new Subject<void>();
  private readonly IDLE_WARNING_THRESHOLD = 25 * 60 * 1000; // 25 minutes
  private readonly IDLE_TIMEOUT = 30 * 60 * 1000; // 30 minutes

  constructor(
    private sessionService: SessionManagementService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.setupSessionMonitoring();
    this.startRealTimeUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupSessionMonitoring(): void {
    // Monitor session info changes
    this.sessionService.sessionInfo$
      .pipe(takeUntil(this.destroy$))
      .subscribe(sessionInfo => {
        this.sessionInfo = sessionInfo;
        this.checkIdleStatus();
      });

    // Monitor activity changes
    this.sessionService.sessionActivity$
      .pipe(takeUntil(this.destroy$))
      .subscribe(activity => {
        this.lastActivity = activity;
        this.checkIdleStatus();
      });

    // Monitor auth state
    this.authService.loginState$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loginState => {
        if (!loginState.isLoggedIn) {
          this.sessionInfo = null;
          this.showIdleWarning = false;
        }
      });
  }

  private startRealTimeUpdates(): void {
    // Update every second for real-time display
    interval(1000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.sessionInfo) {
          this.updateRealTimeValues();
        }
      });
  }

  private updateRealTimeValues(): void {
    if (!this.sessionInfo) return;

    const now = Date.now();
    
    // Update idle time
    this.currentIdleTime = now - this.sessionInfo.lastActivity.getTime();
    
    // Update token expiry countdown
    this.tokenExpiresIn = Math.max(0, this.sessionInfo.expiryTime.getTime() - now);
    
    // Update timeout countdown
    this.timeUntilTimeout = Math.max(0, this.IDLE_TIMEOUT - this.currentIdleTime);
    
    // Update session duration
    if (this.sessionInfo.loginTime) {
      this.sessionInfo.sessionDuration = now - this.sessionInfo.loginTime.getTime();
    }
  }

  private checkIdleStatus(): void {
    if (!this.sessionInfo) return;

    const idleTime = Date.now() - this.sessionInfo.lastActivity.getTime();
    
    // Show warning if idle for more than 25 minutes
    this.showIdleWarning = idleTime >= this.IDLE_WARNING_THRESHOLD && idleTime < this.IDLE_TIMEOUT;
  }

  getStatusClass(): string {
    if (!this.sessionInfo?.isActive) return 'inactive';
    if (this.showIdleWarning) return 'warning';
    return 'active';
  }

  getStatusIndicator(): string {
    if (!this.sessionInfo?.isActive) return 'bg-red-500';
    if (this.showIdleWarning) return 'bg-yellow-500 animate-pulse';
    return 'bg-green-500';
  }

  formatDuration(ms: number): string {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes % 60}m`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }

  formatTimeAgo(date: Date): string {
    const diff = Date.now() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    
    if (minutes < 1) return 'Just now';
    if (minutes === 1) return '1 minute ago';
    if (minutes < 60) return `${minutes} minutes ago`;
    
    const hours = Math.floor(minutes / 60);
    if (hours === 1) return '1 hour ago';
    return `${hours} hours ago`;
  }

  toggleDetails(): void {
    this.showDetails = !this.showDetails;
  }

  extendSession(): void {
    this.sessionService.extendSession();
    this.showIdleWarning = false;
  }
}