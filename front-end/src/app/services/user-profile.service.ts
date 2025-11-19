import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  UserProfile,
  StudentProfile,
  TutorProfile,
  StudentProfileUpdateRequest,
  TutorProfileUpdateRequest,
  PasswordUpdateRequest,
  PasswordUpdateResponse
} from '../models/user-profile.model';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {
  private apiUrl = `${environment.BASE_URL}/api/v1/users`;
  private tutorApiUrl = `${environment.BASE_URL}/api/tutors`;
  
  // Store current user profile
  private currentProfileSubject = new BehaviorSubject<UserProfile | StudentProfile | TutorProfile | null>(null);
  public currentProfile$ = this.currentProfileSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get user by ID
   */
  getUserById(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get user by email
   */
  getUserByEmail(email: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/email/${email}`);
  }

  /**
   * Get student profile
   */
  getStudentProfile(id: number): Observable<StudentProfile> {
    return this.http.get<StudentProfile>(`${this.apiUrl}/${id}`).pipe(
      tap(profile => this.currentProfileSubject.next(profile))
    );
  }

  /**
   * Get tutor profile
   */
  getTutorProfile(id: number): Observable<TutorProfile> {
    return this.http.get<TutorProfile>(`${this.tutorApiUrl}/${id}`).pipe(
      tap(profile => this.currentProfileSubject.next(profile))
    );
  }

  /**
   * Update student profile
   */
  updateStudentProfile(id: number, request: StudentProfileUpdateRequest): Observable<StudentProfile> {
    return this.http.put<StudentProfile>(`${this.apiUrl}/${id}`, request).pipe(
      tap(profile => this.currentProfileSubject.next(profile))
    );
  }

  /**
   * Update tutor profile
   */
  updateTutorProfile(id: number, request: TutorProfileUpdateRequest): Observable<TutorProfile> {
    return this.http.put<TutorProfile>(`${this.tutorApiUrl}/${id}`, request).pipe(
      tap(profile => this.currentProfileSubject.next(profile))
    );
  }

  /**
   * Update password
   */
  updatePassword(userId: number, request: PasswordUpdateRequest): Observable<PasswordUpdateResponse> {
    return this.http.put<PasswordUpdateResponse>(`${this.apiUrl}/${userId}/password`, request);
  }

  /**
   * Upload profile photo
   */
  uploadProfilePhoto(userId: number, file: File): Observable<{ photoUrl: string }> {
    const formData = new FormData();
    formData.append('photo', file);
    return this.http.post<{ photoUrl: string }>(`${this.apiUrl}/${userId}/photo`, formData);
  }

  /**
   * Delete profile photo
   */
  deleteProfilePhoto(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}/photo`);
  }

  /**
   * Search users
   */
  searchUsers(pattern: string): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${this.apiUrl}/search`, {
      params: { pattern }
    });
  }

  /**
   * Get all active tutors
   */
  getAllActiveTutors(): Observable<TutorProfile[]> {
    return this.http.get<TutorProfile[]>(`${this.tutorApiUrl}`);
  }

  /**
   * Get available tutors
   */
  getAvailableTutors(): Observable<TutorProfile[]> {
    return this.http.get<TutorProfile[]>(`${this.tutorApiUrl}/available`);
  }

  /**
   * Get verified tutors
   */
  getVerifiedTutors(): Observable<TutorProfile[]> {
    return this.http.get<TutorProfile[]>(`${this.tutorApiUrl}/verified`);
  }

  /**
   * Get top-rated tutors
   */
  getTopRatedTutors(limit: number = 10): Observable<TutorProfile[]> {
    return this.http.get<TutorProfile[]>(`${this.tutorApiUrl}/top-rated`, {
      params: { limit: limit.toString() }
    });
  }

  /**
   * Search tutors
   */
  searchTutors(keyword?: string): Observable<TutorProfile[]> {
    return this.http.get<TutorProfile[]>(`${this.tutorApiUrl}/search`, {
      params: keyword ? { keyword } : {}
    });
  }

  /**
   * Clear current profile
   */
  clearProfile(): void {
    this.currentProfileSubject.next(null);
  }

  /**
   * Get current profile value
   */
  getCurrentProfileValue(): UserProfile | StudentProfile | TutorProfile | null {
    return this.currentProfileSubject.value;
  }
}
