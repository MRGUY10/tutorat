import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  
  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip authentication for login and other auth endpoints
    if (this.isAuthEndpoint(req.url)) {
      return next.handle(req);
    }

    // Add authorization header if token exists
    const token = this.authService.accessToken;
    if (token) {
      req = this.addAuthHeader(req, token);
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle token expiration
        if (error.status === 401 && !this.isAuthEndpoint(req.url)) {
          return this.handleUnauthorized(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addAuthHeader(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private isAuthEndpoint(url: string): boolean {
    return url.includes('/auth/login') || 
           url.includes('/auth/refresh') || 
           url.includes('/auth/logout') ||
           url.includes('/auth/validate');
  }

  private handleUnauthorized(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Try to refresh token
    const refreshToken = this.authService.refreshTokenValue;
    
    if (refreshToken && !this.authService.isTokenExpired()) {
      return this.authService.refreshToken().pipe(
        switchMap((newTokens) => {
          // Retry original request with new token
          const newReq = this.addAuthHeader(req, newTokens.accessToken);
          return next.handle(newReq);
        }),
        catchError((refreshError) => {
          // Refresh failed, redirect to login
          this.authService.logout();
          this.router.navigate(['/login']);
          return throwError(() => refreshError);
        })
      );
    } else {
      // No refresh token or token is expired, redirect to login
      this.authService.logout();
      this.router.navigate(['/login']);
      return throwError(() => new Error('Authentication required'));
    }
  }
}
