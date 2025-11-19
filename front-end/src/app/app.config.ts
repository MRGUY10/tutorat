import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors, HTTP_INTERCEPTORS } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { SessionRefreshInterceptor, EnhancedAuthInterceptor } from './core/interceptors/session-refresh.interceptor';
import { SessionManagementService } from './services/session-management.service';
import { SessionSecurityService } from './services/session-security.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(routes),
    provideHttpClient(),
    // Authentication interceptors (order matters)
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: SessionRefreshInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: EnhancedAuthInterceptor,
      multi: true
    },
    // Session management services
    SessionManagementService,
    SessionSecurityService
  ]
};
