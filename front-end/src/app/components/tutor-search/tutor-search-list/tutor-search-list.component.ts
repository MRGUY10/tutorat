import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { TutorSearchService } from '../../../services/tutor-search.service';
import { MessagingService } from '../../../services/messaging.service';
import { AuthService } from '../../../services/auth.service';
import { BookingModalComponent } from '../../shared/booking-modal/booking-modal.component';
import { 
  CompleteTutorProfile,
  TutorCard,
  TutorSearchFilters,
  TutorSearchFormOptions,
  TutorStatistics,
  AvailabilityStatus,
  AgeGroup,
  SearchSuggestion,
  getAvailabilityStatusDisplay,
  getAgeGroupDisplay
} from '../../../core/models/tutor.model';

@Component({
  selector: 'app-tutor-search-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, BookingModalComponent],
  templateUrl: './tutor-search-list.component.html',
  styleUrls: ['./tutor-search-list.component.scss']
})
export class TutorSearchListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  // Data
  tutors: CompleteTutorProfile[] = [];
  tutorCards: TutorCard[] = [];
  filteredTutorCards: TutorCard[] = [];
  statistics: TutorStatistics | null = null;
  formOptions: TutorSearchFormOptions | null = null;
  
  // UI State
  loading = false;
  error: string | null = null;
  
  // View modes
  viewMode: 'cards' | 'list' = 'cards';
  
  // Modal states
  showFilterModal = false;
  showTutorDetailModal = false;
  selectedTutor: CompleteTutorProfile | null = null;
  
  // Booking modal state
  showBookingModal = false;
  selectedTutorForBooking: CompleteTutorProfile | null = null;
  
  // Make Math available in template
  Math = Math;
  
  // Search and filtering
  searchForm!: FormGroup;
  filterForm!: FormGroup;
  
  // Quick filters
  quickFilters = {
    available: false,
    verified: false,
    onlineOnly: false,
    inPersonOnly: false
  };
  
  // Pagination
  currentPage = 0;
  pageSize = 12;
  totalTutors = 0;
  
  // Sorting
  sortOptions = [
    { value: 'rating', label: 'Meilleure note' },
    { value: 'price-asc', label: 'Prix croissant' },
    { value: 'price-desc', label: 'Prix décroissant' },
    { value: 'newest', label: 'Plus récents' },
    { value: 'experience', label: 'Plus expérimentés' }
  ];
  
  currentSort = 'rating';
  
  // Enums for template
  AvailabilityStatus = AvailabilityStatus;
  AgeGroup = AgeGroup;
  
  // Expose helper functions
  getAvailabilityStatusDisplay = getAvailabilityStatusDisplay;
  getAgeGroupDisplay = getAgeGroupDisplay;

  // Autocomplete properties
  searchSuggestions: SearchSuggestion[] = [];
  showSuggestions = false;
  selectedSuggestionIndex = -1;
  suggestionTimeout: any;
  loadingSuggestions = false;

  constructor(
    private tutorSearchService: TutorSearchService,
    private messagingService: MessagingService,
    private authService: AuthService,
    private router: Router
  ) {
    this.initializeForms();
  }

  ngOnInit(): void {
    this.initializeComponent();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForms(): void {
    // Search form
    this.searchForm = new FormGroup({
      searchQuery: new FormControl('')
    });

    // Filter form
    this.filterForm = new FormGroup({
      minTarif: new FormControl(null, [Validators.min(0)]),
      maxTarif: new FormControl(null, [Validators.min(0)]),
      minRating: new FormControl(null, [Validators.min(0), Validators.max(5)]),
      availabilityStatus: new FormControl(''),
      verified: new FormControl(null),
      city: new FormControl(''),
      country: new FormControl(''),
      language: new FormControl(''),
      preferredAgeGroup: new FormControl(''),
      onlineTeaching: new FormControl(null),
      inPersonTeaching: new FormControl(null),
      maxResponseHours: new FormControl(null, [Validators.min(1)])
    });

    // Setup search debouncing
    this.searchForm.get('searchQuery')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(query => {
      this.performSearch(query);
      this.loadSearchSuggestions(query);
    });

    // Setup filter changes
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.applyFilters();
    });
  }

  private initializeComponent(): void {
    // Subscribe to service observables
    this.tutorSearchService.tutors$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(tutors => {
      this.tutors = tutors;
    });

    this.tutorSearchService.tutorCards$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(cards => {
      console.log('🃏 Tutor cards received:', cards.length, cards);
      this.tutorCards = cards;
      this.applyClientSideFilters();
    });

    this.tutorSearchService.loading$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(loading => {
      this.loading = loading;
    });

    this.tutorSearchService.error$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(error => {
      console.log('❌ Error received:', error);
      this.error = error;
    });

    this.tutorSearchService.statistics$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(stats => {
      this.statistics = stats;
    });

    // Load initial data
    this.loadInitialData();
  }

  private loadInitialData(): void {
    console.log('🔍 Loading initial tutor data...');
    
    // Load form options
    this.tutorSearchService.getFormOptions().subscribe(options => {
      console.log('📋 Form options loaded:', options);
      this.formOptions = options;
    });

    // Load statistics
    this.tutorSearchService.getTutorStatistics().subscribe(stats => {
      console.log('📊 Statistics loaded:', stats);
    });

    // Load all tutors initially
    console.log('👨‍🏫 Loading all tutors...');
    this.tutorSearchService.loadTutors();
  }

  // ================================================
  // SEARCH AND FILTERING
  // ================================================

  performSearch(query: string): void {
    if (query && query.trim().length > 0) {
      this.tutorSearchService.searchTutorsByKeywords(query.trim());
    } else {
      this.tutorSearchService.loadTutors();
    }
  }

  applyFilters(): void {
    const formValue = this.filterForm.value;
    const filters: TutorSearchFilters = {};

    // Build filters object
    if (formValue.minTarif) filters.minTarif = formValue.minTarif;
    if (formValue.maxTarif) filters.maxTarif = formValue.maxTarif;
    if (formValue.minRating) filters.minRating = formValue.minRating;
    if (formValue.availabilityStatus) filters.availabilityStatus = formValue.availabilityStatus;
    if (formValue.verified !== null) filters.verified = formValue.verified;
    if (formValue.city) filters.city = formValue.city;
    if (formValue.country) filters.country = formValue.country;
    if (formValue.language) filters.language = formValue.language;
    if (formValue.preferredAgeGroup) filters.preferredAgeGroup = formValue.preferredAgeGroup;
    if (formValue.onlineTeaching !== null) filters.onlineTeaching = formValue.onlineTeaching;
    if (formValue.inPersonTeaching !== null) filters.inPersonTeaching = formValue.inPersonTeaching;
    if (formValue.maxResponseHours) filters.maxResponseHours = formValue.maxResponseHours;

    // Add search term if present
    const searchQuery = this.searchForm.get('searchQuery')?.value;
    if (searchQuery && searchQuery.trim().length > 0) {
      filters.searchTerm = searchQuery.trim();
    }

    // Apply filters
    if (Object.keys(filters).length > 0) {
      this.tutorSearchService.searchTutorsWithFilters(filters);
    } else {
      this.tutorSearchService.loadTutors();
    }
  }

  private applyClientSideFilters(): void {
    let filtered = [...this.tutorCards];

    // Apply quick filters
    if (this.quickFilters.available) {
      filtered = filtered.filter(card => card.isOnline);
    }
    if (this.quickFilters.verified) {
      filtered = filtered.filter(card => card.isVerified);
    }
    if (this.quickFilters.onlineOnly) {
      // Would need to check tutor profile for online teaching preference
    }
    if (this.quickFilters.inPersonOnly) {
      // Would need to check tutor profile for in-person teaching preference
    }

    // Apply sorting
    filtered = this.applySorting(filtered);

    this.filteredTutorCards = filtered;
    this.totalTutors = filtered.length;
  }

  private applySorting(cards: TutorCard[]): TutorCard[] {
    switch (this.currentSort) {
      case 'rating':
        return cards.sort((a, b) => (b.rating || 0) - (a.rating || 0));
      case 'price-asc':
        return cards.sort((a, b) => (a.tarif || 0) - (b.tarif || 0));
      case 'price-desc':
        return cards.sort((a, b) => (b.tarif || 0) - (a.tarif || 0));
      case 'newest':
        // Would need registration date in the card
        return cards;
      case 'experience':
        // Would need experience years in the card
        return cards;
      default:
        return cards;
    }
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.searchForm.reset();
    this.quickFilters = {
      available: false,
      verified: false,
      onlineOnly: false,
      inPersonOnly: false
    };
    this.tutorSearchService.loadTutors();
  }

  // ================================================
  // QUICK ACTIONS
  // ================================================

  toggleQuickFilter(filter: keyof typeof this.quickFilters): void {
    this.quickFilters[filter] = !this.quickFilters[filter];
    this.applyClientSideFilters();
  }

  loadAvailableTutors(): void {
    this.tutorSearchService.loadAvailableTutors();
  }

  loadVerifiedTutors(): void {
    this.tutorSearchService.loadVerifiedTutors();
  }

  refreshData(): void {
    this.tutorSearchService.refreshTutors();
    this.tutorSearchService.getTutorStatistics().subscribe();
  }

  // ================================================
  // VIEW CONTROLS
  // ================================================

  setViewMode(mode: 'cards' | 'list'): void {
    this.viewMode = mode;
  }

  setSortOrder(sortBy: string): void {
    this.currentSort = sortBy;
    this.applyClientSideFilters();
  }

  // ================================================
  // PAGINATION
  // ================================================

  get paginatedTutors(): TutorCard[] {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredTutorCards.slice(start, end);
  }

  get totalPages(): number {
    return Math.ceil(this.totalTutors / this.pageSize);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
    }
  }

  // ================================================
  // MODAL CONTROLS
  // ================================================

  openFilterModal(): void {
    this.showFilterModal = true;
  }

  closeFilterModal(): void {
    this.showFilterModal = false;
  }

  openTutorDetailModal(tutorCard: TutorCard): void {
    // Load full tutor profile
    this.tutorSearchService.getTutorById(tutorCard.id).subscribe(tutor => {
      this.selectedTutor = tutor;
      this.showTutorDetailModal = true;
    });
  }

  closeTutorDetailModal(): void {
    this.showTutorDetailModal = false;
    this.selectedTutor = null;
  }

  // ================================================
  // TUTOR ACTIONS
  // ================================================

  contactTutor(tutor: CompleteTutorProfile | TutorCard | null): void {
    if (!tutor) return;
    
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || !currentUser.id) {
      console.error('User not logged in');
      return;
    }

    // Get tutor name
    const tutorName = 'name' in tutor ? tutor.name : `${tutor.prenom} ${tutor.nom}`;

    // Create a conversation with the tutor
    const createConversation = {
      sujet: `Discussion avec ${tutorName}`,
      participantIds: [tutor.id],
      support: false
    };

    this.messagingService.createConversation(createConversation).subscribe({
      next: (conversation: any) => {
        console.log('Conversation created:', conversation);
        // Navigate to messaging with the conversation
        this.router.navigate(['/messages'], { 
          queryParams: { conversationId: conversation.id } 
        });
      },
      error: (error: any) => {
        console.error('Error creating conversation:', error);
        // Still navigate to messages page
        this.router.navigate(['/messages']);
      }
    });
  }

  bookSession(tutor: CompleteTutorProfile | TutorCard | null): void {
    console.log('bookSession called with tutor:', tutor);
    if (!tutor) return;
    
    // Always load fresh tutor profile to ensure we have specialties
    console.log('Loading full tutor profile by ID:', tutor.id);
    this.tutorSearchService.getTutorById(tutor.id).subscribe(fullTutor => {
      console.log('Received full tutor profile:', fullTutor);
      console.log('Tutor specialties:', fullTutor.specialites);
      this.selectedTutorForBooking = fullTutor;
      this.showBookingModal = true;
    });
  }

  viewTutorProfile(tutor: TutorCard): void {
    this.openTutorDetailModal(tutor);
  }

  // ================================================
  // UTILITY METHODS
  // ================================================

  clearError(): void {
    this.tutorSearchService.clearError();
  }

  getTutorInitials(tutorCard: TutorCard): string {
    const names = tutorCard.name.split(' ');
    return names.map(name => name.charAt(0)).join('').toUpperCase();
  }

  formatPrice(price: number | undefined): string {
    if (!price) return 'Prix non spécifié';
    return `${price.toFixed(2)} €/h`;
  }

  formatLocation(location: string | undefined): string {
    return location || 'Localisation non spécifiée';
  }

  formatLanguages(languages: string[] | undefined): string {
    if (!languages || languages.length === 0) return 'Langues non spécifiées';
    return languages.join(', ');
  }

  getStarArray(rating: number): boolean[] {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating);
    }
    return stars;
  }

  getResponseTimeText(hours: number | undefined): string {
    if (!hours) return 'Temps de réponse non spécifié';
    if (hours < 1) return 'Répond en moins d\'1h';
    if (hours === 1) return 'Répond en 1h';
    if (hours < 24) return `Répond en ${hours}h`;
    const days = Math.floor(hours / 24);
    return `Répond en ${days} jour${days > 1 ? 's' : ''}`;
  }

  // ================================================
  // AUTOCOMPLETE METHODS
  // ================================================

  /**
   * Load search suggestions based on query
   */
  loadSearchSuggestions(query: string): void {
    if (this.suggestionTimeout) {
      clearTimeout(this.suggestionTimeout);
    }

    if (!query || query.trim().length < 2) {
      this.searchSuggestions = [];
      this.showSuggestions = false;
      return;
    }

    this.suggestionTimeout = setTimeout(() => {
      this.loadingSuggestions = true;
      this.showSuggestions = true;
      
      this.tutorSearchService.getSearchSuggestions(query).subscribe(suggestions => {
        this.searchSuggestions = suggestions;
        this.selectedSuggestionIndex = -1;
        this.loadingSuggestions = false;
        this.showSuggestions = suggestions.length > 0;
      });
    }, 150);
  }

  /**
   * Handle search input focus
   */
  onSearchFocus(): void {
    const query = this.searchForm.get('searchQuery')?.value;
    if (query && query.trim().length >= 2 && this.searchSuggestions.length > 0) {
      this.showSuggestions = true;
    }
  }

  /**
   * Handle search input blur with delay
   */
  onSearchBlur(): void {
    // Delay hiding to allow click on suggestion
    setTimeout(() => {
      this.showSuggestions = false;
      this.selectedSuggestionIndex = -1;
    }, 200);
  }

  /**
   * Handle keyboard navigation in search
   */
  onSearchKeydown(event: KeyboardEvent): void {
    if (!this.showSuggestions || this.searchSuggestions.length === 0) {
      return;
    }

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.selectedSuggestionIndex = 
          this.selectedSuggestionIndex < this.searchSuggestions.length - 1 
            ? this.selectedSuggestionIndex + 1 
            : 0;
        break;

      case 'ArrowUp':
        event.preventDefault();
        this.selectedSuggestionIndex = 
          this.selectedSuggestionIndex > 0 
            ? this.selectedSuggestionIndex - 1 
            : this.searchSuggestions.length - 1;
        break;

      case 'Enter':
        event.preventDefault();
        if (this.selectedSuggestionIndex >= 0 && this.selectedSuggestionIndex < this.searchSuggestions.length) {
          this.selectSuggestion(this.searchSuggestions[this.selectedSuggestionIndex]);
        } else {
          // Perform search with current query
          const query = this.searchForm.get('searchQuery')?.value;
          this.performSearch(query);
          this.showSuggestions = false;
        }
        break;

      case 'Escape':
        this.showSuggestions = false;
        this.selectedSuggestionIndex = -1;
        break;
    }
  }

  /**
   * Select a search suggestion
   */
  selectSuggestion(suggestion: SearchSuggestion): void {
    this.showSuggestions = false;
    this.selectedSuggestionIndex = -1;

    switch (suggestion.type) {
      case 'tutor':
        // Search for the specific tutor
        this.searchForm.patchValue({ searchQuery: suggestion.label });
        this.performSearch(suggestion.label);
        break;

      case 'subject':
        // Search for the subject
        this.searchForm.patchValue({ searchQuery: suggestion.label });
        this.performSearch(suggestion.label);
        break;

      case 'location':
        // Search for the location
        this.searchForm.patchValue({ searchQuery: suggestion.label });
        this.performSearch(suggestion.label);
        break;
    }
  }

  // ================================================
  // BOOKING MODAL FUNCTIONALITY
  // ================================================

  onCloseBookingModal(): void {
    this.showBookingModal = false;
    this.selectedTutorForBooking = null;
  }

  onBookingSuccess(response: any): void {
    console.log('Booking successful:', response);
    // Handle successful booking (show notification, etc.)
    this.showBookingModal = false;
    this.selectedTutorForBooking = null;
  }
}