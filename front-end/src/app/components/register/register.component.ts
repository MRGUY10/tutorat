import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatiereService } from '../../services/matiere.service';
import { StudentRegisterRequest, TutorRegisterRequest, NiveauAcademique } from '../../core/models/auth.models';
import { Matiere } from '../../models/matiere.model';

@Component({
  selector: 'app-register',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
  studentForm!: FormGroup;
  tutorForm!: FormGroup;
  
  isLoading = false;
  userType: 'student' | 'tutor' | null = null;
  
  // Multi-step form state
  currentStep = 1;
  totalSteps = 3; // For students: 3 steps, For tutors: 4 steps
  
  niveauAcademique = Object.values(NiveauAcademique);
  
  // Matiere data
  matieres: Matiere[] = [];
  filteredMatieres: Matiere[] = [];
  selectedMatieres: number[] = [];
  matieresLoading = false;
  matieresError: string | null = null;
  
  // Searchable dropdown state
  matiereSearchTerm = '';
  showMatiereDropdown = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private matiereService: MatiereService,
    private router: Router
  ) {}
  
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInside = target.closest('.matiere-dropdown-container');
    if (!clickedInside) {
      this.showMatiereDropdown = false;
    }
  }

  ngOnInit(): void {
    this.initializeForms();
    this.loadMatieres();
  }
  
  loadMatieres(): void {
    this.matieresLoading = true;
    this.matieresError = null;
    
    this.matiereService.getAllMatieres().subscribe({
      next: (matieres) => {
        this.matieres = matieres;
        this.filteredMatieres = matieres;
        this.matieresLoading = false;
        console.log('Matieres loaded:', this.matieres.length, 'subjects');
      },
      error: (error) => {
        this.matieresLoading = false;
        this.matieresError = 'Impossible de charger les matières. Veuillez réessayer.';
        console.error('Error loading matieres:', error);
      }
    });
  }

  initializeForms(): void {
    // Password pattern: at least one lowercase, one uppercase, and one digit
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;
    
    this.studentForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [
        Validators.required, 
        Validators.minLength(8),
        Validators.pattern(passwordPattern)
      ]],
      telephone: [''],
      filiere: ['', Validators.required],
      annee: ['', [Validators.required, Validators.min(1), Validators.max(5)]],
      niveau: ['', Validators.required]
    });

    this.tutorForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [
        Validators.required, 
        Validators.minLength(8),
        Validators.pattern(passwordPattern)
      ]],
      telephone: [''],
      experience: [''],
      tarifHoraire: ['', [Validators.required, Validators.min(5), Validators.max(999.99)]],
      diplomes: [''],
      description: [''],
      ville: [''],
      pays: [''],
      coursEnLigne: [true],
      coursPresentiel: [false]
    });
  }

  selectUserType(type: 'student' | 'tutor'): void {
    this.userType = type;
    this.currentStep = 1;
    this.totalSteps = type === 'student' ? 3 : 4;
  }

  goBackToSelection(): void {
    this.userType = null;
    this.currentStep = 1;
    this.selectedMatieres = [];
  }

  goBack(): void {
    this.router.navigate(['/login']);
  }

  // Helper method to check if we're on a specific step
  isStep(step: number): boolean {
    return this.currentStep === step;
  }
  
  // Step navigation methods
  nextStep(): void {
    if (this.currentStep < this.totalSteps) {
      // Validate current step before moving forward
      if (this.isCurrentStepValid()) {
        this.currentStep++;
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }
  }
  
  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
  
  goToStep(step: number): void {
    if (step >= 1 && step <= this.totalSteps && step <= this.currentStep) {
      this.currentStep = step;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
  
  isCurrentStepValid(): boolean {
    const form = this.userType === 'student' ? this.studentForm : this.tutorForm;
    
    if (this.userType === 'student') {
      switch (this.currentStep) {
        case 1: // Personal info
          return !!(form.get('nom')?.valid && form.get('prenom')?.valid && form.get('email')?.valid);
        case 2: // Account info
          return !!(form.get('motDePasse')?.valid && form.get('telephone')?.valid);
        case 3: // Academic info
          return !!(form.get('filiere')?.valid && form.get('annee')?.valid && form.get('niveau')?.valid);
        default:
          return false;
      }
    } else { // tutor
      switch (this.currentStep) {
        case 1: // Personal info
          return !!(form.get('nom')?.valid && form.get('prenom')?.valid && form.get('email')?.valid);
        case 2: // Account info
          return !!(form.get('motDePasse')?.valid && form.get('telephone')?.valid);
        case 3: // Professional info
          return !!(form.get('experience')?.valid && form.get('tarifHoraire')?.valid);
        case 4: // Specialties
          return this.selectedMatieres.length > 0;
        default:
          return false;
      }
    }
  }
  
  getStepTitle(step: number): string {
    if (this.userType === 'student') {
      switch (step) {
        case 1: return 'Informations personnelles';
        case 2: return 'Informations de compte';
        case 3: return 'Informations académiques';
        default: return '';
      }
    } else {
      switch (step) {
        case 1: return 'Informations personnelles';
        case 2: return 'Informations de compte';
        case 3: return 'Informations professionnelles';
        case 4: return 'Spécialités';
        default: return '';
      }
    }
  }

  onStudentSubmit(): void {
    if (this.studentForm.valid) {
      this.isLoading = true;
      
      const studentData: StudentRegisterRequest = {
        nom: this.studentForm.value.nom,
        prenom: this.studentForm.value.prenom,
        email: this.studentForm.value.email,
        motDePasse: this.studentForm.value.motDePasse,
        telephone: this.studentForm.value.telephone,
        filiere: this.studentForm.value.filiere,
        annee: parseInt(this.studentForm.value.annee),
        niveau: this.studentForm.value.niveau as NiveauAcademique
      };

      this.authService.registerStudent(studentData).subscribe({
        next: (response) => {
          this.isLoading = false;
          console.log('Inscription réussie! Bienvenue!');
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erreur lors de l\'inscription:', error);
        }
      });
    }
  }

  onTutorSubmit(): void {
    if (this.tutorForm.valid && this.selectedMatieres.length > 0) {
      this.isLoading = true;
      
      const tutorData: TutorRegisterRequest = {
        nom: this.tutorForm.value.nom,
        prenom: this.tutorForm.value.prenom,
        email: this.tutorForm.value.email,
        motDePasse: this.tutorForm.value.motDePasse,
        telephone: this.tutorForm.value.telephone,
        experience: this.tutorForm.value.experience,
        tarifHoraire: parseFloat(this.tutorForm.value.tarifHoraire),
        diplomes: this.tutorForm.value.diplomes,
        description: this.tutorForm.value.description,
        specialiteIds: this.selectedMatieres,
        ville: this.tutorForm.value.ville,
        pays: this.tutorForm.value.pays,
        coursEnLigne: this.tutorForm.value.coursEnLigne,
        coursPresentiel: this.tutorForm.value.coursPresentiel
      };

      this.authService.registerTutor(tutorData).subscribe({
        next: (response) => {
          this.isLoading = false;
          console.log('Inscription réussie! Bienvenue!');
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erreur lors de l\'inscription:', error);
        }
      });
    } else if (this.selectedMatieres.length === 0) {
      console.error('Veuillez sélectionner au moins une spécialité');
    }
  }
  
  // Helper methods for matiere selection
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
  
  // Searchable dropdown methods
  filterMatieres(): void {
    const searchTerm = this.matiereSearchTerm.toLowerCase().trim();
    if (!searchTerm) {
      this.filteredMatieres = this.matieres;
    } else {
      this.filteredMatieres = this.matieres.filter(matiere =>
        matiere.nom.toLowerCase().includes(searchTerm) ||
        matiere.niveau.toLowerCase().includes(searchTerm)
      );
    }
    this.showMatiereDropdown = true;
  }
  
  clearMatiereSearch(): void {
    this.matiereSearchTerm = '';
    this.filteredMatieres = this.matieres;
    this.showMatiereDropdown = false;
  }
  
  removeMatiereSelection(matiereId: number): void {
    const index = this.selectedMatieres.indexOf(matiereId);
    if (index > -1) {
      this.selectedMatieres.splice(index, 1);
    }
  }
  
  getMatiereName(matiereId: number): string {
    const matiere = this.matieres.find(m => m.id === matiereId);
    return matiere ? matiere.nom : 'Unknown';
  }
}