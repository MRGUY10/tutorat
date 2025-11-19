import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../models/auth.models';

/**
 * Guard that allows access only to Tutor users
 */
@Injectable({
  providedIn: 'root'
})
export class TutorGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    if (this.authService.isTutor()) {
      return true;
    }

    // Not a tutor, redirect to appropriate dashboard
    console.warn('Tutor access required, redirecting to user dashboard');
    this.router.navigate([this.authService.getDefaultRoute()]);
    return false;
  }
}