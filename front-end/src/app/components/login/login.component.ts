import { Component, OnInit, OnDestroy } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from "@angular/forms";
import { NgIf } from "@angular/common";
import { HttpErrorResponse } from "@angular/common/http";
import { Subject, takeUntil } from "rxjs";
import { UserRole, ApiError } from "../../core/models/auth.models";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  imports: [FormsModule, ReactiveFormsModule, NgIf],
  standalone: true,
})
export class LoginComponent implements OnInit, OnDestroy {
  // Form and validation
  loginForm: FormGroup;
  isLoading = false;
  
  // Error handling
  errorMessage: string = "";
  fieldErrors: { [key: string]: string } = {};
  
  // Navigation
  returnUrl: string = '';
  
  // User role info for UI
  userRoles = UserRole;
  
  // Destroy subject for subscriptions
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService, 
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.loginForm = this.createLoginForm();
  }

  ngOnInit(): void {
    // Check if user is already authenticated
    if (this.authService.isAuthenticated()) {
      this.redirectToDefaultRoute();
      return;
    }

    // Get return URL from query parameters
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';
    
    // Clear any existing auth data
    this.authService.logout();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createLoginForm(): FormGroup {
    return this.fb.group({
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.minLength(3)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(3)
      ]],
      rememberMe: [false]
    });
  }

  // ================================================
  // LOGIN FUNCTIONALITY
  // ================================================

  login(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.clearErrors();

    const { email, password } = this.loginForm.value;

    this.authService.login(email.trim(), password)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Login successful:', response);
          this.handleLoginSuccess(response);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Login error:', error);
          this.handleLoginError(error);
          this.isLoading = false;
        }
      });
  }

  private validateForm(): boolean {
    this.clearErrors();
    
    if (this.loginForm.invalid) {
      this.markFormGroupTouched();
      this.setFieldErrors();
      return false;
    }
    
    return true;
  }

  private handleLoginSuccess(response: any): void {
    this.isLoading = false;
    
    // Get user role for conditional navigation
    const userRole = this.authService.getUserRole();
    console.log('User role after login:', userRole);

    // Check if user has USER role (not allowed to login)
    if (response.role === 'USER' || userRole === 'USER' as any) {
      this.errorMessage = "Accès refusé. Les utilisateurs avec le rôle USER ne peuvent pas se connecter.";
      this.authService.logout();
      this.isLoading = false;
      return;
    }

    // Navigate based on role or return URL
    if (this.returnUrl) {
      this.router.navigate([this.returnUrl]);
    } else {
      this.redirectBasedOnRole(userRole);
    }
  }

  private handleLoginError(error: HttpErrorResponse): void {
    const apiError = error.error as ApiError;
    
    switch (error.status) {
      case 401:
        this.errorMessage = "Identifiants invalides. Vérifiez votre email et mot de passe.";
        break;
      case 400:
        this.errorMessage = apiError?.message || "Données de connexion invalides.";
        break;
      case 403:
        this.errorMessage = "Accès refusé. Votre compte pourrait être désactivé.";
        break;
      case 0:
        this.errorMessage = "Impossible de se connecter au serveur. Vérifiez votre connexion.";
        break;
      case 500:
      case 502:
      case 503:
        this.errorMessage = "Erreur du serveur. Veuillez réessayer plus tard.";
        break;
      default:
        this.errorMessage = apiError?.message || 'Erreur de connexion inconnue';
    }
  }

  private redirectBasedOnRole(role: UserRole | null): void {
    switch (role) {
      case UserRole.ADMIN:
        this.router.navigate(['/tutor-verification']);
        break;
      case UserRole.TUTOR:
        this.router.navigate(['/dashboard']);
        break;
      case UserRole.STUDENT:
        this.router.navigate(['/dashboard']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }

  private redirectToDefaultRoute(): void {
    const defaultRoute = this.authService.getDefaultRoute();
    this.router.navigate([defaultRoute]);
  }

  // ================================================
  // FORM VALIDATION HELPERS
  // ================================================

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      this.loginForm.get(key)?.markAsTouched();
    });
  }

  private setFieldErrors(): void {
    this.fieldErrors = {};
    
    const emailControl = this.loginForm.get('email');
    if (emailControl?.invalid && emailControl?.touched) {
      if (emailControl.errors?.['required']) {
        this.fieldErrors['email'] = 'Email est requis';
      } else if (emailControl.errors?.['email']) {
        this.fieldErrors['email'] = 'Format email invalide';
      } else if (emailControl.errors?.['minlength']) {
        this.fieldErrors['email'] = 'Email trop court (minimum 3 caractères)';
      }
    }

    const passwordControl = this.loginForm.get('password');
    if (passwordControl?.invalid && passwordControl?.touched) {
      if (passwordControl.errors?.['required']) {
        this.fieldErrors['password'] = 'Mot de passe est requis';
      } else if (passwordControl.errors?.['minlength']) {
        this.fieldErrors['password'] = 'Mot de passe trop court (minimum 3 caractères)';
      }
    }
  }

  private clearErrors(): void {
    this.errorMessage = '';
    this.fieldErrors = {};
  }

  // ================================================
  // UI HELPERS
  // ================================================

  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }

  getFieldError(fieldName: string): string {
    return this.fieldErrors[fieldName] || '';
  }

  hasFieldError(fieldName: string): boolean {
    return !!this.fieldErrors[fieldName];
  }

  // ================================================
  // NAVIGATION HELPERS
  // ================================================

  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }

  // ================================================
  // QUICK LOGIN HELPERS (for development/testing)
  // ================================================

  quickLoginAs(role: UserRole): void {
    let email = '';
    let password = 'password123';

    switch (role) {
      case UserRole.ADMIN:
        email = 'admin@example.com';
        break;
      case UserRole.TUTOR:
        email = 'tutor@example.com';
        break;
      case UserRole.STUDENT:
        email = 'student@example.com';
        break;
    }

    this.loginForm.patchValue({ email, password });
    this.login();
  }
}
