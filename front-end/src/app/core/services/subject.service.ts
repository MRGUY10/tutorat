import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { 
  Subject, 
  CreateSubjectRequest, 
  UpdateSubjectRequest,
  SubjectSearchParams,
  SubjectStatistics,
  NiveauAcademique,
  SubjectFormOptions
} from '../models/subject.model';

@Injectable({
  providedIn: 'root'
})
export class SubjectService {
  private readonly apiUrl = 'http://localhost:8080/api/matieres';
  
  // State management
  private subjectsSubject = new BehaviorSubject<Subject[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);
  
  // Public observables
  public subjects$ = this.subjectsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Get all subjects
  getAllSubjects(params?: SubjectSearchParams): Observable<Subject[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    let httpParams = new HttpParams();
    if (params?.page !== undefined) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params?.size !== undefined) {
      httpParams = httpParams.set('size', params.size.toString());
    }

    return this.http.get<Subject[]>(this.apiUrl, { params: httpParams })
      .pipe(
        tap(subjects => {
          this.subjectsSubject.next(subjects);
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          this.handleError('Failed to load subjects', error);
          return of([]);
        })
      );
  }

  // Get subject by ID
  getSubjectById(id: number): Observable<Subject> {
    this.loadingSubject.next(true);
    
    return this.http.get<Subject>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        catchError(error => {
          this.handleError(`Failed to load subject with ID ${id}`, error);
          throw error;
        })
      );
  }

  // Create new subject
  createSubject(request: CreateSubjectRequest): Observable<Subject> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.post<Subject>(this.apiUrl, request)
      .pipe(
        tap(subject => {
          const currentSubjects = this.subjectsSubject.value;
          this.subjectsSubject.next([...currentSubjects, subject]);
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          this.handleError('Failed to create subject', error);
          throw error;
        })
      );
  }

  // Update subject
  updateSubject(id: number, request: UpdateSubjectRequest): Observable<Subject> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<Subject>(`${this.apiUrl}/${id}`, request)
      .pipe(
        tap(updatedSubject => {
          const currentSubjects = this.subjectsSubject.value;
          const index = currentSubjects.findIndex(s => s.id === id);
          if (index !== -1) {
            currentSubjects[index] = updatedSubject;
            this.subjectsSubject.next([...currentSubjects]);
          }
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          this.handleError('Failed to update subject', error);
          throw error;
        })
      );
  }

  // Delete subject
  deleteSubject(id: number): Observable<void> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(() => {
          const currentSubjects = this.subjectsSubject.value;
          this.subjectsSubject.next(currentSubjects.filter(s => s.id !== id));
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          this.handleError('Failed to delete subject', error);
          throw error;
        })
      );
  }

  // Search subjects
  searchSubjects(searchTerm: string): Observable<Subject[]> {
    this.loadingSubject.next(true);
    
    const params = new HttpParams().set('searchTerm', searchTerm);
    
    return this.http.get<Subject[]>(`${this.apiUrl}/search`, { params })
      .pipe(
        tap(subjects => {
          this.subjectsSubject.next(subjects);
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          this.handleError('Failed to search subjects', error);
          return of([]);
        })
      );
  }

  // Get all domains
  getAllDomains(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/domains`)
      .pipe(
        catchError(error => {
          this.handleError('Failed to load domains', error);
          return of([]);
        })
      );
  }

  // Count subjects by domain
  countSubjectsByDomain(domain: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count/domain/${domain}`)
      .pipe(
        catchError(error => {
          this.handleError(`Failed to count subjects for domain ${domain}`, error);
          return of(0);
        })
      );
  }

  // Check if subject exists by name
  existsByName(name: string): Observable<boolean> {
    const params = new HttpParams().set('nom', name);
    
    return this.http.get<boolean>(`${this.apiUrl}/exists`, { params })
      .pipe(
        catchError(error => {
          this.handleError('Failed to check subject existence', error);
          return of(false);
        })
      );
  }

  // Get subject statistics
  getSubjectStatistics(): Observable<SubjectStatistics> {
    // This would need to be implemented on the backend or calculated from existing data
    return this.getAllSubjects().pipe(
      map(subjects => this.calculateStatistics(subjects)),
      catchError(error => {
        this.handleError('Failed to calculate statistics', error);
        return of({
          totalSubjects: 0,
          subjectsByDomain: {},
          subjectsByLevel: {}
        });
      })
    );
  }

  // Get form options
  getFormOptions(): Observable<SubjectFormOptions> {
    return this.getAllDomains().pipe(
      map(domains => ({
        niveauOptions: [
          { value: NiveauAcademique.DEBUTANT, label: 'Débutant' },
          { value: NiveauAcademique.INTERMEDIAIRE, label: 'Intermédiaire' },
          { value: NiveauAcademique.AVANCE, label: 'Avancé' }
        ],
        domaineOptions: domains
      })),
      catchError(error => {
        this.handleError('Failed to load form options', error);
        return of({
          niveauOptions: [
            { value: NiveauAcademique.DEBUTANT, label: 'Débutant' },
            { value: NiveauAcademique.INTERMEDIAIRE, label: 'Intermédiaire' },
            { value: NiveauAcademique.AVANCE, label: 'Avancé' }
          ],
          domaineOptions: ['Mathematics', 'Physics', 'Computer Science', 'Languages']
        });
      })
    );
  }

  // Load subjects (to be called from components)
  loadSubjects(params?: SubjectSearchParams): void {
    this.getAllSubjects(params).subscribe();
  }

  // Refresh subjects list
  refreshSubjects(): void {
    this.loadSubjects();
  }

  // Clear error
  clearError(): void {
    this.errorSubject.next(null);
  }

  // Private helper methods
  private handleError(message: string, error: any): void {
    console.error(message, error);
    let errorMessage = message;
    if (error?.status === 404) {
      errorMessage = 'Subject API endpoint not found (404). Please check backend configuration.';
    } else if (error?.error?.message) {
      errorMessage = error.error.message;
    } else if (error?.message) {
      errorMessage = error.message;
    }
    this.errorSubject.next(errorMessage);
    this.loadingSubject.next(false);
  }

  private calculateStatistics(subjects: Subject[]): SubjectStatistics {
    const subjectsByDomain: { [domain: string]: number } = {};
    const subjectsByLevel: { [level: string]: number } = {};

    subjects.forEach(subject => {
      // Count by domain
      if (subject.domaine) {
        subjectsByDomain[subject.domaine] = (subjectsByDomain[subject.domaine] || 0) + 1;
      }

      // Count by level
      if (subject.niveau) {
        subjectsByLevel[subject.niveau] = (subjectsByLevel[subject.niveau] || 0) + 1;
      }
    });

    return {
      totalSubjects: subjects.length,
      subjectsByDomain,
      subjectsByLevel
    };
  }
}