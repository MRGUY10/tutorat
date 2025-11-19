import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

// Services
import { SessionService } from '../../../services/session.service';
import { SubjectService } from '../../../services/subject.service';
import { AuthService } from '../../../services/auth.service';

// Models
import { CompleteTutorProfile, TutorSpecialty } from '../../../core/models/tutor.model';
import { Subject as SubjectModel, NiveauAcademique } from '../../../core/models/subject.model';
import { CreateSessionRequest, SessionType, Urgence } from '../../../core/models/session.model';
import {
  BookingStep,
  BookingState,
  BookingFormData,
  createInitialBookingState,
  createInitialBookingFormData,
  validateBookingForm,
  formatDuration,
  formatBookingDateTime,
  getUrgencyDisplay,
  getSessionTypeDisplay
} from '../../../core/models/booking.model';

@Component({
  selector: 'app-booking-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './booking-modal.component.html',
  styleUrls: ['./booking-modal.component.scss']
})
export class BookingModalComponent implements OnInit, OnDestroy, OnChanges {
  private destroy$ = new Subject<void>();

  // Input/Output
  @Input() isOpen = false;
  @Input() tutor: CompleteTutorProfile | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() bookingSuccess = new EventEmitter<any>();

  // State
  bookingState: BookingState = createInitialBookingState();
  bookingForm!: FormGroup;
  selectedSubject: SubjectModel | null = null;
  tutorSpecialties: TutorSpecialty[] = [];
  availableSubjects: SubjectModel[] = [];

  // Enums for template
  SessionType = SessionType;
  Urgence = Urgence;

  constructor(
    private fb: FormBuilder,
    private sessionService: SessionService,
    private subjectService: SubjectService,
    private authService: AuthService
  ) {
    this.createBookingForm();
  }

  ngOnInit(): void {
    // Watch for tutor changes
    if (this.tutor && this.isOpen) {
      this.initializeBooking();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['tutor'] && this.tutor) || (changes['isOpen'] && this.isOpen && this.tutor)) {
      this.initializeBooking();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ================================================
  // INITIALIZATION
  // ================================================

  initializeBooking(): void {
    console.log('initializeBooking called with tutor:', this.tutor);
    if (!this.tutor) {
      console.log('No tutor available for booking initialization');
      return;
    }
    
    this.bookingState = createInitialBookingState();
    this.bookingState.isActive = true;
    this.bookingState.currentStep = 'subjects';
    
    this.loadTutorSpecialties();
    this.resetForm();
  }

  private loadTutorSpecialties(): void {
    console.log('loadTutorSpecialties - tutor:', this.tutor);
    console.log('loadTutorSpecialties - tutor.specialites:', this.tutor?.specialites);
    
    if (!this.tutor?.specialites) {
      console.log('No tutor specialites found');
      return;
    }
    
    this.tutorSpecialties = this.tutor.specialites;
    console.log('Tutor specialties assigned:', this.tutorSpecialties);
    
    // Convert specialties to Subject format for consistency
    this.availableSubjects = this.tutorSpecialties.map(specialty => ({
      id: specialty.matiereId,
      nom: specialty.matiereNom,
      description: specialty.matiereDescription || '',
      niveau: NiveauAcademique.INTERMEDIAIRE, // Default level since not available in specialty
      domaine: 'General', // Default domain since not available in specialty
      createdAt: new Date(),
      updatedAt: new Date()
    }));
    console.log('Available subjects created:', this.availableSubjects);
  }

  private createBookingForm(): void {
    const formData = createInitialBookingFormData();
    
    this.bookingForm = this.fb.group({
      matiereId: [formData.matiereId, Validators.required],
      dateVoulue: [formData.dateVoulue, Validators.required],
      dureeSouhaitee: [formData.dureeSouhaitee, [Validators.required, Validators.min(30), Validators.max(240)]],
      budgetMax: [formData.budgetMax, [Validators.required, Validators.min(0)]],
      urgence: [formData.urgence, Validators.required],
      message: [formData.message, Validators.required],
      notesAdditionnelles: [formData.notesAdditionnelles],
      flexibleSurDate: [formData.flexibleSurDate],
      accepteSeanceEnLigne: [formData.accepteSeanceEnLigne],
      accepteSeancePresentiel: [formData.accepteSeancePresentiel]
    });
  }

  private resetForm(): void {
    const formData = createInitialBookingFormData();
    this.bookingForm.patchValue(formData);
    this.selectedSubject = null;
  }

  // ================================================
  // MODAL CONTROL
  // ================================================

  openModal(tutor: CompleteTutorProfile): void {
    this.tutor = tutor;
    this.isOpen = true;
    this.initializeBooking();
  }

  closeModal(): void {
    this.isOpen = false;
    this.bookingState = createInitialBookingState();
    this.selectedSubject = null;
    this.availableSubjects = [];
    this.tutorSpecialties = [];
    this.close.emit();
  }

  // ================================================
  // BOOKING FLOW NAVIGATION
  // ================================================

  onSubjectSelect(subject: SubjectModel): void {
    this.selectedSubject = subject;
    this.bookingForm.patchValue({ matiereId: subject.id });
    this.nextStep();
  }

  nextStep(): void {
    switch (this.bookingState.currentStep) {
      case 'subjects':
        this.bookingState.currentStep = 'details';
        break;
      case 'details':
        if (this.bookingForm.valid) {
          this.bookingState.currentStep = 'review';
        } else {
          this.markFormGroupTouched();
        }
        break;
    }
  }

  previousStep(): void {
    switch (this.bookingState.currentStep) {
      case 'details':
        this.bookingState.currentStep = 'subjects';
        break;
      case 'review':
        this.bookingState.currentStep = 'details';
        break;
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.bookingForm.controls).forEach(key => {
      const control = this.bookingForm.get(key);
      control?.markAsTouched();
    });
  }

  // ================================================
  // FORM SUBMISSION
  // ================================================

  onSubmitBooking(): void {
    if (!this.bookingForm.valid || !this.tutor || !this.selectedSubject) {
      this.markFormGroupTouched();
      return;
    }

    this.bookingState.isSubmitting = true;
    this.bookingState.error = null;

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.bookingState.error = 'User not authenticated';
      this.bookingState.isSubmitting = false;
      return;
    }

    const formValue = this.bookingForm.value;
    const sessionRequest: CreateSessionRequest = {
      tuteurId: this.tutor.id,
      matiereId: formValue.matiereId,
      dateVoulue: formValue.dateVoulue,
      dureeSouhaitee: formValue.dureeSouhaitee,
      budgetMax: formValue.budgetMax,
      urgence: formValue.urgence,
      message: formValue.message,
      notesAdditionnelles: formValue.notesAdditionnelles,
      flexibleSurDate: formValue.flexibleSurDate,
      accepteSeanceEnLigne: formValue.accepteSeanceEnLigne,
      accepteSeancePresentiel: formValue.accepteSeancePresentiel
    };

    this.sessionService.createSessionRequest(currentUser.id, sessionRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('Session request created:', response);
          this.bookingState.currentStep = 'success';
          this.bookingState.isSuccess = true;
          this.bookingState.isSubmitting = false;
          this.bookingSuccess.emit(response);
        },
        error: (error) => {
          console.error('Error creating session request:', error);
          this.bookingState.error = error.error?.message || 'Failed to create session request';
          this.bookingState.isSubmitting = false;
        }
      });
  }

  // ================================================
  // HELPER METHODS
  // ================================================

  getSessionTypeDisplay(type: SessionType): string {
    return getSessionTypeDisplay(type);
  }

  getUrgenceDisplay(urgence: Urgence): string {
    return getUrgencyDisplay(urgence);
  }

  formatBookingDateTime(dateTime: string): string {
    return formatBookingDateTime(dateTime);
  }

  formatDuration(minutes: number): string {
    return formatDuration(minutes);
  }

  getProgressWidth(): string {
    switch (this.bookingState.currentStep) {
      case 'subjects': return '25%';
      case 'details': return '50%';
      case 'review': return '75%';
      case 'success': return '100%';
      default: return '0%';
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.bookingForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.bookingForm.get(fieldName);
    if (field?.errors) {
      if (field.errors['required']) return `${fieldName} is required`;
      if (field.errors['min']) return `Minimum value is ${field.errors['min'].min}`;
      if (field.errors['max']) return `Maximum value is ${field.errors['max'].max}`;
    }
    return '';
  }

  // Additional booking actions
  bookAnotherSession(): void {
    this.initializeBooking();
  }
}