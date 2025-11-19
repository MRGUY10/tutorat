// Auth Guards Exports
export { AuthGuard } from './auth.guard';
export { AdminGuard } from './admin.guard';
export { TutorGuard } from './tutor.guard';
export { StudentGuard } from './student.guard';
export { RoleBasedGuard } from './role-based.guard';
export { LoginRedirectGuard } from './login-redirect.guard';

// Guard Configuration Types
export interface GuardConfig {
  roles?: import('../models/auth.models').UserRole[];
  redirectTo?: string;
  requiresAuth?: boolean;
}

// Route Data Interface for Guards
export interface RouteGuardData {
  roles?: import('../models/auth.models').UserRole[];
  redirectTo?: string;
  allowAnonymous?: boolean;
}