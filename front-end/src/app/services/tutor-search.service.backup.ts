import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, of, combineLatest } from 'rxjs';
import { map, catchError, tap, shareReplay } from 'rxjs/operators';
import { 
  CompleteTutorProfile,
  TutorSearchFilters,
  TutorSearchParams,
  TutorSearchFormOptions,
  TutorStatistics,
  TutorReview,
  CreateReviewRequest,
  TutorCard,
  AvailabilityStatus,
  AgeGroup,
  UserStatus,
  tutorProfileToCard
} from '../core/models/tutor.model';

// Import new service
import { TutorService } from './tutor.service';
import { convertLegacyFiltersToSearchRequest } from '../core/models/tutor-backend.model';

/**
 * Legacy TutorSearchService that now delegates to the new TutorService
 * Provides backward compatibility for existing components
 */
@Injectable({
  providedIn: 'root'
})
export class TutorSearchService {
  private readonly reviewsApiUrl = 'http://localhost:8080/api/tutors/reviews';
  
  // State management - now delegates to TutorService
  public tutors$ = this.tutorService.tutors$.pipe(
    map(tutors => tutors.map(tutor => this.convertSummaryToCompleteProfile(tutor)))
  );
  public tutorCards$ = this.tutorService.tutorCards$;
  public loading$ = this.tutorService.loading$;
  public error$ = this.tutorService.error$;
  public statistics$ = this.tutorService.statistics$;

  constructor(
    private http: HttpClient,
    private tutorService: TutorService
  ) {}

  // ================================================
  // TUTOR SEARCH AND DISCOVERY - Updated to use TutorService
  // ================================================

  /**
   * Get all complete tutor profiles - delegates to TutorService
   */
  getAllTutors(): Observable<CompleteTutorProfile[]> {
    return this.tutorService.getAllTutors();
  }

  /**
   * Search tutors by keywords - delegates to TutorService
   */
  searchTutorsByKeywords(searchTerm: string): Observable<CompleteTutorProfile[]> {
    return this.tutorService.searchTutorsByKeyword(searchTerm).pipe(
      map(summaries => summaries.map(s => this.convertSummaryToCompleteProfile(s)))
    );
  }

  /**
   * Get available tutors only - delegates to TutorService
   */
  getAvailableTutors(): Observable<CompleteTutorProfile[]> {
    return this.tutorService.getAvailableTutors().pipe(
      map(summaries => summaries.map(s => this.convertSummaryToCompleteProfile(s)))
    );
  }

  /**
   * Get verified tutors only - delegates to TutorService
   */
  getVerifiedTutors(): Observable<CompleteTutorProfile[]> {
    return this.tutorService.getVerifiedTutors().pipe(
      map(summaries => summaries.map(s => this.convertSummaryToCompleteProfile(s)))
    );
  }

  /**
   * Advanced search with filters - delegates to TutorService
   */
  searchTutorsWithFilters(filters: TutorSearchFilters): Observable<CompleteTutorProfile[]> {
    return this.tutorService.searchTutorsWithLegacyFilters(filters);
  }

  /**
   * Get tutor by ID - delegates to TutorService
   */
  getTutorById(id: number): Observable<CompleteTutorProfile> {
    return this.tutorService.getTutorProfile(id).pipe(
      map(profile => ({
        id: profile.id,
        nom: profile.nom,
        prenom: profile.prenom,
        email: profile.email,
        telephone: profile.telephone,
        statut: profile.statut,
        dateInscription: profile.dateInscription,
        experience: profile.experience,
        tarif: profile.tarifHoraire,
        diplomes: profile.diplomes,
        description: profile.description,
        noteGlobale: profile.noteMoyenne,
        nombreEvaluations: profile.nombreEvaluations,
        isVerified: profile.verifie,
        locationCity: profile.ville,
        locationCountry: profile.pays,
        onlineTeaching: profile.coursEnLigne,
        inPersonTeaching: profile.coursPresentiel,
        availabilityStatus: profile.disponible ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.OFFLINE,
        profileCompletionPercentage: profile.profileCompletion,
        verificationDate: profile.dateVerification
      } as CompleteTutorProfile))
    );
  }

  /**
   * Helper method to convert TutorSummaryResponse to CompleteTutorProfile
   */
  private convertSummaryToCompleteProfile(summary: any): CompleteTutorProfile {
    return {
      id: summary.id,
      nom: summary.fullName?.split(' ')[1] || '',
      prenom: summary.fullName?.split(' ')[0] || '',
      email: `${summary.fullName?.toLowerCase().replace(' ', '.')}@example.com`,
      statut: UserStatus.ACTIVE,
      dateInscription: new Date().toISOString(),
      specialite: summary.specialite,
      tarif: summary.tarifHoraire,
      noteGlobale: summary.noteMoyenne,
      nombreEvaluations: summary.nombreEvaluations,
      description: summary.description,
      isVerified: summary.verifie,
      locationCity: summary.ville,
      locationCountry: summary.pays,
      onlineTeaching: summary.coursEnLigne,
      inPersonTeaching: summary.coursPresentiel,
      availabilityStatus: summary.disponible ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.OFFLINE
    } as CompleteTutorProfile;
  }

  // ================================================
  // TUTOR LOCATION AND FILTERING
  // ================================================

  /**
   * Get tutors by location
   */
  getTutorsByLocation(city?: string, country?: string): Observable<CompleteTutorProfile[]> {
    this.loadingSubject.next(true);
    
    let params = new HttpParams();
    if (city) params = params.set('city', city);
    if (country) params = params.set('country', country);

    return this.http.get<CompleteTutorProfile[]>(`${this.apiUrl}/location`, { params })
      .pipe(
        tap(tutors => {
          this.tutorsSubject.next(tutors);
          this.tutorCardsSubject.next(tutors.map(tutorProfileToCard));
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          this.handleError('Failed to load tutors by location', error);
          return of([]);
        })
      );
  }

  /**
   * Get all cities where tutors are available
   */
  getAllCities(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/cities`)
      .pipe(
        catchError(error => {
          this.handleError('Failed to load cities', error);
          return of(['Paris', 'Lyon', 'Marseille', 'Toulouse', 'Nice']); // Fallback
        })
      );
  }

  /**
   * Get all countries where tutors are available
   */
  getAllCountries(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/countries`)
      .pipe(
        catchError(error => {
          this.handleError('Failed to load countries', error);
          return of(['France', 'Belgique', 'Suisse', 'Canada']); // Fallback
        })
      );
  }

  // ================================================
  // TUTOR REVIEWS AND RATINGS
  // ================================================

  /**
   * Get reviews for a tutor
   */
  getTutorReviews(tuteurId: number): Observable<TutorReview[]> {
    return this.http.get<TutorReview[]>(`${this.reviewsApiUrl}/tutor/${tuteurId}`)
      .pipe(
        catchError(error => {
          this.handleError('Failed to load tutor reviews', error);
          return of([]);
        })
      );
  }

  /**
   * Get verified reviews for a tutor
   */
  getVerifiedTutorReviews(tuteurId: number): Observable<TutorReview[]> {
    return this.http.get<TutorReview[]>(`${this.reviewsApiUrl}/tutor/${tuteurId}/verified`)
      .pipe(
        catchError(error => {
          this.handleError('Failed to load verified reviews', error);
          return of([]);
        })
      );
  }

  /**
   * Create a review for a tutor
   */
  createReview(review: CreateReviewRequest): Observable<TutorReview> {
    return this.http.post<TutorReview>(this.reviewsApiUrl, review)
      .pipe(
        catchError(error => {
          this.handleError('Failed to create review', error);
          throw error;
        })
      );
  }

  /**
   * Get average rating for a tutor
   */
  getTutorAverageRating(tuteurId: number): Observable<number> {
    return this.http.get<number>(`${this.reviewsApiUrl}/tutor/${tuteurId}/average-rating`)
      .pipe(
        catchError(error => {
          this.handleError('Failed to get average rating', error);
          return of(0);
        })
      );
  }

  // ================================================
  // FORM OPTIONS AND HELPERS
  // ================================================

  /**
   * Get form options for search filters
   */
  getFormOptions(): Observable<TutorSearchFormOptions> {
    return combineLatest([
      this.getAllCities(),
      this.getAllCountries()
    ]).pipe(
      map(([cities, countries]) => ({
        availabilityOptions: [
          { value: AvailabilityStatus.AVAILABLE, label: 'Disponible' },
          { value: AvailabilityStatus.BUSY, label: 'Occupé' },
          { value: AvailabilityStatus.OFFLINE, label: 'Hors ligne' }
        ],
        ageGroupOptions: [
          { value: AgeGroup.ENFANTS, label: 'Enfants (5-12 ans)' },
          { value: AgeGroup.ADOLESCENTS, label: 'Adolescents (13-17 ans)' },
          { value: AgeGroup.JEUNES_ADULTES, label: 'Jeunes Adultes (18-25 ans)' },
          { value: AgeGroup.ADULTES, label: 'Adultes (26-60 ans)' },
          { value: AgeGroup.SENIORS, label: 'Seniors (60+ ans)' }
        ],
        cities,
        countries,
        languages: ['Français', 'English', 'Español', 'Deutsch', 'Italiano', 'العربية']
      })),
      catchError(error => {
        this.handleError('Failed to load form options', error);
        return of({
          availabilityOptions: [
            { value: AvailabilityStatus.AVAILABLE, label: 'Disponible' },
            { value: AvailabilityStatus.BUSY, label: 'Occupé' },
            { value: AvailabilityStatus.OFFLINE, label: 'Hors ligne' }
          ],
          ageGroupOptions: [
            { value: AgeGroup.ENFANTS, label: 'Enfants (5-12 ans)' },
            { value: AgeGroup.ADOLESCENTS, label: 'Adolescents (13-17 ans)' },
            { value: AgeGroup.JEUNES_ADULTES, label: 'Jeunes Adultes (18-25 ans)' },
            { value: AgeGroup.ADULTES, label: 'Adultes (26-60 ans)' },
            { value: AgeGroup.SENIORS, label: 'Seniors (60+ ans)' }
          ],
          cities: ['Paris', 'Lyon', 'Marseille', 'Toulouse', 'Nice'],
          countries: ['France', 'Belgique', 'Suisse', 'Canada'],
          languages: ['Français', 'English', 'Español', 'Deutsch', 'Italiano', 'العربية']
        });
      })
    );
  }

  /**
   * Get tutor statistics
   */
  getTutorStatistics(): Observable<TutorStatistics> {
    return this.getAllTutors().pipe(
      map(tutors => this.calculateStatistics(tutors)),
      tap(stats => this.statisticsSubject.next(stats)),
      catchError(error => {
        this.handleError('Failed to calculate statistics', error);
        return of({
          totalTutors: 0,
          availableTutors: 0,
          verifiedTutors: 0,
          averageRating: 0,
          tutorsByCity: {},
          tutorsByCountry: {},
          tutorsByAgeGroup: {},
          tutorsByAvailability: {}
        });
      })
    );
  }

  // ================================================
  // UTILITY METHODS
  // ================================================

  /**
   * Load tutors (to be called from components)
   */
  loadTutors(params?: TutorSearchParams): void {
    if (params?.searchTerm) {
      this.searchTutorsByKeywords(params.searchTerm).subscribe();
    } else {
      this.getAllTutors().subscribe();
    }
  }

  /**
   * Load available tutors only
   */
  loadAvailableTutors(): void {
    this.getAvailableTutors().subscribe();
  }

  /**
   * Load verified tutors only
   */
  loadVerifiedTutors(): void {
    this.getVerifiedTutors().subscribe();
  }

  /**
   * Refresh tutors list
   */
  refreshTutors(): void {
    this.loadTutors();
  }

  /**
   * Clear error
   */
  clearError(): void {
    this.errorSubject.next(null);
  }

  /**
   * Reset search results
   */
  resetSearch(): void {
    this.tutorsSubject.next([]);
    this.tutorCardsSubject.next([]);
    this.errorSubject.next(null);
  }

  // ================================================
  // PRIVATE HELPER METHODS
  // ================================================

  private handleError(message: string, error: any): void {
    console.error(message, error);
    let errorMessage = message;
    
    if (error?.status === 404) {
      errorMessage = 'Tutor API endpoint not found (404). Please check backend configuration.';
    } else if (error?.error?.message) {
      errorMessage = error.error.message;
    } else if (error?.message) {
      errorMessage = error.message;
    }
    
    this.errorSubject.next(errorMessage);
    this.loadingSubject.next(false);
  }

  private calculateStatistics(tutors: CompleteTutorProfile[]): TutorStatistics {
    const tutorsByCity: { [city: string]: number } = {};
    const tutorsByCountry: { [country: string]: number } = {};
    const tutorsByAgeGroup: { [ageGroup: string]: number } = {};
    const tutorsByAvailability: { [status: string]: number } = {};

    let totalRating = 0;
    let ratedTutorsCount = 0;
    let availableTutors = 0;
    let verifiedTutors = 0;

    tutors.forEach(tutor => {
      // Count by city
      if (tutor.locationCity) {
        tutorsByCity[tutor.locationCity] = (tutorsByCity[tutor.locationCity] || 0) + 1;
      }

      // Count by country
      if (tutor.locationCountry) {
        tutorsByCountry[tutor.locationCountry] = (tutorsByCountry[tutor.locationCountry] || 0) + 1;
      }

      // Count by age group
      if (tutor.preferredAgeGroup) {
        tutorsByAgeGroup[tutor.preferredAgeGroup] = (tutorsByAgeGroup[tutor.preferredAgeGroup] || 0) + 1;
      }

      // Count by availability
      if (tutor.availabilityStatus) {
        tutorsByAvailability[tutor.availabilityStatus] = (tutorsByAvailability[tutor.availabilityStatus] || 0) + 1;
        if (tutor.availabilityStatus === AvailabilityStatus.AVAILABLE) {
          availableTutors++;
        }
      }

      // Calculate average rating
      if (tutor.noteGlobale && tutor.noteGlobale > 0) {
        totalRating += tutor.noteGlobale;
        ratedTutorsCount++;
      }

      // Count verified tutors
      if (tutor.isVerified) {
        verifiedTutors++;
      }
    });

    return {
      totalTutors: tutors.length,
      availableTutors,
      verifiedTutors,
      averageRating: ratedTutorsCount > 0 ? totalRating / ratedTutorsCount : 0,
      tutorsByCity,
      tutorsByCountry,
      tutorsByAgeGroup,
      tutorsByAvailability
    };
  }

  /**
   * Get mock tutor data for development/testing
   */
  private getMockTutors(): CompleteTutorProfile[] {
    return [
      {
        id: 1,
        nom: 'Martin',
        prenom: 'Sophie',
        email: 'sophie.martin@example.com',
        telephone: '+33123456789',
        statut: UserStatus.ACTIVE,
        dateInscription: '2023-01-15',
        profilePhotoUrl: 'https://images.unsplash.com/photo-1494790108755-2616b612b714?w=400&h=400&fit=crop&crop=face',
        bio: 'Passionate mathematics teacher with 5 years of experience. I specialize in helping students overcome their fear of math and develop confidence in problem-solving.',
        diplomes: 'Master in Mathematics - Sorbonne University',
        experience: '5 years of tutoring experience',
        tarif: 25.0,
        noteGlobale: 4.8,
        nombreEvaluations: 12,
        locationCity: 'Paris',
        locationCountry: 'France',
        availabilityStatus: AvailabilityStatus.AVAILABLE,
        onlineTeaching: true,
        inPersonTeaching: true,
        preferredAgeGroup: AgeGroup.JEUNES_ADULTES
      },
      {
        id: 2,
        nom: 'Durand',
        prenom: 'Pierre',
        email: 'pierre.durand@example.com',
        telephone: '+33234567890',
        statut: UserStatus.ACTIVE,
        dateInscription: '2022-09-22',
        profilePhotoUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop&crop=face',
        bio: 'Computer Science expert with industry experience. I love teaching programming and helping students build real-world projects.',
        diplomes: 'PhD in Computer Science - École Polytechnique',
        experience: '8 years of professional development + 3 years tutoring',
        tarif: 35.0,
        noteGlobale: 4.9,
        nombreEvaluations: 28,
        locationCity: 'Lyon',
        locationCountry: 'France',
        availabilityStatus: AvailabilityStatus.AVAILABLE,
        onlineTeaching: true,
        inPersonTeaching: false,
        preferredAgeGroup: AgeGroup.ADULTES
      },
      {
        id: 3,
        nom: 'Bernard',
        prenom: 'Marie',
        email: 'marie.bernard@example.com',
        telephone: '+33345678901',
        statut: UserStatus.ACTIVE,
        dateInscription: '2023-03-10',
        profilePhotoUrl: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&h=400&fit=crop&crop=face',
        bio: 'Multilingual language teacher passionate about helping students master foreign languages through immersive techniques.',
        diplomes: 'Master in Applied Linguistics - University of Provence',
        experience: '4 years of language tutoring',
        tarif: 22.0,
        noteGlobale: 4.6,
        nombreEvaluations: 15,
        locationCity: 'Marseille',
        locationCountry: 'France',
        availabilityStatus: AvailabilityStatus.BUSY,
        onlineTeaching: true,
        inPersonTeaching: true,
        preferredAgeGroup: AgeGroup.JEUNES_ADULTES
      },
      {
        id: 4,
        nom: 'Petit',
        prenom: 'Thomas',
        email: 'thomas.petit@example.com',
        telephone: '+33456789012',
        statut: UserStatus.ACTIVE,
        dateInscription: '2023-11-05',
        profilePhotoUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop&crop=face',
        bio: 'Physics enthusiast with a talent for making complex concepts simple and engaging for students of all levels.',
        diplomes: 'Master in Physics - University of Nice',
        experience: '2 years of tutoring experience',
        tarif: 28.0,
        noteGlobale: 4.3,
        nombreEvaluations: 7,
        locationCity: 'Nice',
        locationCountry: 'France',
        availabilityStatus: AvailabilityStatus.AVAILABLE,
        onlineTeaching: false,
        inPersonTeaching: true,
        preferredAgeGroup: AgeGroup.ADULTES
      }
    ];
  }
}