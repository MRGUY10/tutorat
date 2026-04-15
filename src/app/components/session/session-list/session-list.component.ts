import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';

import { SessionService } from '../../../services/session.service';
import { AuthService } from '../../../services/auth.service';
import { EvaluationService } from '../../../services/evaluation.service';
import { RatingModalComponent } from '../../shared/rating-modal/rating-modal.component';
import { SessionCalendarComponent } from '../session-calendar/session-calendar.component';
import {
  SessionCard,
  SessionFilters,
  SessionStatus,
  SessionType,
  SessionStatistics,
  SessionResponse,
  getSessionStatusDisplay,
  getSessionTypeDisplay,
  formatDuration
} from '../../../core/models/session.model';

@Component({
  selector: 'app-session-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RatingModalComponent, SessionCalendarComponent],
  templateUrl: './session-list.component.html',
  styleUrls: ['./session-list.component.css']
})
export class SessionListComponent implements OnInit, OnDestroy {
  @Input() userId!: number;
  @Input() userType: 'tutor' | 'student' = 'student';
  @Input() showStatistics = true;
  @Input() maxHeight = '600px';

  // Component state
  sessions: SessionCard[] = [];
  filteredSessions: SessionCard[] = [];
  statistics: SessionStatistics | null = null;
  loading = false;
  error: string | null = null;
  
  // UI state
  showFilters = false;
  selectedSession: SessionCard | null = null;
  viewMode: 'list' | 'grid' | 'calendar' = 'list';
  sortBy: 'date' | 'status' | 'subject' | 'price' = 'date';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  // Rating modal
  showRatingModal = false;
  sessionToRate: SessionResponse | null = null;
  sessionEvaluationStatus = new Map<number, boolean>();
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;
  
  // Forms and filters
  filterForm: FormGroup;
  searchForm: FormGroup;
  
  // Enums for template
  SessionStatus = SessionStatus;
  SessionType = SessionType;
  
  private destroy$ = new Subject<void>();

  constructor(
    private sessionService: SessionService,
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private evaluationService: EvaluationService
  ) {
    this.filterForm = this.createFilterForm();
    this.searchForm = this.createSearchForm();
  }

  ngOnInit(): void {
    this.initializeComponent();
    this.setupFormSubscriptions();
    
    // Get user info from auth service if not provided as input
    if (!this.userId || this.userId <= 0) {
      const authUserId = this.authService.getUserId();
      if (authUserId && authUserId > 0) {
        this.userId = authUserId;
      }
    }
    
    // Get user type from auth service if it's still default
    if (this.userType === 'student') {
      const userRole = this.authService.getUserRole();
      if (userRole === 'TUTOR') {
        this.userType = 'tutor';
      }
    }
    
    // Only load data if we have a valid userId
    if (this.userId && this.userId > 0) {
      this.loadSessions();
      this.loadStatistics();
    } else {
      console.warn('SessionListComponent: Invalid or missing userId, skipping data load');
      
      // If we don't have userId yet, wait for authentication
      this.authService.isAuthenticated$.pipe(
        takeUntil(this.destroy$)
      ).subscribe(isAuthenticated => {
        if (isAuthenticated) {
          const authUserId = this.authService.getUserId();
          const userRole = this.authService.getUserRole();
          
          if (authUserId && authUserId > 0) {
            this.userId = authUserId;
            this.userType = userRole === 'TUTOR' ? 'tutor' : 'student';
            this.loadSessions();
            this.loadStatistics();
          }
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Public method to refresh data (can be called externally)
  public refreshData(): void {
    if (this.userId && this.userId > 0) {
      this.loadSessions();
      this.loadStatistics();
    }
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private initializeComponent(): void {
    // Subscribe to session cards
    this.sessionService.sessionCards$
      .pipe(takeUntil(this.destroy$))
      .subscribe(sessions => {
        this.sessions = sessions;
        this.applyFiltersAndSort();
        // Load evaluation status for completed sessions
        this.loadEvaluationStatus();
      });

    // Subscribe to loading state
    this.sessionService.sessionsLoading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => this.loading = loading);

    // Subscribe to error state
    this.sessionService.sessionsError$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => this.error = error);
  }

  private createFilterForm(): FormGroup {
    return this.formBuilder.group({
      status: [''],
      type: [''],
      startDate: [''],
      endDate: [''],
      subjectId: [''],
      minPrice: [''],
      maxPrice: ['']
    });
  }

  private createSearchForm(): FormGroup {
    return this.formBuilder.group({
      searchTerm: ['']
    });
  }

  private setupFormSubscriptions(): void {
    // Search form subscription
    this.searchForm.get('searchTerm')?.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.applyFiltersAndSort());

    // Filter form subscription
    this.filterForm.valueChanges
      .pipe(
        debounceTime(300),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.applyFiltersAndSort());
  }

  // ===============================================
  // DATA LOADING
  // ===============================================

  private loadSessions(): void {
    // Validate userId before making API calls
    if (!this.userId || this.userId <= 0) {
      console.warn('SessionListComponent: Cannot load sessions - invalid userId:', this.userId);
      return;
    }

    const filters = this.buildFiltersFromForm();
    
    if (this.userType === 'tutor') {
      this.sessionService.getSessionsByTutor(this.userId, filters).subscribe();
    } else {
      this.sessionService.getSessionsByStudent(this.userId, filters).subscribe();
    }
  }

  private loadStatistics(): void {
    if (this.showStatistics && this.userId && this.userId > 0) {
      this.sessionService.getSessionStatistics(this.userId, this.userType)
        .pipe(takeUntil(this.destroy$))
        .subscribe(stats => this.statistics = stats);
    }
  }

  private buildFiltersFromForm(): SessionFilters {
    const formValue = this.filterForm.value;
    const filters: SessionFilters = {};

    if (formValue.status) filters.status = formValue.status;
    if (formValue.type) filters.type = formValue.type;
    if (formValue.startDate) filters.startDate = new Date(formValue.startDate);
    if (formValue.endDate) filters.endDate = new Date(formValue.endDate);
    if (formValue.subjectId) filters.subjectId = parseInt(formValue.subjectId);
    if (formValue.minPrice) filters.minPrice = parseFloat(formValue.minPrice);
    if (formValue.maxPrice) filters.maxPrice = parseFloat(formValue.maxPrice);

    const searchTerm = this.searchForm.get('searchTerm')?.value;
    if (searchTerm) filters.searchTerm = searchTerm;

    return filters;
  }

  // ===============================================
  // FILTERING AND SORTING
  // ===============================================

  private applyFiltersAndSort(): void {
    let filtered = [...this.sessions];

    // Apply search filter
    const searchTerm = this.searchForm.get('searchTerm')?.value?.toLowerCase();
    if (searchTerm) {
      filtered = filtered.filter(session =>
        session.subject.toLowerCase().includes(searchTerm) ||
        session.studentName.toLowerCase().includes(searchTerm) ||
        session.tutorName.toLowerCase().includes(searchTerm) ||
        session.title.toLowerCase().includes(searchTerm)
      );
    }

    // Apply other filters
    const formFilters = this.filterForm.value;
    
    if (formFilters.status) {
      filtered = filtered.filter(session => session.status === formFilters.status);
    }
    
    if (formFilters.type) {
      filtered = filtered.filter(session => session.type === formFilters.type);
    }
    
    if (formFilters.minPrice) {
      filtered = filtered.filter(session => session.price >= parseFloat(formFilters.minPrice));
    }
    
    if (formFilters.maxPrice) {
      filtered = filtered.filter(session => session.price <= parseFloat(formFilters.maxPrice));
    }

    // Apply sorting
    this.sortSessions(filtered);

    // Update pagination
    this.updatePagination(filtered);
  }

  private sortSessions(sessions: SessionCard[]): void {
    sessions.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'date':
          comparison = a.dateTime.getTime() - b.dateTime.getTime();
          break;
        case 'status':
          comparison = a.status.localeCompare(b.status);
          break;
        case 'subject':
          comparison = a.subject.localeCompare(b.subject);
          break;
        case 'price':
          comparison = a.price - b.price;
          break;
      }

      return this.sortDirection === 'asc' ? comparison : -comparison;
    });

    this.filteredSessions = sessions;
  }

  private updatePagination(sessions: SessionCard[]): void {
    this.totalPages = Math.ceil(sessions.length / this.itemsPerPage);
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.filteredSessions = sessions.slice(startIndex, endIndex);
  }

  // ===============================================
  // UI ACTIONS
  // ===============================================

  onSort(field: 'date' | 'status' | 'subject' | 'price'): void {
    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'asc';
    }
    this.applyFiltersAndSort();
  }

  onPageChange(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.applyFiltersAndSort();
    }
  }

  onViewModeChange(mode: 'list' | 'grid' | 'calendar'): void {
    this.viewMode = mode;
  }

  onSessionSelect(session: SessionCard): void {
    this.selectedSession = session;
  }

  onSessionAction(session: SessionCard, action: 'view' | 'join' | 'cancel' | 'edit'): void {
    switch (action) {
      case 'view':
        this.router.navigate(['/sessions', session.id]);
        break;
      case 'join':
        this.joinSession(session);
        break;
      case 'cancel':
        this.cancelSession(session);
        break;
      case 'edit':
        this.router.navigate(['/sessions', session.id, 'edit']);
        break;
    }
  }

  private joinSession(session: SessionCard): void {
    const joinUrl = this.sessionService.getSessionJoinUrl({
      id: session.id,
      tuteurId: 0,
      etudiantId: 0,
      matiereId: 0,
      dateHeure: session.dateTime.toISOString(),
      duree: session.duration,
      statut: session.status,
      prix: session.price,
      typeSession: session.type,
      lienVisio: session.type === SessionType.EN_LIGNE ? 'https://meet.google.com/example' : undefined,
      notes: '',
      salle: session.location,
      createdAt: new Date().toISOString()
    });

    if (joinUrl) {
      window.open(joinUrl, '_blank');
    } else if (session.type === SessionType.PRESENTIEL) {
      // Show location details
      alert(`Rendez-vous en présentiel: ${session.location}`);
    }
  }

  private cancelSession(session: SessionCard): void {
    if (confirm('Êtes-vous sûr de vouloir annuler cette session ?')) {
      this.sessionService.cancelSession(session.id).subscribe({
        next: () => {
          this.loadSessions();
        },
        error: (error) => {
          console.error('Failed to cancel session:', error);
          alert('Erreur lors de l\'annulation de la session');
        }
      });
    }
  }

  onRefresh(): void {
    this.loadSessions();
    this.loadStatistics();
  }

  onClearFilters(): void {
    this.filterForm.reset();
    this.searchForm.reset();
    this.currentPage = 1;
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  getStatusDisplay = getSessionStatusDisplay;
  getTypeDisplay = getSessionTypeDisplay;
  formatDuration = formatDuration;

  formatDate(date: Date): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(price);
  }

  canJoinSession(session: SessionCard): boolean {
    return this.sessionService.canJoinSession({
      id: session.id,
      tuteurId: 0,
      etudiantId: 0,
      matiereId: 0,
      dateHeure: session.dateTime.toISOString(),
      duree: session.duration,
      statut: session.status,
      prix: session.price,
      typeSession: session.type,
      createdAt: new Date().toISOString(),
      lienVisio: session.type === SessionType.EN_LIGNE ? 'https://meet.google.com/example' : undefined,
      notes: '',
      salle: session.location
    });
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  getSessionCountByStatus(status: SessionStatus): number {
    return this.sessions.filter(s => s.status === status).length;
  }

  getUpcomingSessionsCount(): number {
    return this.sessions.filter(s => s.isUpcoming).length;
  }

  trackSession(index: number, session: SessionCard): number {
    return session.id;
  }

  // ===============================================
  // RATING FUNCTIONALITY
  // ===============================================

  /**
   * Open rating modal for a completed session
   */
  async openRatingModal(session: SessionCard): Promise<void> {
    // Check if session is completed
    if (session.status !== SessionStatus.TERMINEE) {
      console.warn('Can only rate completed sessions');
      return;
    }

    // Check if already rated
    const alreadyRated = this.sessionEvaluationStatus.get(session.id);
    if (alreadyRated) {
      console.warn('Session already rated');
      return;
    }

    try {
      // Fetch the full session details
      const fullSession = await this.sessionService.getSessionById(session.id).toPromise();
      if (fullSession) {
        this.sessionToRate = fullSession;
        this.showRatingModal = true;
      }
    } catch (error) {
      console.error('Failed to load session for rating:', error);
      this.error = 'Impossible de charger la session pour l\'évaluation';
    }
  }

  /**
   * Close rating modal
   */
  closeRatingModal(): void {
    this.showRatingModal = false;
    this.sessionToRate = null;
  }

  /**
   * Handle rating submission
   */
  onRatingSubmitted(): void {
    if (this.sessionToRate) {
      // Mark as rated
      this.sessionEvaluationStatus.set(this.sessionToRate.id, true);
      
      // Show success message
      console.log('Rating submitted successfully');
      
      // Refresh data
      this.loadSessions();
    }
  }

  /**
   * Check if user can rate a session
   */
  canRateSession(session: SessionCard): boolean {
    // Only completed sessions can be rated
    if (session.status !== SessionStatus.TERMINEE) {
      return false;
    }

    // Check if already rated
    const alreadyRated = this.sessionEvaluationStatus.get(session.id);
    if (alreadyRated) {
      return false;
    }

    return true;
  }

  /**
   * Load evaluation status for sessions
   */
  private loadEvaluationStatus(): void {
    this.sessions.forEach(session => {
      if (session.status === SessionStatus.TERMINEE) {
        this.evaluationService.getSessionEvaluationStatus(session.id)
          .subscribe(status => {
            // If user is student, check if student evaluation exists
            // If user is tutor, check if tutor evaluation exists
            const hasRated = this.userType === 'student' 
              ? status.studentEvaluationExists 
              : status.tutorEvaluationExists;
            
            this.sessionEvaluationStatus.set(session.id, hasRated);
          });
      }
    });
  }
}