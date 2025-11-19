import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserProfileService } from '../../services/user-profile.service';
import { AuthService } from '../../services/auth.service';
import { MatiereService } from '../../services/matiere.service';
import {
  UserProfile,
  StudentProfile,
  TutorProfile,
  StudentProfileUpdateRequest,
  TutorProfileUpdateRequest,
  PasswordUpdateRequest
} from '../../models/user-profile.model';
import { Matiere } from '../../models/matiere.model';
import { DEFAULT_AVATAR } from '../../shared/constants/default-avatar';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | StudentProfile | TutorProfile | null = null;
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  
  isLoading = false;
  isEditing = false;
  isChangingPassword = false;
  isSaving = false;
  
  userType: 'student' | 'tutor' | 'admin' | null = null;
  currentUserId: number | null = null;
  
  successMessage: string | null = null;
  errorMessage: string | null = null;
  
  // For tutors
  matieres: Matiere[] = [];
  selectedMatieres: number[] = [];
  
  // For photo upload
  selectedFile: File | null = null;
  photoPreview: string | null = null;
  
  // Default avatar constant
  readonly DEFAULT_AVATAR = DEFAULT_AVATAR;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private userProfileService: UserProfileService,
    private authService: AuthService,
    private matiereService: MatiereService
  ) {}

  ngOnInit(): void {
    this.initializeForms();
    this.loadUserProfile();
  }

  initializeForms(): void {
    // Basic profile form
    this.profileForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      telephone: [''],
      email: [{ value: '', disabled: true }] // Email is readonly
    });

    // Password change form
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  loadUserProfile(): void {
    this.isLoading = true;
    
    // Get current user info from AuthService
    const user = this.authService.getCurrentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.currentUserId = user.id;
    this.userType = user.userType.toLowerCase() as 'student' | 'tutor' | 'admin';

    // Load profile based on user type
    if (this.userType === 'tutor') {
      this.loadTutorProfile(this.currentUserId);
    } else if (this.userType === 'student') {
      this.loadStudentProfile(this.currentUserId);
    } else {
      this.loadBasicProfile(this.currentUserId);
    }
  }

  loadBasicProfile(id: number): void {
    this.userProfileService.getUserById(id).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.populateForm(profile);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.errorMessage = 'Impossible de charger le profil';
        this.isLoading = false;
      }
    });
  }

  loadStudentProfile(id: number): void {
    this.userProfileService.getStudentProfile(id).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.populateForm(profile);
        this.populateStudentFields(profile);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading student profile:', error);
        this.errorMessage = 'Impossible de charger le profil étudiant';
        this.isLoading = false;
      }
    });
  }

  loadTutorProfile(id: number): void {
    // Load matieres first for tutor
    this.matiereService.getAllMatieres().subscribe({
      next: (matieres) => {
        this.matieres = matieres;
      },
      error: (error) => {
        console.error('Error loading matieres:', error);
      }
    });

    this.userProfileService.getTutorProfile(id).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.populateForm(profile);
        this.populateTutorFields(profile);
        this.selectedMatieres = profile.specialites.map(s => s.matiereId);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tutor profile:', error);
        this.errorMessage = 'Impossible de charger le profil tuteur';
        this.isLoading = false;
      }
    });
  }

  populateForm(profile: UserProfile): void {
    this.profileForm.patchValue({
      nom: profile.nom,
      prenom: profile.prenom,
      telephone: profile.telephone || '',
      email: profile.email
    });
  }

  populateStudentFields(profile: StudentProfile): void {
    this.profileForm.addControl('filiere', this.fb.control(profile.filiere, Validators.required));
    this.profileForm.addControl('annee', this.fb.control(profile.annee, Validators.required));
    this.profileForm.addControl('niveau', this.fb.control(profile.niveau, Validators.required));
  }

  populateTutorFields(profile: TutorProfile): void {
    this.profileForm.addControl('experience', this.fb.control(profile.experience));
    this.profileForm.addControl('tarifHoraire', this.fb.control(profile.tarifHoraire, [Validators.required, Validators.min(5)]));
    this.profileForm.addControl('diplomes', this.fb.control(profile.diplomes));
    this.profileForm.addControl('description', this.fb.control(profile.description));
    this.profileForm.addControl('ville', this.fb.control(profile.ville));
    this.profileForm.addControl('pays', this.fb.control(profile.pays));
    this.profileForm.addControl('coursEnLigne', this.fb.control(profile.coursEnLigne));
    this.profileForm.addControl('coursPresentiel', this.fb.control(profile.coursPresentiel));
    this.profileForm.addControl('disponible', this.fb.control(profile.disponible));
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      // Cancelled editing, restore original values
      if (this.profile) {
        this.populateForm(this.profile);
      }
    }
  }

  saveProfile(): void {
    if (this.profileForm.invalid || !this.currentUserId) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = null;
    this.successMessage = null;

    if (this.userType === 'tutor') {
      this.saveTutorProfile();
    } else if (this.userType === 'student') {
      this.saveStudentProfile();
    }
  }

  saveStudentProfile(): void {
    const formValue = this.profileForm.getRawValue();
    const updateRequest: StudentProfileUpdateRequest = {
      nom: formValue.nom,
      prenom: formValue.prenom,
      telephone: formValue.telephone,
      filiere: formValue.filiere,
      annee: parseInt(formValue.annee),
      niveau: formValue.niveau
    };

    this.userProfileService.updateStudentProfile(this.currentUserId!, updateRequest).subscribe({
      next: (updatedProfile) => {
        this.profile = updatedProfile;
        this.isEditing = false;
        this.isSaving = false;
        this.successMessage = 'Profil mis à jour avec succès!';
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.errorMessage = 'Erreur lors de la mise à jour du profil';
        this.isSaving = false;
      }
    });
  }

  saveTutorProfile(): void {
    const formValue = this.profileForm.getRawValue();
    const updateRequest: TutorProfileUpdateRequest = {
      nom: formValue.nom,
      prenom: formValue.prenom,
      telephone: formValue.telephone,
      experience: formValue.experience,
      tarifHoraire: parseFloat(formValue.tarifHoraire),
      diplomes: formValue.diplomes,
      description: formValue.description,
      ville: formValue.ville,
      pays: formValue.pays,
      coursEnLigne: formValue.coursEnLigne,
      coursPresentiel: formValue.coursPresentiel,
      disponible: formValue.disponible,
      specialiteIds: this.selectedMatieres
    };

    this.userProfileService.updateTutorProfile(this.currentUserId!, updateRequest).subscribe({
      next: (updatedProfile) => {
        this.profile = updatedProfile;
        this.isEditing = false;
        this.isSaving = false;
        this.successMessage = 'Profil mis à jour avec succès!';
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        this.errorMessage = 'Erreur lors de la mise à jour du profil';
        this.isSaving = false;
      }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid || !this.currentUserId) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request: PasswordUpdateRequest = this.passwordForm.value;

    this.userProfileService.updatePassword(this.currentUserId, request).subscribe({
      next: (response) => {
        this.isChangingPassword = false;
        this.isSaving = false;
        this.passwordForm.reset();
        this.successMessage = 'Mot de passe modifié avec succès!';
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (error) => {
        console.error('Error changing password:', error);
        this.errorMessage = error.error?.message || 'Erreur lors du changement de mot de passe';
        this.isSaving = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      
      // Preview image
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.photoPreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  uploadPhoto(): void {
    if (!this.selectedFile || !this.currentUserId) {
      return;
    }

    this.isSaving = true;
    this.userProfileService.uploadProfilePhoto(this.currentUserId, this.selectedFile).subscribe({
      next: (response) => {
        if (this.profile) {
          this.profile.photo = response.photoUrl;
        }
        this.selectedFile = null;
        this.photoPreview = null;
        this.isSaving = false;
        this.successMessage = 'Photo de profil mise à jour!';
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (error) => {
        console.error('Error uploading photo:', error);
        this.errorMessage = 'Erreur lors du téléchargement de la photo';
        this.isSaving = false;
      }
    });
  }

  toggleMatiereSelection(matiereId: number): void {
    const index = this.selectedMatieres.indexOf(matiereId);
    if (index > -1) {
      this.selectedMatieres.splice(index, 1);
    } else {
      this.selectedMatieres.push(matiereId);
    }
  }

  isMatiereSelected(matiereId: number): boolean {
    return this.selectedMatieres.includes(matiereId);
  }

  isTutorProfile(profile: any): profile is TutorProfile {
    return this.userType === 'tutor' && 'tarifHoraire' in profile;
  }

  isStudentProfile(profile: any): profile is StudentProfile {
    return this.userType === 'student' && 'filiere' in profile;
  }
}
