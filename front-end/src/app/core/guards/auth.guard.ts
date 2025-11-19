import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> {
    
    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      console.log('Authentication required, redirecting to login');
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // Check if user has USER role - not allowed
    const userRole = this.authService.getUserRole();
    if (userRole === 'USER' as any) {
      console.log('USER role not allowed, logging out');
      this.authService.logout();
      this.router.navigate(['/login']);
      return false;
    }

    // Check token expiry
    if (this.authService.isTokenExpired()) {
      console.log('Token expired, attempting refresh...');
      
      return this.authService.refreshToken().pipe(
        map(() => {
          return this.checkRoleAccess(route, state);
        }),
        tap(hasAccess => {
          if (!hasAccess) {
            this.handleAccessDenied(route, state);
          }
        })
      );
    }

    // Check role-based access
    return this.checkRoleAccess(route, state);
  }

  private checkRoleAccess(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // Get required roles from route data
    const requiredRoles = route.data['roles'] as UserRole[];
    
    if (!requiredRoles || requiredRoles.length === 0) {
      // No specific roles required, just need to be authenticated
      return true;
    }

    // Check if user has any of the required roles
    if (this.authService.canAccess(requiredRoles)) {
      return true;
    }

    // Access denied
    console.warn('Access denied: insufficient permissions', {
      userRole: this.authService.getUserRole(),
      requiredRoles: requiredRoles,
      route: state.url
    });
    
    return false;
  }

  private handleAccessDenied(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): void {
    const userRole = this.authService.getUserRole();
    
    // Redirect based on user role
    switch (userRole) {
      case UserRole.ADMIN:
        this.router.navigate(['/admin']);
        break;
      case UserRole.TUTOR:
        this.router.navigate(['/tutor-dashboard']);
        break;
      case UserRole.STUDENT:
        this.router.navigate(['/student-dashboard']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }
}
