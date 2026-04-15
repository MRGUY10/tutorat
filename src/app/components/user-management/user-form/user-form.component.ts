import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { UserManagementService } from '../../../services/user-management.service';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit, OnDestroy {
  adminForm!: FormGroup;
  isLoading = false;
  showSuccessMessage = false;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private userManagementService: UserManagementService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm(): void {
    this.adminForm = this.fb.group({
      prenom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      motDePasse: ['', [Validators.required, this.passwordStrengthValidator]],
      confirmPassword: ['', [Validators.required]],
      permissions: ['READ_WRITE_ALL', [Validators.required]],
      departement: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  private passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) {
      return null;
    }

    const hasUpperCase = /[A-Z]+/.test(value);
    const hasLowerCase = /[a-z]+/.test(value);
    const hasNumeric = /[0-9]+/.test(value);
    const hasSpecialChar = /[\W_]+/.test(value);
    const minLength = value.length >= 8;

    const passwordValid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecialChar && minLength;

    if (!passwordValid) {
      return { passwordStrength: true };
    }

    return null;
  }

  private passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('motDePasse');
    const confirmPassword = group.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.adminForm.valid && !this.isLoading) {
      this.isLoading = true;
      
      const adminData = {
        prenom: this.adminForm.value.prenom,
        nom: this.adminForm.value.nom,
        email: this.adminForm.value.email,
        telephone: this.adminForm.value.telephone,
        motDePasse: this.adminForm.value.motDePasse,
        permissions: this.adminForm.value.permissions,
        departement: this.adminForm.value.departement
      };

      this.userManagementService.registerAdmin(adminData)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.isLoading = false;
            this.showSuccessMessage = true;
            
            this.adminForm.reset();
            this.adminForm.get('permissions')?.setValue('READ_WRITE_ALL');
            
            setTimeout(() => {
              this.showSuccessMessage = false;
              this.router.navigate(['/user-management']);
            }, 3000);
          },
          error: (error) => {
            this.isLoading = false;
            console.error('Error creating admin:', error);
          }
        });
    } else {
      this.markFormGroupTouched(this.adminForm);
    }
  }

  onCancel(): void {
    this.router.navigate(['/user-management']);
  }

  getControl(controlName: string): AbstractControl | null {
    return this.adminForm.get(controlName);
  }

  hasError(controlName: string, errorType: string): boolean {
    const control = this.getControl(controlName);
    return !!(control && control.hasError(errorType) && (control.dirty || control.touched));
  }

  getErrorMessage(controlName: string): string {
    const control = this.getControl(controlName);
    if (!control || !control.errors) {
      return '';
    }

    const errors = control.errors;

    switch (controlName) {
      case 'prenom':
      case 'nom':
        if (errors['required']) return `${controlName === 'prenom' ? 'First name' : 'Last name'} is required`;
        if (errors['minlength']) return `${controlName === 'prenom' ? 'First name' : 'Last name'} must be at least 2 characters`;
        if (errors['maxlength']) return `${controlName === 'prenom' ? 'First name' : 'Last name'} must not exceed 50 characters`;
        break;
      case 'email':
        if (errors['required']) return 'Email is required';
        if (errors['email']) return 'Please enter a valid email address';
        break;
      case 'telephone':
        if (errors['required']) return 'Phone number is required';
        if (errors['pattern']) return 'Please enter a valid 10-digit phone number';
        break;
      case 'motDePasse':
        if (errors['required']) return 'Password is required';
        if (errors['passwordStrength']) return 'Password must contain uppercase, lowercase, number, and special character (min 8 chars)';
        break;
      case 'confirmPassword':
        if (errors['required']) return 'Please confirm your password';
        break;
      case 'departement':
        if (errors['required']) return 'Department is required';
        if (errors['minlength']) return 'Department must be at least 2 characters';
        if (errors['maxlength']) return 'Department must not exceed 100 characters';
        break;
      case 'permissions':
        if (errors['required']) return 'Permissions level is required';
        break;
    }

    return 'Invalid input';
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(field => {
      const control = formGroup.get(field);
      control?.markAsTouched({ onlySelf: true });

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}