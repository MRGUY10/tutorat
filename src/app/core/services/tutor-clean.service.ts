import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';

// Import backend models
import { 
  TutorProfileResponse,
  TutorSummaryResponse,
  TutorSearchRequest,
  TutorProfileUpdateRequest,
  TutorStatisticsResponse
} from '../models/tutor-backend.model';

// Import existing models for backward compatibility
import { 
  CompleteTutorProfile,
  TutorCard,
  TutorSearchFilters
} from '../models/tutor.model';

/**
 * Updated TutorService that properly integrates with the backend TutorController
 * Uses proper HTTP typing with Angular's HttpClient
 */
@Injectable({
  providedIn: 'root'
})
export class TutorService {
  private readonly baseApiUrl = 'http://localhost:8080/api/tutors';
  
  // State management
  private tutorsSubject = new BehaviorSubject<TutorSummaryResponse[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);
  private statisticsSubject = new BehaviorSubject<TutorStatisticsResponse | null>(null);
  
  // Public observables
  public tutors$ = this.tutorsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public error$ = this.errorSubject.asObservable();
  public statistics$ = this.statisticsSubject.asObservable();
  
  // Computed observables
  public tutorCards$ = this.tutors$.pipe(
    map(tutors => tutors.map(tutor => this.convertSummaryToCard(tutor)))
  );

  constructor(private http: HttpClient) {
    this.loadTutors();
  }

  // ================================================
  // PROFILE OPERATIONS
  // ================================================

  /**
   * Get tutor profile by ID
   * GET /api/tutors/{id}
   */
  getTutorProfile(id: number): Observable<TutorProfileResponse> {
    return this.makeRequest<TutorProfileResponse>('GET', `${this.baseApiUrl}/${id}`);
  }

  /**
   * Update tutor profile
   * PUT /api/tutors/{id}
   */
  updateTutorProfile(id: number, request: TutorProfileUpdateRequest): Observable<TutorProfileResponse> {
    return this.makeRequest<TutorProfileResponse>('PUT', `${this.baseApiUrl}/${id}`, request)
      .pipe(tap(() => this.refreshTutors()));
  }

  /**
   * Delete tutor (soft delete)
   * DELETE /api/tutors/{id}
   */
  deleteTutor(id: number): Observable<void> {
    return this.makeRequest<any>('DELETE', `${this.baseApiUrl}/${id}`)
      .pipe(
        map(() => void 0),
        tap(() => this.refreshTutors())
      );
  }

  // ================================================
  // LISTING AND SEARCH OPERATIONS
  // ================================================

  /**
   * Get all active tutors
   * GET /api/tutors
   */
  getAllActiveTutors(): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    return this.makeRequest<TutorSummaryResponse[]>('GET', this.baseApiUrl)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Get available tutors for booking
   * GET /api/tutors/available
   */
  getAvailableTutors(): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/available`)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Get verified tutors
   * GET /api/tutors/verified
   */
  getVerifiedTutors(): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/verified`)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Get top-rated tutors
   * GET /api/tutors/top-rated
   */
  getTopRatedTutors(limit: number = 10): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    const params = new HttpParams().set('limit', limit.toString());
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/top-rated`, null, params)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Search tutors by keyword
   * GET /api/tutors/search
   */
  searchTutorsByKeyword(keyword?: string): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    let params = new HttpParams();
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/search`, null, params)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Advanced search with filters
   * POST /api/tutors/search
   */
  searchTutorsWithFilters(searchRequest: TutorSearchRequest): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    return this.makeRequest<TutorSummaryResponse[]>('POST', `${this.baseApiUrl}/search`, searchRequest)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Get tutors by specialty
   * GET /api/tutors/specialty/{specialite}
   */
  getTutorsBySpecialty(specialite: string): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/specialty/${encodeURIComponent(specialite)}`)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Get tutors by location
   * GET /api/tutors/location/{ville}
   */
  getTutorsByLocation(ville: string): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/location/${encodeURIComponent(ville)}`)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  /**
   * Get tutors by price range
   * GET /api/tutors/price-range
   */
  getTutorsByPriceRange(minPrice: number, maxPrice: number): Observable<TutorSummaryResponse[]> {
    this.setLoading(true);
    const params = new HttpParams()
      .set('minPrice', minPrice.toString())
      .set('maxPrice', maxPrice.toString());
    return this.makeRequest<TutorSummaryResponse[]>('GET', `${this.baseApiUrl}/price-range`, null, params)
      .pipe(tap(tutors => this.updateTutorState(tutors)));
  }

  // ================================================
  // ADMINISTRATION OPERATIONS
  // ================================================

  /**
   * Verify tutor profile (Admin only)
   * PUT /api/tutors/{id}/verify
   */
  verifyTutor(id: number): Observable<TutorProfileResponse> {
    return this.makeRequest<TutorProfileResponse>('PUT', `${this.baseApiUrl}/${id}/verify`, {})
      .pipe(tap(() => this.refreshTutors()));
  }

  /**
   * Update tutor availability
   * PUT /api/tutors/{id}/availability
   */
  updateTutorAvailability(id: number, available: boolean): Observable<TutorProfileResponse> {
    const params = new HttpParams().set('available', available.toString());
    return this.makeRequest<TutorProfileResponse>('PUT', `${this.baseApiUrl}/${id}/availability`, {}, params)
      .pipe(tap(() => this.refreshTutors()));
  }

  // ================================================
  // STATISTICS AND ANALYTICS
  // ================================================

  /**
   * Get tutor statistics
   * GET /api/tutors/statistics
   */
  getTutorStatistics(): Observable<TutorStatisticsResponse> {
    return this.makeRequest<TutorStatisticsResponse>('GET', `${this.baseApiUrl}/statistics`)
      .pipe(tap(stats => this.statisticsSubject.next(stats)));
  }

  // ================================================
  // BACKWARD COMPATIBILITY METHODS
  // ================================================

  /**
   * Legacy method: Get all tutors in old format for backward compatibility
   */
  getAllTutors(): Observable<CompleteTutorProfile[]> {
    return this.getAllActiveTutors().pipe(
      map(tutors => tutors.map(tutor => this.convertSummaryToCompleteProfile(tutor)))
    );
  }

  /**
   * Legacy method: Search with old filter format
   */
  searchTutorsWithLegacyFilters(filters: TutorSearchFilters): Observable<CompleteTutorProfile[]> {
    const searchRequest = this.convertLegacyFiltersToSearchRequest(filters);
    return this.searchTutorsWithFilters(searchRequest).pipe(
      map(tutors => tutors.map(tutor => this.convertSummaryToCompleteProfile(tutor)))
    );
  }

  // ================================================
  // UTILITY METHODS
  // ================================================

  /**
   * Generic HTTP request method that handles typing properly
   */
  private makeRequest<T>(
    method: string, 
    url: string, 
    body?: any, 
    params?: HttpParams
  ): Observable<T> {
    const options = {
      ...(params && { params }),
      ...(body && { body })
    };

    let request: Observable<any>;
    
    switch (method.toLowerCase()) {
      case 'get':
        request = this.http.get(url, options);
        break;
      case 'post':
        request = this.http.post(url, body || {}, options);
        break;
      case 'put':
        request = this.http.put(url, body || {}, options);
        break;
      case 'delete':
        request = this.http.delete(url, options);
        break;
      default:
        return throwError(() => new Error(`Unsupported HTTP method: ${method}`));
    }

    return request.pipe(
      map(response => response as T),
      catchError(this.handleError<T>(`${method} ${url}`))
    );
  }

  /**
   * Initialize service by loading tutors
   */
  private loadTutors(): void {
    this.getAllActiveTutors().subscribe();
  }

  /**
   * Refresh tutors after updates
   */
  private refreshTutors(): void {
    this.getAllActiveTutors().subscribe();
  }

  /**
   * Update internal state with new tutors
   */
  private updateTutorState(tutors: TutorSummaryResponse[]): void {
    this.tutorsSubject.next(tutors);
    this.setLoading(false);
    this.clearError();
  }

  /**
   * Set loading state
   */
  private setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  /**
   * Clear error state
   */
  private clearError(): void {
    this.errorSubject.next(null);
  }

  /**
   * Convert TutorSummaryResponse to TutorCard for backward compatibility
   */
  private convertSummaryToCard(summary: TutorSummaryResponse): TutorCard {
    return {
      id: summary.id,
      fullName: summary.fullName,
      tarif: summary.tarifHoraire,
      noteGlobale: summary.noteMoyenne,
      nombreEvaluations: summary.nombreEvaluations,
      isVerified: summary.verifie,
      disponible: summary.disponible,
      locationCity: summary.ville,
      locationCountry: summary.pays,
      onlineTeaching: summary.coursEnLigne,
      inPersonTeaching: summary.coursPresentiel,
      description: summary.description,
      availabilityStatus: summary.disponible ? 'AVAILABLE' : 'OFFLINE'
    } as TutorCard;
  }

  /**
   * Convert TutorSummaryResponse to CompleteTutorProfile
   */
  private convertSummaryToCompleteProfile(summary: TutorSummaryResponse): CompleteTutorProfile {
    return {
      id: summary.id,
      nom: summary.fullName?.split(' ')[1] || '',
      prenom: summary.fullName?.split(' ')[0] || '',
      email: `${summary.fullName?.toLowerCase().replace(/\s+/g, '.')}@example.com`,
      statut: 'ACTIVE' as any,
      dateInscription: new Date().toISOString(),
      tarif: summary.tarifHoraire,
      noteGlobale: summary.noteMoyenne,
      nombreEvaluations: summary.nombreEvaluations,
      description: summary.description,
      isVerified: summary.verifie,
      locationCity: summary.ville,
      locationCountry: summary.pays,
      onlineTeaching: summary.coursEnLigne,
      inPersonTeaching: summary.coursPresentiel,
      availabilityStatus: summary.disponible ? 'AVAILABLE' : 'OFFLINE'
    } as CompleteTutorProfile;
  }

  /**
   * Convert legacy search filters to TutorSearchRequest format
   */
  private convertLegacyFiltersToSearchRequest(filters: TutorSearchFilters): TutorSearchRequest {
    return {
      keyword: (filters as any).searchTerm,
      ville: filters.city,
      pays: filters.country,
      minTarif: filters.minTarif,
      maxTarif: filters.maxTarif,
      minRating: filters.minRating,
      verifiedOnly: filters.verified,
      availableOnly: (filters as any).availabilityStatus === 'AVAILABLE',
      onlineOnly: filters.onlineTeaching === true && filters.inPersonTeaching !== true,
      inPersonOnly: filters.inPersonTeaching === true && filters.onlineTeaching !== true,
      sortBy: 'rating',
      sortDirection: 'desc',
      page: 0,
      size: 20
    };
  }

  /**
   * Handle HTTP errors
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: HttpErrorResponse): Observable<T> => {
      console.error(`${operation} failed:`, error);
      
      let errorMessage = `${operation} failed`;
      
      if (error.error instanceof ErrorEvent) {
        errorMessage = `Error: ${error.error.message}`;
      } else {
        if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else {
          errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
      }
      
      this.errorSubject.next(errorMessage);
      this.setLoading(false);
      
      if (result !== undefined) {
        return of(result as T);
      }
      
      return throwError(() => error);
    };
  }
}