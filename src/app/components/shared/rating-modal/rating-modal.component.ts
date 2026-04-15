import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EvaluationService } from '../../../services/evaluation.service';
import { AuthService } from '../../../services/auth.service';
import { CreateEvaluation, EvaluationType } from '../../../core/models/evaluation.model';
import { SessionResponse } from '../../../core/models/session.model';

@Component({
  selector: 'app-rating-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rating-modal.component.html',
  styleUrls: ['./rating-modal.component.css']
})
export class RatingModalComponent implements OnInit {
  @Input() session!: SessionResponse;
  @Input() show = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() ratingSubmitted = new EventEmitter<void>();

  // Rating data
  rating = 0;
  hoveredRating = 0;
  comment = '';
  
  // Detailed criteria
  showDetailedCriteria = false;
  qualiteEnseignement = 0;
  communication = 0;
  ponctualite = 0;
  preparation = 0;
  patience = 0;
  recommanderais = true;

  // UI state
  submitting = false;
  error: string | null = null;
  currentUserId!: number;
  userType: 'student' | 'tutor' = 'student';

  constructor(
    private evaluationService: EvaluationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.currentUserId = userId;
    }

    const userRole = this.authService.getUserRole();
    this.userType = userRole === 'TUTOR' ? 'tutor' : 'student';
  }

  // ===============================================
  // RATING INTERACTION
  // ===============================================

  setRating(value: number): void {
    this.rating = value;
  }

  setHoveredRating(value: number): void {
    this.hoveredRating = value;
  }

  clearHoveredRating(): void {
    this.hoveredRating = 0;
  }

  getStarClass(position: number): string {
    const activeRating = this.hoveredRating || this.rating;
    if (position <= activeRating) {
      return 'text-yellow-400 fill-current';
    }
    return 'text-gray-300';
  }

  setCriteriaRating(criteria: string, value: number): void {
    switch (criteria) {
      case 'qualiteEnseignement':
        this.qualiteEnseignement = value;
        break;
      case 'communication':
        this.communication = value;
        break;
      case 'ponctualite':
        this.ponctualite = value;
        break;
      case 'preparation':
        this.preparation = value;
        break;
      case 'patience':
        this.patience = value;
        break;
    }
  }

  // ===============================================
  // FORM SUBMISSION
  // ===============================================

  async submitRating(): Promise<void> {
    if (this.rating === 0) {
      this.error = 'Veuillez sélectionner une note';
      return;
    }

    if (!this.session || !this.currentUserId) {
      this.error = 'Session ou utilisateur non défini';
      return;
    }

    this.submitting = true;
    this.error = null;

    // Determine evaluation type and evaluated user
    const isStudent = this.userType === 'student';
    const evaluationType = isStudent ? EvaluationType.ETUDIANT_VERS_TUTEUR : EvaluationType.TUTEUR_VERS_ETUDIANT;
    const evalueId = isStudent ? this.session.tuteurId : this.session.etudiantId;

    const evaluation: CreateEvaluation = {
      sessionId: this.session.id,
      evaluateurId: this.currentUserId,
      evalueId: evalueId,
      note: this.rating,
      commentaire: this.comment.trim() || undefined,
      typeEvaluation: evaluationType,
      recommanderais: this.recommanderais
    };

    // Add detailed criteria if provided
    if (this.showDetailedCriteria) {
      if (this.qualiteEnseignement > 0) evaluation.qualiteEnseignement = this.qualiteEnseignement;
      if (this.communication > 0) evaluation.communication = this.communication;
      if (this.ponctualite > 0) evaluation.ponctualite = this.ponctualite;
      if (this.preparation > 0) evaluation.preparation = this.preparation;
      if (this.patience > 0) evaluation.patience = this.patience;
    }

    try {
      await this.evaluationService.createEvaluation(evaluation).toPromise();
      this.ratingSubmitted.emit();
      this.close();
    } catch (error: any) {
      console.error('Failed to submit rating:', error);
      this.error = error.error?.message || 'Échec de la soumission de l\'évaluation';
    } finally {
      this.submitting = false;
    }
  }

  // ===============================================
  // MODAL CONTROL
  // ===============================================

  close(): void {
    this.resetForm();
    this.closeModal.emit();
  }

  private resetForm(): void {
    this.rating = 0;
    this.hoveredRating = 0;
    this.comment = '';
    this.qualiteEnseignement = 0;
    this.communication = 0;
    this.ponctualite = 0;
    this.preparation = 0;
    this.patience = 0;
    this.recommanderais = true;
    this.showDetailedCriteria = false;
    this.error = null;
  }

  toggleDetailedCriteria(): void {
    this.showDetailedCriteria = !this.showDetailedCriteria;
  }
}
