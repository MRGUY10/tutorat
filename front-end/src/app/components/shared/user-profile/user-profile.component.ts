import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIf, NgClass } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../../services/auth.service';
import { CurrentUser, UserRole } from '../../../core/models/auth.models';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styles: [`
    .animate-fade-in {
      animation: fadeIn 0.5s ease-in-out;
    }
    
    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `],
  imports: [ReactiveFormsModule, NgIf, NgClass],
  standalone: true
})
export class UserProfileComponent implements OnInit, OnDestroy {
  profileForm: FormGroup;
  currentUser: CurrentUser | null = null;
  isEditing = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';
  
  // User role types for template
  userRoles = UserRole;
  
  // Destroy subject for subscriptions
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.profileForm = this.createProfileForm();
  }

  ngOnInit(): void {
    this.loadUserProfile();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createProfileForm(): FormGroup {
    return this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['']
    });
  }

  private loadUserProfile(): void {
    this.authService.currentUserInfo$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
        if (user) {
          this.profileForm.patchValue({
            nom: user.nom,
            prenom: user.prenom,
            email: user.email,
            telephone: '' // Add telephone if available in user model
          });
        }
      });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    this.clearMessages();
    
    if (!this.isEditing) {
      // Canceled editing, restore original values
      this.loadUserProfile();
    }
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isSaving = true;
    this.clearMessages();

    const formData = this.profileForm.value;
    
    // Here you would typically call a profile update API
    // For now, we'll simulate the update
    setTimeout(() => {
      this.isSaving = false;
      this.isEditing = false;
      this.successMessage = 'Profil mis à jour avec succès';
      
      // Clear success message after 3 seconds
      setTimeout(() => {
        this.successMessage = '';
      }, 3000);
    }, 1000);
  }

  changePassword(): void {
    // Navigate to change password page or open modal
    this.router.navigate(['/change-password']);
  }

  logout(): void {
    this.authService.logout();
  }

  deleteAccount(): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.')) {
      // Implement account deletion
      console.log('Account deletion requested');
    }
  }

  // Form validation helpers
  isFieldInvalid(fieldName: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field?.invalid && field?.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    
    if (field?.errors?.['required']) {
      return 'Ce champ est requis';
    }
    if (field?.errors?.['email']) {
      return 'Format email invalide';
    }
    if (field?.errors?.['minlength']) {
      return `Minimum ${field.errors['minlength'].requiredLength} caractères`;
    }
    
    return '';
  }

  private markFormGroupTouched(): void {
    Object.keys(this.profileForm.controls).forEach(key => {
      this.profileForm.get(key)?.markAsTouched();
    });
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Role-specific display methods
  getRoleDisplayName(): string {
    if (!this.currentUser) return '';
    
    switch (this.currentUser.role) {
      case UserRole.ADMIN:
        return 'Administrateur';
      case UserRole.TUTOR:
        return 'Tuteur';
      case UserRole.STUDENT:
        return 'Étudiant';
      default:
        return 'Utilisateur';
    }
  }

  getRoleIcon(): string {
    if (!this.currentUser) return '👤';
    
    switch (this.currentUser.role) {
      case UserRole.ADMIN:
        return '👑';
      case UserRole.TUTOR:
        return '👨‍🏫';
      case UserRole.STUDENT:
        return '👨‍🎓';
      default:
        return '👤';
    }
  }

  getRoleBadgeClass(): string {
    if (!this.currentUser) return 'bg-gray-100 text-gray-800';
    
    switch (this.currentUser.role) {
      case UserRole.ADMIN:
        return 'bg-red-100 text-red-800';
      case UserRole.TUTOR:
        return 'bg-green-100 text-green-800';
      case UserRole.STUDENT:
        return 'bg-blue-100 text-blue-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }
}