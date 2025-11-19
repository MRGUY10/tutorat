import {Component, HostListener, OnInit, OnDestroy, signal} from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import {NgClass, NgIf} from "@angular/common";
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { CurrentUser, UserRole } from '../../core/models/auth.models';

// Import our new shared components
import { HeaderComponent } from '../../components/shared/header/header.component';
import { SidebarComponent } from '../../components/shared/sidebar/sidebar.component';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, HeaderComponent, SidebarComponent],
  templateUrl: './main-layout.component.html',
  standalone: true,
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  sidebarCollapsed = signal(false); // Start expanded
  isMobile = false;
  user: CurrentUser | null = null;
  currentPageTitle = 'Dashboard';
  
  // User role types for template
  userRoles = UserRole;
  
  // Destroy subject for subscriptions
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Check authentication status
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadUserProfile();
    this.checkScreenSize();
    this.setupAutoTokenRefresh();
    
    // On mobile, start collapsed
    if (this.isMobile) {
      this.sidebarCollapsed.set(true);
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUserProfile() {
    // Subscribe to current user info
    this.authService.currentUserInfo$
      .pipe(takeUntil(this.destroy$))
      .subscribe(currentUser => {
        console.log('Current user data:', currentUser);
        this.user = currentUser;
        
        if (currentUser) {
          this.updatePageTitle(currentUser.role);
        }
      });
    
    // Also listen to login state changes
    this.authService.loginState$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loginState => {
        if (!loginState.isLoggedIn) {
          this.router.navigate(['/login']);
        }
      });
  }

  private setupAutoTokenRefresh() {
    // Check token expiry every 30 seconds
    setInterval(() => {
      this.authService.checkTokenExpiry();
    }, 30000);
  }

  private updatePageTitle(role: UserRole) {
    switch (role) {
      case UserRole.ADMIN:
        this.currentPageTitle = 'Administration';
        break;
      case UserRole.TUTOR:
        this.currentPageTitle = 'Espace Tuteur';
        break;
      case UserRole.STUDENT:
        this.currentPageTitle = 'Espace Étudiant';
        break;
      default:
        this.currentPageTitle = 'Dashboard';
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    const previousIsMobile = this.isMobile;
    this.checkScreenSize();

    // If switching from mobile to desktop, show sidebar
    if (previousIsMobile && !this.isMobile) {
      this.sidebarCollapsed.set(false);
    }
    // If switching from desktop to mobile, hide sidebar
    else if (!previousIsMobile && this.isMobile) {
      this.sidebarCollapsed.set(true);
    }
  }

  private checkScreenSize() {
    this.isMobile = window.innerWidth < 1024; // lg breakpoint
  }

  toggleSidebar() {
    this.sidebarCollapsed.update(value => !value);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // Testing method to switch user roles
  switchUserRole(role: UserRole) {
    if (this.user) {
      this.user = {
        ...this.user,
        role: role
      };
      this.updatePageTitle(role);
      console.log('Switched to role:', role);
    }
  }

  // Get user display name
  getUserDisplayName(): string {
    if (!this.user) return '';
    return `${this.user.prenom} ${this.user.nom}`.trim() || this.user.email;
  }

  // Get user role display
  getUserRoleDisplay(): string {
    if (!this.user) return '';
    
    switch (this.user.role) {
      case UserRole.ADMIN:
        return 'Administrateur';
      case UserRole.TUTOR:
        return 'Tuteur';
      case UserRole.STUDENT:
        return 'Étudiant';
      default:
        return 'Utilisateur';
    }
  }

  // Check if user has specific role
  hasRole(role: UserRole): boolean {
    return this.user?.role === role;
  }

  // Navigation based on role
  navigateToProfile(): void {
    if (!this.user) return;
    
    switch (this.user.role) {
      case UserRole.ADMIN:
        this.router.navigate(['/admin/profile']);
        break;
      case UserRole.TUTOR:
        this.router.navigate(['/tutor/profile']);
        break;
      case UserRole.STUDENT:
        this.router.navigate(['/student/profile']);
        break;
      default:
        this.router.navigate(['/profile']);
    }
  }
}
