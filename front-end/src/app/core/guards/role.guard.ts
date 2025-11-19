import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // Get roles from localStorage (or your auth service)
    const auth = JSON.parse(localStorage.getItem('auth_tokens') || '{}');
    const roles: string[] = auth.userInfo?.roles || [];
    const requiredRoles: string[] = next.data['roles'] || [];

    // If no roles required, allow access
    if (!requiredRoles.length) return true;

    // If user has at least one required role, allow access
    if (roles.some(role => requiredRoles.includes(role))) {
      return true;
    }

    // Otherwise, redirect to access denied page
    this.router.navigate(['/access-denied']);
    return false;
  }
}
