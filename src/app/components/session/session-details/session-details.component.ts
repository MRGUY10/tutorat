import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

import { SessionService } from '../../../services/session.service';
import {
  SessionResponse,
  SessionDetails,
  UpdateSession,
  SessionStatus,
  SessionType,
  getSessionStatusDisplay,
  getSessionTypeDisplay,
  formatDuration
} from '../../../core/models/session.model';

@Component({
  selector: 'app-session-details',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './session-details.component.html',
  styleUrls: ['./session-details.component.css']
})
export class SessionDetailsComponent implements OnInit, OnDestroy {
  @Input() sessionId?: number;
  @Input() userType: 'tutor' | 'student' = 'student';
  @Input() allowEdit = true;

  // Component state
  session: SessionDetails | null = null;
  loading = false;
  error: string | null = null;
  saving = false;

  // UI state
  isEditing = false;
  showConfirmDialog = false;
  confirmAction: 'cancel' | 'complete' | 'start' | null = null;
  activeTab: 'details' | 'notes' | 'history' = 'details';

  // Forms
  editForm: FormGroup;
  notesForm: FormGroup;

  // Enums for template
  SessionStatus = SessionStatus;
  SessionType = SessionType;

  private destroy$ = new Subject<void>();

  constructor(
    private sessionService: SessionService,
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.editForm = this.createEditForm();
    this.notesForm = this.createNotesForm();
  }

  ngOnInit(): void {
    // Get session ID from route if not provided as input
    if (!this.sessionId) {
      this.route.params.pipe(takeUntil(this.destroy$))
        .subscribe(params => {
          if (params['id']) {
            this.sessionId = parseInt(params['id']);
            this.loadSession();
          }
        });
    } else {
      this.loadSession();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private createEditForm(): FormGroup {
    return this.formBuilder.group({
      dateHeure: ['', Validators.required],
      duree: [60, [Validators.required, Validators.min(15), Validators.max(480)]],
      prix: [0, [Validators.required, Validators.min(0)]],
      typeSession: ['', Validators.required],
      lienVisio: [''],
      salle: [''],
      notes: ['']
    });
  }

  private createNotesForm(): FormGroup {
    return this.formBuilder.group({
      notes: ['', Validators.maxLength(1000)]
    });
  }

  // ===============================================
  // DATA LOADING
  // ===============================================

  loadSession(): void {
    if (!this.sessionId) return;

    this.loading = true;
    this.error = null;

    this.sessionService.getSessionDetails(this.sessionId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (session) => {
          this.session = session;
          this.populateForms();
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Erreur lors du chargement de la session';
          this.loading = false;
          console.error('Failed to load session:', error);
        }
      });
  }

  private populateForms(): void {
    if (!this.session) return;

    // Populate edit form
    this.editForm.patchValue({
      dateHeure: new Date(this.session.dateHeure).toISOString().slice(0, 16),
      duree: this.session.duree,
      prix: this.session.prix,
      typeSession: this.session.typeSession,
      lienVisio: this.session.lienVisio || '',
      salle: this.session.salle || '',
      notes: this.session.notes || ''
    });

    // Populate notes form
    this.notesForm.patchValue({
      notes: this.session.notes || ''
    });
  }

  // ===============================================
  // SESSION ACTIONS
  // ===============================================

  onStartSession(): void {
    if (!this.session) return;

    this.confirmAction = 'start';
    this.showConfirmDialog = true;
  }

  onCompleteSession(): void {
    if (!this.session) return;

    this.confirmAction = 'complete';
    this.showConfirmDialog = true;
  }

  onCancelSession(): void {
    if (!this.session) return;

    this.confirmAction = 'cancel';
    this.showConfirmDialog = true;
  }

  onJoinSession(): void {
    if (!this.session) return;

    const joinUrl = this.sessionService.getSessionJoinUrl(this.session);
    
    if (joinUrl) {
      window.open(joinUrl, '_blank');
    } else if (this.session.typeSession === SessionType.PRESENTIEL) {
      // Show location details in a modal or navigate to location info
      alert(`Rendez-vous en présentiel: ${this.session.salle || 'Lieu à confirmer'}`);
    }
  }

  onConfirmAction(): void {
    if (!this.session || !this.confirmAction) return;

    this.saving = true;

    let action$;
    switch (this.confirmAction) {
      case 'start':
        action$ = this.sessionService.startSession(this.session.id);
        break;
      case 'complete':
        action$ = this.sessionService.completeSession(this.session.id);
        break;
      case 'cancel':
        action$ = this.sessionService.cancelSession(this.session.id);
        break;
      default:
        return;
    }

    action$.pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedSession) => {
          // Reload the full session details to get updated data with participant info
          this.loadSession();
          this.saving = false;
          this.showConfirmDialog = false;
          this.confirmAction = null;
        },
        error: (error) => {
          this.saving = false;
          console.error('Failed to update session:', error);
          alert('Erreur lors de la mise à jour de la session');
        }
      });
  }

  onCancelAction(): void {
    this.showConfirmDialog = false;
    this.confirmAction = null;
  }

  // ===============================================
  // EDITING ACTIONS
  // ===============================================

  onStartEdit(): void {
    this.isEditing = true;
    this.populateForms();
  }

  onCancelEdit(): void {
    this.isEditing = false;
    this.populateForms();
  }

  onSaveChanges(): void {
    if (!this.session || !this.editForm.valid) return;

    this.saving = true;
    const formValue = this.editForm.value;

    const updateData: UpdateSession = {
      dateHeure: new Date(formValue.dateHeure).toISOString(),
      duree: formValue.duree,
      prix: formValue.prix,
      typeSession: formValue.typeSession,
      lienVisio: formValue.lienVisio,
      salle: formValue.salle,
      notes: formValue.notes
    };

    this.sessionService.updateSession(this.session.id, updateData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedSession) => {
          // Reload session details with participant info
          this.loadSession();
          this.isEditing = false;
          this.saving = false;
        },
        error: (error) => {
          this.saving = false;
          console.error('Failed to update session:', error);
          alert('Erreur lors de la sauvegarde');
        }
      });
  }

  onSaveNotes(): void {
    if (!this.session || !this.notesForm.valid) return;

    this.saving = true;
    const notes = this.notesForm.get('notes')?.value;

    const updateData: UpdateSession = { notes };

    this.sessionService.updateSession(this.session.id, updateData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedSession) => {
          // Reload session details with participant info
          this.loadSession();
          this.saving = false;
        },
        error: (error) => {
          this.saving = false;
          console.error('Failed to update notes:', error);
          alert('Erreur lors de la sauvegarde des notes');
        }
      });
  }

  // ===============================================
  // UI HELPERS
  // ===============================================

  onTabChange(tab: 'details' | 'notes' | 'history'): void {
    this.activeTab = tab;
  }

  onBack(): void {
    this.router.navigate(['/sessions']);
  }

  getStatusDisplay = getSessionStatusDisplay;
  getTypeDisplay = getSessionTypeDisplay;
  formatDuration = formatDuration;

  formatDate(dateString: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(dateString));
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(price);
  }

  canJoinSession(): boolean {
    if (!this.session) return false;
    return this.sessionService.canJoinSession(this.session);
  }

  canStartSession(): boolean {
    if (!this.session) return false;
    return this.session.statut === SessionStatus.CONFIRMEE;
  }

  canCompleteSession(): boolean {
    if (!this.session) return false;
    return this.session.statut === SessionStatus.EN_COURS;
  }

  canCancelSession(): boolean {
    if (!this.session) return false;
    return this.session.statut === SessionStatus.DEMANDEE || 
           this.session.statut === SessionStatus.CONFIRMEE;
  }

  canEditSession(): boolean {
    if (!this.session || !this.allowEdit) return false;
    return this.session.statut === SessionStatus.DEMANDEE || 
           this.session.statut === SessionStatus.CONFIRMEE;
  }

  getStatusColor(): string {
    if (!this.session) return 'bg-gray-100 text-gray-800';

    switch (this.session.statut) {
      case SessionStatus.DEMANDEE:
        return 'bg-yellow-100 text-yellow-800';
      case SessionStatus.CONFIRMEE:
        return 'bg-blue-100 text-blue-800';
      case SessionStatus.EN_COURS:
        return 'bg-green-100 text-green-800';
      case SessionStatus.TERMINEE:
        return 'bg-gray-100 text-gray-800';
      case SessionStatus.ANNULEE:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getTypeIcon(): string {
    if (!this.session) return 'help';
    
    switch (this.session.typeSession) {
      case SessionType.EN_LIGNE:
        return 'videocam';
      case SessionType.PRESENTIEL:
        return 'location_on';
      default:
        return 'help';
    }
  }

  getActionButtonText(): string {
    if (!this.confirmAction) return '';

    switch (this.confirmAction) {
      case 'start':
        return 'Démarrer la session';
      case 'complete':
        return 'Terminer la session';
      case 'cancel':
        return 'Annuler la session';
      default:
        return '';
    }
  }

  getConfirmMessage(): string {
    if (!this.confirmAction) return '';

    switch (this.confirmAction) {
      case 'start':
        return 'Êtes-vous sûr de vouloir démarrer cette session ?';
      case 'complete':
        return 'Êtes-vous sûr de vouloir marquer cette session comme terminée ?';
      case 'cancel':
        return 'Êtes-vous sûr de vouloir annuler cette session ? Cette action ne peut pas être annulée.';
      default:
        return '';
    }
  }

  isFormValid(): boolean {
    return this.editForm.valid;
  }

  getFieldError(fieldName: string): string {
    const field = this.editForm.get(fieldName);
    if (!field || !field.errors || !field.touched) return '';

    if (field.errors['required']) return 'Ce champ est obligatoire';
    if (field.errors['min']) return `Valeur minimale: ${field.errors['min'].min}`;
    if (field.errors['max']) return `Valeur maximale: ${field.errors['max'].max}`;

    return 'Valeur invalide';
  }
}