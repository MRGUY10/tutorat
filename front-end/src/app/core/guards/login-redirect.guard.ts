import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/**
 * Guard that prevents authenticated users from accessing login/register pages
 * Redirects them to their appropriate dashboard
 */
@Injectable({
  providedIn: 'root'
})
export class LoginRedirectGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    
    if (this.authService.isAuthenticated() && !this.authService.isTokenExpired()) {
      // User is already authenticated, redirect to their dashboard
      console.log('User already authenticated, redirecting to dashboard');
      const defaultRoute = this.authService.getDefaultRoute();
      this.router.navigate([defaultRoute]);
      return false;
    }

    // User not authenticated, allow access to login/register
    return true;
  }
}