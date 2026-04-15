import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class UnauthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn() && !this.authService.isTokenExpired()) {
      // User is already authenticated, redirect to dashboard
      this.router.navigate(['/dashboard']);
      return false;
    }
    
    // User is not authenticated, allow access to login/register pages
    return true;
  }
}
