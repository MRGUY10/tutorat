import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, of, forkJoin, throwError } from 'rxjs';
import { map, catchError, tap, shareReplay, switchMap } from 'rxjs/operators';
import { TutorService } from './tutor.service';
import { SubjectService } from './subject.service';
import { UserManagementService } from './user-management.service';
import {
  SessionResponse,
  SessionDetails,
  CreateSession,
  UpdateSession,
  SessionRequestResponse,
  CreateSessionRequest,
  UpdateSessionRequest,
  SessionRequestDetails,
  SessionCard,
  SessionRequestCard,
  SessionFilters,
  SessionRequestFilters,
  SessionStatistics,
  SessionRequestStatistics,
  SessionStatus,
  RequestStatus,
  SessionType,
  Urgence,
  sessionToCard,
  sessionRequestToCard
} from '../core/models/session.model';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private readonly apiUrl = 'http://localhost:8080/api';
  private readonly sessionEndpoint = `${this.apiUrl}/sessions`;
  private readonly requestEndpoint = `${this.apiUrl}/session-requests`;

  // Session state management
  private sessionsSubject = new BehaviorSubject<SessionResponse[]>([]);
  private sessionCardsSubject = new BehaviorSubject<SessionCard[]>([]);
  private sessionsLoadingSubject = new BehaviorSubject<boolean>(false);
  private sessionsErrorSubject = new BehaviorSubject<string | null>(null);

  // Session request state management
  private requestsSubject = new BehaviorSubject<SessionRequestResponse[]>([]);
  private requestDetailsSubject = new BehaviorSubject<SessionRequestDetails[]>([]);
  private requestCardsSubject = new BehaviorSubject<SessionRequestCard[]>([]);
  private requestsLoadingSubject = new BehaviorSubject<boolean>(false);
  private requestsErrorSubject = new BehaviorSubject<string | null>(null);

  // Public observables
  public readonly sessions$ = this.sessionsSubject.asObservable();
  public readonly sessionCards$ = this.sessionCardsSubject.asObservable();
  public readonly sessionsLoading$ = this.sessionsLoadingSubject.asObservable();
  public readonly sessionsError$ = this.sessionsErrorSubject.asObservable();

  public readonly requests$ = this.requestsSubject.asObservable();
  public readonly requestDetails$ = this.requestDetailsSubject.asObservable();
  public readonly requestCards$ = this.requestCardsSubject.asObservable();
  public readonly requestsLoading$ = this.requestsLoadingSubject.asObservable();
  public readonly requestsError$ = this.requestsErrorSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tutorService: TutorService,
    private subjectService: SubjectService,
    private userManagementService: UserManagementService
  ) {}

  // ===============================================
  // SESSION MANAGEMENT
  // ===============================================

  /**
   * Create a new session
   */
  createSession(createData: CreateSession): Observable<SessionResponse> {
    return this.http.post<SessionResponse>(this.sessionEndpoint, createData)
      .pipe(
        tap(session => {
          const currentSessions = this.sessionsSubject.value;
          this.sessionsSubject.next([...currentSessions, session]);
          this.refreshSessionCards();
        }),
        catchError(error => {
          this.handleSessionError('Failed to create session', error);
          throw error;
        })
      );
  }

  /**
   * Get session by ID
   */
  getSessionById(sessionId: number): Observable<SessionResponse> {
    return this.http.get<SessionResponse>(`${this.sessionEndpoint}/${sessionId}`)
      .pipe(
        catchError(error => {
          this.handleSessionError('Failed to load session', error);
          throw error;
        })
      );
  }

  /**
   * Get detailed session information with participant and subject names
   */
  getSessionDetails(sessionId: number): Observable<SessionDetails> {
    return this.getSessionById(sessionId).pipe(
      switchMap(session => {
        // Get additional details in parallel
        const tutor$ = this.getTutorInfo(session.tuteurId);
        const student$ = this.getStudentInfo(session.etudiantId);  
        const subject$ = this.getSubjectInfo(session.matiereId);

        return forkJoin({
          tutor: tutor$,
          student: student$,
          subject: subject$
        }).pipe(
          map(({ tutor, student, subject }) => {
            // Combine session with additional details
            const tutorFullName = `${tutor.prenom || ''} ${tutor.nom || ''}`.trim();
            const studentFullName = `${student.prenom || ''} ${student.nom || ''}`.trim();
            
            const sessionDetails: SessionDetails = {
              ...session,
              etudiantNom: student.nom || 'Inconnu',
              etudiantPrenom: student.prenom || '', 
              etudiantEmail: student.email || 'N/A',
              etudiantTelephone: student.telephone || '',
              tuteurNom: tutor.nom || 'Inconnu',
              tuteurPrenom: tutor.prenom || '',
              tuteurEmail: tutor.email || 'N/A',
              tuteurTelephone: tutor.telephone || '',
              tuteurSpecialite: tutor.specialite || 'Non spécifiée',
              tuteurTarifHoraire: tutor.tarifHoraire || 0,
              tuteurVerified: tutor.verified || false,
              matiereNom: subject.nom || 'Matière inconnue',
              participantNames: {
                tutorName: tutor.fullName || tutorFullName || 'Tuteur inconnu',
                studentName: studentFullName || 'Étudiant inconnu',
                subjectName: subject.nom || 'Matière inconnue'
              }
            };
            return sessionDetails;
          })
        );
      }),
      catchError(error => {
        console.error('Failed to get session details:', error);
        throw error;
      })
    );
  }

  /**
   * Update session
   */
  updateSession(sessionId: number, updateData: UpdateSession): Observable<SessionResponse> {
    return this.http.put<SessionResponse>(`${this.sessionEndpoint}/${sessionId}`, updateData)
      .pipe(
        tap(updatedSession => {
          const currentSessions = this.sessionsSubject.value;
          const index = currentSessions.findIndex(s => s.id === sessionId);
          if (index !== -1) {
            currentSessions[index] = updatedSession;
            this.sessionsSubject.next([...currentSessions]);
            this.refreshSessionCards();
          }
        }),
        catchError(error => {
          this.handleSessionError('Failed to update session', error);
          throw error;
        })
      );
  }

  /**
   * Start session
   */
  startSession(sessionId: number): Observable<SessionResponse> {
    return this.http.post<SessionResponse>(`${this.sessionEndpoint}/${sessionId}/start`, {})
      .pipe(
        tap(updatedSession => this.updateSessionInState(updatedSession)),
        catchError(error => {
          this.handleSessionError('Failed to start session', error);
          throw error;
        })
      );
  }

  /**
   * Complete session
   */
  completeSession(sessionId: number): Observable<SessionResponse> {
    return this.http.post<SessionResponse>(`${this.sessionEndpoint}/${sessionId}/complete`, {})
      .pipe(
        tap(updatedSession => this.updateSessionInState(updatedSession)),
        catchError(error => {
          this.handleSessionError('Failed to complete session', error);
          throw error;
        })
      );
  }

  /**
   * Cancel session
   */
  cancelSession(sessionId: number): Observable<SessionResponse> {
    const updateData: UpdateSession = { statut: SessionStatus.ANNULEE };
    return this.updateSession(sessionId, updateData);
  }

  /**
   * Get sessions by tutor
   */
  getSessionsByTutor(tutorId: number, filters?: SessionFilters): Observable<SessionResponse[]> {
    // Validate tutorId
    if (!tutorId || tutorId <= 0) {
      console.warn('SessionService: Invalid tutorId provided:', tutorId);
      this.sessionsLoadingSubject.next(false);
      return of([]);
    }

    this.sessionsLoadingSubject.next(true);
    let params = new HttpParams();
    
    if (filters) {
      params = this.buildSessionParams(params, filters);
    }

    return this.http.get<SessionResponse[]>(`${this.sessionEndpoint}/tutor/${tutorId}`, { params })
      .pipe(
        tap(sessions => {
          this.sessionsSubject.next(sessions);
          this.refreshSessionCards();
          this.sessionsLoadingSubject.next(false);
        }),
        catchError(error => {
          this.handleSessionError('Failed to load tutor sessions', error);
          return of([]);
        }),
        shareReplay(1)
      );
  }

  /**
   * Get sessions by student
   */
  getSessionsByStudent(studentId: number, filters?: SessionFilters): Observable<SessionResponse[]> {
    // Validate studentId
    if (!studentId || studentId <= 0) {
      console.warn('SessionService: Invalid studentId provided:', studentId);
      this.sessionsLoadingSubject.next(false);
      return of([]);
    }

    this.sessionsLoadingSubject.next(true);
    let params = new HttpParams();
    
    if (filters) {
      params = this.buildSessionParams(params, filters);
    }

    return this.http.get<SessionResponse[]>(`${this.sessionEndpoint}/student/${studentId}`, { params })
      .pipe(
        tap(sessions => {
          this.sessionsSubject.next(sessions);
          this.refreshSessionCards();
          this.sessionsLoadingSubject.next(false);
        }),
        catchError(error => {
          this.handleSessionError('Failed to load student sessions', error);
          return of([]);
        }),
        shareReplay(1)
      );
  }

  /**
   * Get upcoming sessions
   */
  getUpcomingSessions(userId: number, userType: 'tutor' | 'student'): Observable<SessionResponse[]> {
    // Validate userId
    if (!userId || userId <= 0) {
      console.warn('SessionService: Invalid userId provided for upcoming sessions:', userId);
      return of([]);
    }

    const endpoint = userType === 'tutor' ? 
      `${this.sessionEndpoint}/tutor/${userId}/upcoming` : 
      `${this.sessionEndpoint}/student/${userId}/upcoming`;

    return this.http.get<SessionResponse[]>(endpoint)
      .pipe(
        catchError(error => {
          this.handleSessionError('Failed to load upcoming sessions', error);
          return of([]);
        })
      );
  }

  // ===============================================
  // SESSION REQUEST MANAGEMENT
  // ===============================================

  /**
   * Create session request
   */
  createSessionRequest(studentId: number, requestData: CreateSessionRequest): Observable<SessionRequestResponse> {
    // Validate studentId
    if (!studentId || studentId <= 0) {
      console.error('SessionService: Invalid studentId provided for creating session request:', studentId);
      return throwError(() => new Error('Invalid student ID provided'));
    }

    return this.http.post<SessionRequestResponse>(`${this.requestEndpoint}?etudiantId=${studentId}`, requestData)
      .pipe(
        tap(request => {
          const currentRequests = this.requestsSubject.value;
          this.requestsSubject.next([...currentRequests, request]);
        }),
        catchError(error => {
          this.handleRequestError('Failed to create session request', error);
          throw error;
        })
      );
  }

  /**
   * Update session request
   */
  updateSessionRequest(requestId: number, updateData: UpdateSessionRequest): Observable<SessionRequestResponse> {
    return this.http.put<SessionRequestResponse>(`${this.requestEndpoint}/${requestId}`, updateData)
      .pipe(
        tap(updatedRequest => {
          const currentRequests = this.requestsSubject.value;
          const index = currentRequests.findIndex(r => r.id === requestId);
          if (index !== -1) {
            currentRequests[index] = updatedRequest;
            this.requestsSubject.next([...currentRequests]);
          }
        }),
        catchError(error => {
          this.handleRequestError('Failed to update session request', error);
          throw error;
        })
      );
  }

  /**
   * Accept session request
   */
  acceptSessionRequest(requestId: number, message?: string): Observable<SessionRequestResponse> {
    let params = new HttpParams();
    if (message) {
      params = params.set('message', message);
    }

    return this.http.post<SessionRequestResponse>(`${this.requestEndpoint}/${requestId}/accept`, {}, { params })
      .pipe(
        tap(updatedRequest => this.updateRequestInState(updatedRequest)),
        catchError(error => {
          this.handleRequestError('Failed to accept session request', error);
          throw error;
        })
      );
  }

  /**
   * Reject session request
   */
  rejectSessionRequest(requestId: number, reason?: string): Observable<SessionRequestResponse> {
    let params = new HttpParams();
    if (reason) {
      params = params.set('reason', reason);
    }

    return this.http.post<SessionRequestResponse>(`${this.requestEndpoint}/${requestId}/reject`, {}, { params })
      .pipe(
        tap(updatedRequest => this.updateRequestInState(updatedRequest)),
        catchError(error => {
          this.handleRequestError('Failed to reject session request', error);
          throw error;
        })
      );
  }

  /**
   * Get session request by ID
   */
  getSessionRequestById(requestId: number): Observable<SessionRequestResponse> {
    return this.http.get<SessionRequestResponse>(`${this.requestEndpoint}/${requestId}`)
      .pipe(
        catchError(error => {
          this.handleRequestError('Failed to load session request', error);
          throw error;
        })
      );
  }

  /**
   * Get detailed session request by ID with enriched user and subject data
   */
  getDetailedSessionRequestById(requestId: number): Observable<SessionRequestDetails> {
    return this.http.get<SessionRequestDetails>(`${this.requestEndpoint}/${requestId}/details`)
      .pipe(
        switchMap(request => {
          // Fetch student, tutor, and subject information in parallel
          const student$ = this.userManagementService.getUserById(request.etudiantId).pipe(
            catchError(() => of(null))
          );
          const tutor$ = request.tuteurId ? this.userManagementService.getUserById(request.tuteurId).pipe(
            catchError(() => of(null))
          ) : of(null);
          const subject$ = this.subjectService.getSubjectById(request.matiereId).pipe(
            catchError(() => of(null))
          );

          return forkJoin({
            student: student$,
            tutor: tutor$,
            subject: subject$
          }).pipe(
            map(({ student, tutor, subject }) => {
              // Enrich the request with fetched data
              if (student) {
                request.etudiantNom = student.nom || null;
                request.etudiantPrenom = student.prenom || null;
                request.etudiantEmail = student.email || null;
                request.etudiantTelephone = student.telephone || null;
                request.etudiantStatus = (student as any).statut || (student as any).status || null;
              }

              if (tutor) {
                request.tuteurNom = tutor.nom || null;
                request.tuteurPrenom = tutor.prenom || null;
                request.tuteurEmail = tutor.email || null;
                request.tuteurTelephone = tutor.telephone || null;
                request.tuteurStatus = (tutor as any).statut || (tutor as any).status || null;
              }

              if (subject) {
                request.matiereNom = subject.nom || null;
                request.matiereDescription = subject.description || null;
              }

              return request;
            })
          );
        }),
        catchError(error => {
          this.handleRequestError('Failed to load detailed session request', error);
          throw error;
        })
      );
  }

  /**
   * Get session requests by student
   */
  getSessionRequestsByStudent(studentId: number, filters?: SessionRequestFilters): Observable<SessionRequestDetails[]> {
    // Validate studentId
    if (!studentId || studentId <= 0) {
      console.warn('SessionService: Invalid studentId provided for session requests:', studentId);
      this.requestsLoadingSubject.next(false);
      return of([]);
    }

    this.requestsLoadingSubject.next(true);
    let params = new HttpParams();
    
    if (filters) {
      params = this.buildRequestParams(params, filters);
    }

    return this.http.get<SessionRequestDetails[]>(`${this.requestEndpoint}/student/${studentId}`, { params })
      .pipe(
        tap(requests => {
          this.requestDetailsSubject.next(requests);
          this.refreshRequestCards();
          this.requestsLoadingSubject.next(false);
        }),
        catchError(error => {
          this.handleRequestError('Failed to load student session requests', error);
          return of([]);
        }),
        shareReplay(1)
      );
  }

  /**
   * Get session requests by tutor
   */
  getSessionRequestsByTutor(tutorId: number, filters?: SessionRequestFilters): Observable<SessionRequestDetails[]> {
    this.requestsLoadingSubject.next(true);
    let params = new HttpParams();
    
    if (filters) {
      params = this.buildRequestParams(params, filters);
    }

    return this.http.get<SessionRequestDetails[]>(`${this.requestEndpoint}/tutor/${tutorId}`, { params })
      .pipe(
        tap(requests => {
          this.requestDetailsSubject.next(requests);
          this.refreshRequestCards();
          this.requestsLoadingSubject.next(false);
        }),
        catchError(error => {
          this.handleRequestError('Failed to load tutor session requests', error);
          return of([]);
        }),
        shareReplay(1)
      );
  }

  /**
   * Get pending session requests
   */
  getPendingSessionRequests(tutorId?: number, studentId?: number): Observable<SessionRequestDetails[]> {
    let params = new HttpParams();
    if (tutorId) params = params.set('tuteurId', tutorId.toString());
    if (studentId) params = params.set('etudiantId', studentId.toString());

    return this.http.get<SessionRequestDetails[]>(`${this.requestEndpoint}/pending`, { params })
      .pipe(
        catchError(error => {
          this.handleRequestError('Failed to load pending session requests', error);
          return of([]);
        })
      );
  }

  /**
   * Search session requests
   */
  searchSessionRequests(searchTerm: string, status?: RequestStatus): Observable<SessionRequestDetails[]> {
    let params = new HttpParams().set('searchTerm', searchTerm);
    if (status) params = params.set('statut', status);

    return this.http.get<SessionRequestDetails[]>(`${this.requestEndpoint}/search`, { params })
      .pipe(
        catchError(error => {
          this.handleRequestError('Failed to search session requests', error);
          return of([]);
        })
      );
  }

  // ===============================================
  // STATISTICS AND ANALYTICS
  // ===============================================

  /**
   * Get session statistics
   */
  getSessionStatistics(userId: number, userType: 'tutor' | 'student'): Observable<SessionStatistics> {
    // This would need to be implemented in the backend
    // For now, we'll calculate from current sessions
    return this.sessions$.pipe(
      map(sessions => this.calculateSessionStatistics(sessions))
    );
  }

  /**
   * Get session request statistics
   */
  getSessionRequestStatistics(userId: number, userType: 'tutor' | 'student'): Observable<SessionRequestStatistics> {
    // This would need to be implemented in the backend
    // For now, we'll calculate from current requests
    return this.requestDetails$.pipe(
      map(requests => this.calculateRequestStatistics(requests))
    );
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  /**
   * Create session from accepted request
   */
  createSessionFromRequest(requestId: number, sessionData: CreateSession): Observable<SessionResponse> {
    sessionData.demandeSessionId = requestId;
    return this.createSession(sessionData);
  }

  /**
   * Check if user can join session
   */
  canJoinSession(session: SessionResponse): boolean {
    const sessionDate = new Date(session.dateHeure);
    const now = new Date();
    const timeDifference = Math.abs(sessionDate.getTime() - now.getTime());
    const fifteenMinutes = 15 * 60 * 1000;

    return session.statut === SessionStatus.CONFIRMEE && 
           sessionDate > now && 
           timeDifference <= fifteenMinutes;
  }

  /**
   * Get session join URL
   */
  getSessionJoinUrl(session: SessionResponse): string | null {
    if (session.typeSession === SessionType.EN_LIGNE && session.lienVisio) {
      return session.lienVisio;
    }
    return null;
  }

  // ===============================================
  // PRIVATE HELPER METHODS
  // ===============================================

  private updateSessionInState(session: SessionResponse): void {
    const currentSessions = this.sessionsSubject.value;
    const index = currentSessions.findIndex(s => s.id === session.id);
    if (index !== -1) {
      currentSessions[index] = session;
      this.sessionsSubject.next([...currentSessions]);
      this.refreshSessionCards();
    }
  }

  private updateRequestInState(request: SessionRequestResponse): void {
    const currentRequests = this.requestsSubject.value;
    const index = currentRequests.findIndex(r => r.id === request.id);
    if (index !== -1) {
      currentRequests[index] = request;
      this.requestsSubject.next([...currentRequests]);
    }
  }

  private refreshSessionCards(): void {
    const sessions = this.sessionsSubject.value;
    // In a real app, you'd need to fetch user and subject names
    // For now, we'll use placeholder names
    const cards = sessions.map(session => 
      sessionToCard(session, 'Student Name', 'Tutor Name', 'Subject Name')
    );
    this.sessionCardsSubject.next(cards);
  }

  private refreshRequestCards(): void {
    const requests = this.requestDetailsSubject.value;
    const cards = requests.map(request => sessionRequestToCard(request));
    this.requestCardsSubject.next(cards);
  }

  private buildSessionParams(params: HttpParams, filters: SessionFilters): HttpParams {
    if (filters.status) params = params.set('status', filters.status);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.startDate) params = params.set('startDate', filters.startDate.toISOString());
    if (filters.endDate) params = params.set('endDate', filters.endDate.toISOString());
    if (filters.subjectId) params = params.set('subjectId', filters.subjectId.toString());
    if (filters.minPrice) params = params.set('minPrice', filters.minPrice.toString());
    if (filters.maxPrice) params = params.set('maxPrice', filters.maxPrice.toString());
    if (filters.searchTerm) params = params.set('searchTerm', filters.searchTerm);

    return params;
  }

  private buildRequestParams(params: HttpParams, filters: SessionRequestFilters): HttpParams {
    if (filters.status) params = params.set('status', filters.status);
    if (filters.urgency) params = params.set('urgency', filters.urgency);
    if (filters.startDate) params = params.set('startDate', filters.startDate.toISOString());
    if (filters.endDate) params = params.set('endDate', filters.endDate.toISOString());
    if (filters.subjectId) params = params.set('subjectId', filters.subjectId.toString());
    if (filters.minBudget) params = params.set('minBudget', filters.minBudget.toString());
    if (filters.maxBudget) params = params.set('maxBudget', filters.maxBudget.toString());
    if (filters.searchTerm) params = params.set('searchTerm', filters.searchTerm);

    return params;
  }

  private calculateSessionStatistics(sessions: SessionResponse[]): SessionStatistics {
    const now = new Date();
    const sessionsByStatus: Record<SessionStatus, number> = {
      [SessionStatus.DEMANDEE]: 0,
      [SessionStatus.CONFIRMEE]: 0,
      [SessionStatus.EN_COURS]: 0,
      [SessionStatus.TERMINEE]: 0,
      [SessionStatus.ANNULEE]: 0
    };

    const sessionsByType: Record<SessionType, number> = {
      [SessionType.EN_LIGNE]: 0,
      [SessionType.PRESENTIEL]: 0
    };

    let upcomingSessions = 0;
    let completedSessions = 0;
    let cancelledSessions = 0;
    let totalEarnings = 0;

    sessions.forEach(session => {
      sessionsByStatus[session.statut]++;
      sessionsByType[session.typeSession]++;

      const sessionDate = new Date(session.dateHeure);
      if (sessionDate > now && (session.statut === SessionStatus.CONFIRMEE || session.statut === SessionStatus.DEMANDEE)) {
        upcomingSessions++;
      }

      if (session.statut === SessionStatus.TERMINEE) {
        completedSessions++;
        totalEarnings += session.prix;
      }

      if (session.statut === SessionStatus.ANNULEE) {
        cancelledSessions++;
      }
    });

    // Generate monthly counts (simplified)
    const monthlySessionCounts = this.generateMonthlyCounts(sessions);

    return {
      totalSessions: sessions.length,
      upcomingSessions,
      completedSessions,
      cancelledSessions,
      averageRating: 4.5, // Would need to be calculated from reviews
      totalEarnings,
      sessionsByStatus,
      sessionsByType,
      monthlySessionCounts
    };
  }

  private calculateRequestStatistics(requests: SessionRequestDetails[]): SessionRequestStatistics {
    const requestsByStatus: Record<RequestStatus, number> = {
      [RequestStatus.EN_ATTENTE]: 0,
      [RequestStatus.ACCEPTEE]: 0,
      [RequestStatus.REFUSEE]: 0
    };

    const requestsByUrgency: Record<Urgence, number> = {
      [Urgence.FAIBLE]: 0,
      [Urgence.MOYENNE]: 0,
      [Urgence.HAUTE]: 0
    };

    let pendingRequests = 0;
    let acceptedRequests = 0;
    let rejectedRequests = 0;

    requests.forEach(request => {
      requestsByStatus[request.statut]++;
      requestsByUrgency[request.urgence]++;

      switch (request.statut) {
        case RequestStatus.EN_ATTENTE:
          pendingRequests++;
          break;
        case RequestStatus.ACCEPTEE:
          acceptedRequests++;
          break;
        case RequestStatus.REFUSEE:
          rejectedRequests++;
          break;
      }
    });

    const monthlyRequestCounts = this.generateMonthlyRequestCounts(requests);

    return {
      totalRequests: requests.length,
      pendingRequests,
      acceptedRequests,
      rejectedRequests,
      averageResponseTime: 2.5, // Would need to be calculated from actual response times
      requestsByUrgency,
      requestsByStatus,
      monthlyRequestCounts
    };
  }

  private generateMonthlyCounts(sessions: SessionResponse[]): { month: string; count: number }[] {
    // Simplified implementation - in reality, you'd want to group by actual months
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months.map(month => ({ month, count: Math.floor(Math.random() * 10) + 1 }));
  }

  private generateMonthlyRequestCounts(requests: SessionRequestDetails[]): { month: string; count: number }[] {
    // Simplified implementation - in reality, you'd want to group by actual months
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months.map(month => ({ month, count: Math.floor(Math.random() * 5) + 1 }));
  }

  private handleSessionError(message: string, error: any): void {
    console.error(message, error);
    this.sessionsErrorSubject.next(message);
    this.sessionsLoadingSubject.next(false);
  }

  private handleRequestError(message: string, error: any): void {
    console.error(message, error);
    this.requestsErrorSubject.next(message);
    this.requestsLoadingSubject.next(false);
  }

  // ===============================================
  // HELPER METHODS FOR DETAILED SESSIONS
  // ===============================================

  /**
   * Get tutor information by ID using TutorService
   */
  private getTutorInfo(tutorId: number): Observable<any> {
    return this.tutorService.getTutorProfile(tutorId)
      .pipe(
        map(tutor => ({
          id: tutor.id || tutorId,
          nom: tutor.nom || 'Inconnu',
          prenom: tutor.prenom || '',
          email: tutor.email || 'N/A',
          telephone: tutor.telephone || '',
          specialite: tutor.specialites?.map((s: any) => s.matiereNom).join(', ') || 'Non spécifiée',
          tarifHoraire: tutor.tarifHoraire || 0,
          verified: tutor.verifie || false,
          fullName: tutor.fullName || `${tutor.prenom || ''} ${tutor.nom || ''}`.trim() || 'Tuteur inconnu',
          experience: tutor.experience || '',
          diplomes: tutor.diplomes || '',
          description: tutor.description || '',
          disponible: tutor.disponible || false,
          coursEnLigne: tutor.coursEnLigne || false,
          coursPresentiel: tutor.coursPresentiel || false,
          ville: tutor.ville || '',
          pays: tutor.pays || ''
        })),
        catchError(error => {
          console.warn('Failed to load tutor info for ID:', tutorId, error);
          return of({
            id: tutorId,
            nom: 'Tuteur',
            prenom: 'Inconnu',
            email: 'N/A',
            telephone: '',
            specialite: 'Non spécifiée',
            tarifHoraire: 0,
            verified: false,
            fullName: 'Tuteur inconnu'
          });
        })
      );
  }

  /**
   * Get student information by ID  
   */
  private getStudentInfo(studentId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/v1/users/${studentId}`)
      .pipe(
        map(student => ({
          id: student.id || studentId,
          nom: student.nom || 'Inconnu',
          prenom: student.prenom || '',
          email: student.email || 'N/A',
          telephone: student.telephone || ''
        })),
        catchError(error => {
          console.warn('Failed to load student info for ID:', studentId, error);
          return of({
            id: studentId,
            nom: 'Étudiant',
            prenom: 'Inconnu',
            email: 'N/A',
            telephone: ''
          });
        })
      );
  }

  /**
   * Get subject information by ID using SubjectService
   */
  private getSubjectInfo(subjectId: number): Observable<any> {
    return this.subjectService.getSubjectById(subjectId)
      .pipe(
        map(subject => ({
          id: subject.id || subjectId,
          nom: subject.nom || 'Matière inconnue',
          description: subject.description || 'Description non disponible'
        })),
        catchError(error => {
          console.warn('Failed to load subject info for ID:', subjectId, error);
          return of({
            id: subjectId,
            nom: 'Matière inconnue',
            description: 'Description non disponible'
          });
        })
      );
  }
}