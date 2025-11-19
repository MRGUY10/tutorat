import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../models/auth.models';

/**
 * Flexible role-based guard that can be configured via route data
 * Usage in routes:
 * {
 *   path: 'admin',
 *   component: AdminComponent,
 *   canActivate: [RoleBasedGuard],
 *   data: { 
 *     roles: [UserRole.ADMIN],
 *     redirectTo: '/dashboard' // optional custom redirect
 *   }
 * }
 */
@Injectable({
  providedIn: 'root'
})
export class RoleBasedGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | Observable<boolean> {
    
    // Check authentication
    if (!this.authService.isAuthenticated()) {
      console.log('Authentication required for role-protected route');
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // Get required roles from route data
    const requiredRoles = route.data['roles'] as UserRole[];
    const customRedirect = route.data['redirectTo'] as string;
    
    if (!requiredRoles || requiredRoles.length === 0) {
      console.warn('RoleBasedGuard: No roles specified in route data');
      return true;
    }

    // Check if user has required role
    if (this.authService.hasAnyRole(requiredRoles)) {
      return true;
    }

    // Access denied - redirect appropriately
    console.warn('Access denied for role-protected route', {
      userRole: this.authService.getUserRole(),
      requiredRoles: requiredRoles,
      route: state.url
    });

    // Use custom redirect or default route
    const redirectRoute = customRedirect || this.authService.getDefaultRoute();
    this.router.navigate([redirectRoute]);
    
    return false;
  }
}