import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { TutorService } from '../../services/tutor.service';
import { TutorProfileResponse } from '../../core/models/tutor-backend.model';

interface TutorVerificationItem extends TutorProfileResponse {
  processing?: boolean;
}

@Component({
  selector: 'app-tutor-verification',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tutor-verification.component.html',
  styleUrls: ['./tutor-verification.component.css']
})
export class TutorVerificationComponent implements OnInit, OnDestroy {
  // Tutor lists
  allTutors: TutorVerificationItem[] = [];
  verifiedTutors: TutorVerificationItem[] = [];
  unverifiedTutors: TutorVerificationItem[] = [];
  filteredTutors: TutorVerificationItem[] = [];
  
  // UI state
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;
  
  // Filters
  searchTerm = '';
  statusFilter: 'all' | 'verified' | 'unverified' = 'all';
  sortBy: 'name' | 'rating' | 'evaluations' | 'date' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // Statistics
  totalTutors = 0;
  totalVerified = 0;
  totalUnverified = 0;
  averageRating = 0;
  
  // Modal state
  selectedTutor: TutorVerificationItem | null = null;
  showDetailsModal = false;
  showConfirmModal = false;
  confirmAction: 'verify' | 'unverify' | null = null;
  
  private destroy$ = new Subject<void>();

  constructor(private tutorService: TutorService) {}

  ngOnInit(): void {
    this.loadTutors();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ===============================================
  // DATA LOADING
  // ===============================================

  loadTutors(): void {
    this.loading = true;
    this.error = null;

    // Get all tutor summaries first, then fetch full profiles
    this.tutorService.getAllActiveTutors()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (summaries) => {
          // Fetch full profiles for all tutors
          const profileRequests = summaries.map(summary =>
            this.tutorService.getTutorProfile(summary.id)
          );

          if (profileRequests.length === 0) {
            this.allTutors = [];
            this.categorizeTutors();
            this.calculateStatistics();
            this.applyFilters();
            this.loading = false;
            return;
          }

          forkJoin(profileRequests)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (profiles) => {
                this.allTutors = profiles.map(p => ({ ...p, processing: false }));
                this.categorizeTutors();
                this.calculateStatistics();
                this.applyFilters();
                this.loading = false;
              },
              error: (error) => {
                console.error('Failed to load tutor profiles:', error);
                this.error = 'Impossible de charger les profils des tuteurs';
                this.loading = false;
              }
            });
        },
        error: (error) => {
          console.error('Failed to load tutors:', error);
          this.error = 'Impossible de charger les tuteurs';
          this.loading = false;
        }
      });
  }

  private categorizeTutors(): void {
    this.verifiedTutors = this.allTutors.filter(t => t.verifie);
    this.unverifiedTutors = this.allTutors.filter(t => !t.verifie);
  }

  private calculateStatistics(): void {
    this.totalTutors = this.allTutors.length;
    this.totalVerified = this.verifiedTutors.length;
    this.totalUnverified = this.unverifiedTutors.length;
    
    const ratingsSum = this.allTutors
      .filter(t => t.noteMoyenne)
      .reduce((sum, t) => sum + (t.noteMoyenne || 0), 0);
    const ratingsCount = this.allTutors.filter(t => t.noteMoyenne).length;
    this.averageRating = ratingsCount > 0 ? ratingsSum / ratingsCount : 0;
  }

  // ===============================================
  // FILTERING AND SORTING
  // ===============================================

  applyFilters(): void {
    let filtered = [...this.allTutors];

    // Apply status filter
    switch (this.statusFilter) {
      case 'verified':
        filtered = this.verifiedTutors;
        break;
      case 'unverified':
        filtered = this.unverifiedTutors;
        break;
    }

    // Apply search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(t =>
        t.nom.toLowerCase().includes(term) ||
        t.prenom.toLowerCase().includes(term) ||
        (t.fullName && t.fullName.toLowerCase().includes(term)) ||
        t.email.toLowerCase().includes(term) ||
        t.ville?.toLowerCase().includes(term) ||
        t.specialites?.some(s => s.matiereNom.toLowerCase().includes(term))
      );
    }

    // Apply sorting
    filtered.sort((a, b) => {
      let comparison = 0;
      
      switch (this.sortBy) {
        case 'name':
          comparison = `${a.prenom} ${a.nom}`.localeCompare(`${b.prenom} ${b.nom}`);
          break;
        case 'rating':
          comparison = (b.noteMoyenne || 0) - (a.noteMoyenne || 0);
          break;
        case 'evaluations':
          comparison = (b.nombreEvaluations || 0) - (a.nombreEvaluations || 0);
          break;
        case 'date':
          comparison = new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime();
          break;
      }

      return this.sortDirection === 'asc' ? comparison : -comparison;
    });

    this.filteredTutors = filtered;
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStatusFilterChange(status: 'all' | 'verified' | 'unverified'): void {
    this.statusFilter = status;
    this.applyFilters();
  }

  onSortChange(field: 'name' | 'rating' | 'evaluations' | 'date'): void {
    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'desc';
    }
    this.applyFilters();
  }

  // ===============================================
  // VERIFICATION ACTIONS
  // ===============================================

  openVerifyConfirmation(tutor: TutorVerificationItem): void {
    this.selectedTutor = tutor;
    this.confirmAction = 'verify';
    this.showConfirmModal = true;
  }

  openUnverifyConfirmation(tutor: TutorVerificationItem): void {
    this.selectedTutor = tutor;
    this.confirmAction = 'unverify';
    this.showConfirmModal = true;
  }

  confirmVerificationAction(): void {
    if (!this.selectedTutor || !this.confirmAction) return;

    const tutor = this.selectedTutor;
    tutor.processing = true;
    this.showConfirmModal = false;

    const action$ = this.confirmAction === 'verify'
      ? this.tutorService.verifyTutor(tutor.id)
      : this.tutorService.unverifyTutor(tutor.id);

    action$
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedTutor) => {
          // Update the tutor in the list
          const index = this.allTutors.findIndex(t => t.id === tutor.id);
          if (index !== -1) {
            this.allTutors[index] = { ...updatedTutor, processing: false };
          }
          
          this.categorizeTutors();
          this.calculateStatistics();
          this.applyFilters();
          
          const action = this.confirmAction === 'verify' ? 'vérifié' : 'non vérifié';
          this.showSuccess(`Tuteur ${action} avec succès`);
          
          this.selectedTutor = null;
          this.confirmAction = null;
        },
        error: (error) => {
          console.error('Verification action failed:', error);
          tutor.processing = false;
          this.showError('Échec de l\'action de vérification');
        }
      });
  }

  cancelVerificationAction(): void {
    this.showConfirmModal = false;
    this.selectedTutor = null;
    this.confirmAction = null;
  }

  // ===============================================
  // TUTOR DETAILS
  // ===============================================

  viewTutorDetails(tutor: TutorVerificationItem): void {
    this.selectedTutor = tutor;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    setTimeout(() => {
      this.selectedTutor = null;
    }, 300);
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  getVerificationBadgeClass(verified: boolean): string {
    return verified
      ? 'bg-green-100 text-green-800 border-green-300'
      : 'bg-yellow-100 text-yellow-800 border-yellow-300';
  }

  getVerificationIcon(verified: boolean): string {
    return verified ? 'verified' : 'pending';
  }

  getRatingStars(rating: number | null | undefined): string {
    if (!rating) return '☆☆☆☆☆';
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);
    
    return '★'.repeat(fullStars) +
           (halfStar ? '⯨' : '') +
           '☆'.repeat(emptyStars);
  }

  formatDate(dateString: string | null | undefined): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getSpecialitiesText(tutor: TutorVerificationItem): string {
    if (!tutor.specialites || tutor.specialites.length === 0) {
      return 'Aucune spécialité';
    }
    return tutor.specialites.map(s => s.matiereNom).join(', ');
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = null;
    }, 5000);
  }

  private showError(message: string): void {
    this.error = message;
    setTimeout(() => {
      this.error = null;
    }, 5000);
  }

  refresh(): void {
    this.loadTutors();
  }
}
