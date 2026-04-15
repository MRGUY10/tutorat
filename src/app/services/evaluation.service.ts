import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import {
  CreateEvaluation,
  EvaluationResponse,
  EvaluationSummary,
  EvaluationType,
  SessionEvaluationStatus,
  SessionFeedback
} from '../core/models/evaluation.model';

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {
  private readonly apiUrl = 'http://localhost:8080/api/evaluations';

  // Cache for evaluation summaries
  private evaluationSummaryCache = new Map<string, BehaviorSubject<EvaluationSummary | null>>();

  constructor(private http: HttpClient) {}

  // ===============================================
  // EVALUATION CRUD OPERATIONS
  // ===============================================

  /**
   * Create a new evaluation
   */
  createEvaluation(evaluation: CreateEvaluation): Observable<EvaluationResponse> {
    return this.http.post<EvaluationResponse>(this.apiUrl, evaluation)
      .pipe(
        tap(response => {
          console.log('Evaluation created successfully:', response);
          // Invalidate cache for the evaluated user
          this.invalidateCache(evaluation.evalueId, evaluation.typeEvaluation);
        }),
        catchError(error => {
          console.error('Failed to create evaluation:', error);
          throw error;
        })
      );
  }

  /**
   * Update an existing evaluation
   */
  updateEvaluation(evaluationId: number, evaluation: CreateEvaluation): Observable<EvaluationResponse> {
    return this.http.put<EvaluationResponse>(`${this.apiUrl}/${evaluationId}`, evaluation)
      .pipe(
        tap(response => {
          console.log('Evaluation updated successfully:', response);
          this.invalidateCache(evaluation.evalueId, evaluation.typeEvaluation);
        }),
        catchError(error => {
          console.error('Failed to update evaluation:', error);
          throw error;
        })
      );
  }

  /**
   * Get evaluation by ID
   */
  getEvaluationById(evaluationId: number): Observable<EvaluationResponse> {
    return this.http.get<EvaluationResponse>(`${this.apiUrl}/${evaluationId}`)
      .pipe(
        catchError(error => {
          console.error('Failed to load evaluation:', error);
          throw error;
        })
      );
  }

  /**
   * Delete an evaluation
   */
  deleteEvaluation(evaluationId: number, evaluatorId: number): Observable<void> {
    const params = new HttpParams().set('evaluatorId', evaluatorId.toString());
    return this.http.delete<void>(`${this.apiUrl}/${evaluationId}`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to delete evaluation:', error);
          throw error;
        })
      );
  }

  // ===============================================
  // EVALUATION RETRIEVAL
  // ===============================================

  /**
   * Get evaluations for a session
   */
  getEvaluationsBySession(sessionId: number): Observable<EvaluationResponse[]> {
    return this.http.get<EvaluationResponse[]>(`${this.apiUrl}/session/${sessionId}`)
      .pipe(
        catchError(error => {
          console.error('Failed to load session evaluations:', error);
          return of([]);
        })
      );
  }

  /**
   * Get evaluations by evaluator
   */
  getEvaluationsByEvaluator(evaluatorId: number): Observable<EvaluationResponse[]> {
    return this.http.get<EvaluationResponse[]>(`${this.apiUrl}/evaluator/${evaluatorId}`)
      .pipe(
        catchError(error => {
          console.error('Failed to load evaluator evaluations:', error);
          return of([]);
        })
      );
  }

  /**
   * Get evaluations for a user (as evaluated)
   */
  getEvaluationsForUser(userId: number, type: EvaluationType): Observable<EvaluationResponse[]> {
    const params = new HttpParams().set('type', type);
    return this.http.get<EvaluationResponse[]>(`${this.apiUrl}/user/${userId}`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to load user evaluations:', error);
          return of([]);
        })
      );
  }

  /**
   * Get recent evaluations for a user
   */
  getRecentEvaluationsForUser(userId: number, type: EvaluationType, limit: number = 5): Observable<EvaluationResponse[]> {
    const params = new HttpParams()
      .set('type', type)
      .set('limit', limit.toString());
    
    return this.http.get<EvaluationResponse[]>(`${this.apiUrl}/user/${userId}/recent`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to load recent evaluations:', error);
          return of([]);
        })
      );
  }

  /**
   * Get evaluations with comments for a user
   */
  getEvaluationsWithComments(userId: number, type: EvaluationType): Observable<EvaluationResponse[]> {
    const params = new HttpParams().set('type', type);
    return this.http.get<EvaluationResponse[]>(`${this.apiUrl}/user/${userId}/comments`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to load evaluations with comments:', error);
          return of([]);
        })
      );
  }

  // ===============================================
  // EVALUATION SUMMARIES AND STATISTICS
  // ===============================================

  /**
   * Get evaluation summary for a user with caching
   */
  getEvaluationSummary(userId: number, type: EvaluationType): Observable<EvaluationSummary | null> {
    const cacheKey = `${userId}_${type}`;
    
    if (!this.evaluationSummaryCache.has(cacheKey)) {
      this.evaluationSummaryCache.set(cacheKey, new BehaviorSubject<EvaluationSummary | null>(null));
      this.fetchEvaluationSummary(userId, type, cacheKey);
    }
    
    return this.evaluationSummaryCache.get(cacheKey)!.asObservable();
  }

  private fetchEvaluationSummary(userId: number, type: EvaluationType, cacheKey: string): void {
    const params = new HttpParams().set('type', type);
    this.http.get<EvaluationSummary>(`${this.apiUrl}/user/${userId}/summary`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to load evaluation summary:', error);
          return of(null);
        })
      )
      .subscribe(summary => {
        this.evaluationSummaryCache.get(cacheKey)?.next(summary);
      });
  }

  /**
   * Get average rating for a user
   */
  getAverageRating(userId: number, type: EvaluationType): Observable<{ averageRating: number; evaluationCount: number }> {
    const params = new HttpParams().set('type', type);
    return this.http.get<{ averageRating: number; evaluationCount: number }>(`${this.apiUrl}/user/${userId}/average`, { params })
      .pipe(
        catchError(error => {
          console.error('Failed to load average rating:', error);
          return of({ averageRating: 0, evaluationCount: 0 });
        })
      );
  }

  /**
   * Check if evaluation exists for session
   */
  getSessionEvaluationStatus(sessionId: number): Observable<SessionEvaluationStatus> {
    return this.http.get<SessionEvaluationStatus>(`${this.apiUrl}/session/${sessionId}/status`)
      .pipe(
        catchError(error => {
          console.error('Failed to load session evaluation status:', error);
          return of({
            studentEvaluationExists: false,
            tutorEvaluationExists: false,
            evaluationComplete: false
          });
        })
      );
  }

  /**
   * Get comprehensive session feedback
   */
  getSessionFeedback(sessionId: number): Observable<SessionFeedback | null> {
    return this.http.get<SessionFeedback>(`${this.apiUrl}/session/${sessionId}/feedback`)
      .pipe(
        catchError(error => {
          console.error('Failed to load session feedback:', error);
          return of(null);
        })
      );
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  /**
   * Check if current user can evaluate a session
   */
  canEvaluateSession(sessionId: number, userId: number, userType: 'student' | 'tutor'): Observable<boolean> {
    return this.getSessionEvaluationStatus(sessionId).pipe(
      map(status => {
        if (userType === 'student') {
          return !status.studentEvaluationExists;
        } else {
          return !status.tutorEvaluationExists;
        }
      })
    );
  }

  /**
   * Invalidate cache for a user
   */
  private invalidateCache(userId: number, type: EvaluationType): void {
    const cacheKey = `${userId}_${type}`;
    if (this.evaluationSummaryCache.has(cacheKey)) {
      this.fetchEvaluationSummary(userId, type, cacheKey);
    }
  }

  /**
   * Clear all cache
   */
  clearCache(): void {
    this.evaluationSummaryCache.clear();
  }
}
