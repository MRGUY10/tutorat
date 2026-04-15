import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, filter, take, switchMap, catchError } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { SessionManagementService } from '../../services/session-management.service';

/**
 * Session Refresh Interceptor
 * Automatically handles token refresh when API calls fail due to expired tokens
 */
@Injectable()
export class SessionRefreshInterceptor implements HttpInterceptor {
    
    private isRefreshing = false;
    private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);
    
    constructor(
        private authService: AuthService,
        private sessionService: SessionManagementService
    ) {}
    
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Add auth token to request if available
        const authToken = this.authService.getToken();
        if (authToken && !this.isAuthEndpoint(request.url)) {
            request = this.addTokenToRequest(request, authToken);
        }
        
        return next.handle(request).pipe(
            catchError((error: HttpErrorResponse) => {
                // Handle 401 Unauthorized errors
                if (error.status === 401 && !this.isAuthEndpoint(request.url)) {
                    return this.handle401Error(request, next);
                }
                
                // Handle other errors
                return throwError(() => error);
            })
        );
    }
    
    /**
     * Handle 401 Unauthorized errors by attempting token refresh
     */
    private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);
            
            const refreshToken = this.authService.getRefreshToken();
            
            if (refreshToken) {
                return this.authService.refreshToken().pipe(
                    switchMap((tokenResponse: any) => {
                        this.isRefreshing = false;
                        this.refreshTokenSubject.next(tokenResponse.token);
                        
                        // Retry the original request with new token
                        const retryRequest = this.addTokenToRequest(request, tokenResponse.token);
                        return next.handle(retryRequest);
                    }),
                    catchError((error) => {
                        this.isRefreshing = false;
                        
                        // If refresh fails, logout user
                        console.error('Token refresh failed, logging out user');
                        this.authService.logout();
                        
                        return throwError(() => error);
                    })
                );
            } else {
                // No refresh token, logout user
                this.isRefreshing = false;
                this.authService.logout();
                return throwError(() => new Error('No refresh token available'));
            }
        } else {
            // Refresh is already in progress, wait for it
            return this.refreshTokenSubject.pipe(
                filter(token => token !== null),
                take(1),
                switchMap(token => {
                    const retryRequest = this.addTokenToRequest(request, token);
                    return next.handle(retryRequest);
                })
            );
        }
    }
    
    /**
     * Add authorization token to request
     */
    private addTokenToRequest(request: HttpRequest<any>, token: string): HttpRequest<any> {
        return request.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
    }
    
    /**
     * Check if the request is to an auth endpoint (no token needed)
     */
    private isAuthEndpoint(url: string): boolean {
        return url.includes('/auth/login') || 
               url.includes('/auth/refresh') || 
               url.includes('/auth/register') ||
               url.includes('/auth/check-email');
    }
}

/**
 * Enhanced Auth Interceptor with Session Management
 * Handles token injection, automatic refresh, and session tracking
 */
@Injectable()
export class EnhancedAuthInterceptor implements HttpInterceptor {
    
    constructor(
        private authService: AuthService,
        private sessionService: SessionManagementService
    ) {}
    
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Record activity for session management
        if (this.sessionService.isSessionActive()) {
            this.sessionService.extendSession();
        }
        
        // Skip token injection for auth endpoints
        if (this.isAuthEndpoint(request.url)) {
            return next.handle(request);
        }
        
        // Add auth token if available
        const token = this.authService.getToken();
        if (token) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`,
                    'X-Session-ID': this.sessionService.getCurrentSession()?.sessionId || 'unknown'
                }
            });
        }
        
        return next.handle(request).pipe(
            catchError((error: HttpErrorResponse) => {
                // Log API errors for session tracking
                console.error(`API Error [${error.status}]:`, error.message);
                
                // Handle specific error codes
                if (error.status === 403) {
                    console.warn('Access forbidden - insufficient permissions');
                } else if (error.status === 429) {
                    console.warn('Rate limit exceeded - too many requests');
                }
                
                return throwError(() => error);
            })
        );
    }
    
    private isAuthEndpoint(url: string): boolean {
        const authEndpoints = [
            '/auth/login',
            '/auth/refresh',
            '/auth/logout',
            '/auth/register',
            '/auth/check-email'
        ];
        
        return authEndpoints.some(endpoint => url.includes(endpoint));
    }
}

/**
 * Request Timing Interceptor for Performance Monitoring
 */
@Injectable()
export class RequestTimingInterceptor implements HttpInterceptor {
    
    constructor(private sessionService: SessionManagementService) {}
    
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const startTime = Date.now();
        
        return next.handle(request).pipe(
            catchError((error: HttpErrorResponse) => {
                const duration = Date.now() - startTime;
                
                // Log slow or failed requests
                if (duration > 5000 || error.status >= 500) {
                    console.warn(`Slow/Failed request to ${request.url}:`, {
                        duration: duration + 'ms',
                        status: error.status,
                        method: request.method
                    });
                }
                
                return throwError(() => error);
            })
        );
    }
}