import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { TutorService } from './tutor.service';
import { CompleteTutorProfile, TutorSearchFilters, AvailabilityStatus, UserStatus, SearchSuggestion } from '../models/tutor.model';
import { TutorSummaryResponse } from '../models/tutor-backend.model';

@Injectable({
  providedIn: 'root'
})
export class TutorSearchService {

  constructor(private tutorService: TutorService) {}

  get tutors$(): Observable<CompleteTutorProfile[]> {
    return this.tutorService.tutors$.pipe(
      map(tutors => tutors.map(t => this.convertToLegacy(t)))
    );
  }

  get loading$(): Observable<boolean> {
    return this.tutorService.loading$;
  }

  get error$(): Observable<string | null> {
    return this.tutorService.error$;
  }

  get tutorCards$(): Observable<any[]> {
    return this.tutorService.tutorCards$;
  }

  get statistics$(): Observable<any> {
    return this.tutorService.statistics$;
  }

  getAllTutors(): Observable<CompleteTutorProfile[]> {
    return this.tutorService.getAllTutors();
  }

  searchTutorsByKeywords(searchTerm: string): Observable<CompleteTutorProfile[]> {
    return this.tutorService.searchTutorsByKeyword(searchTerm).pipe(
      map(tutors => tutors.map(t => this.convertToLegacy(t)))
    );
  }

  getAvailableTutors(): Observable<CompleteTutorProfile[]> {
    return this.tutorService.getAvailableTutors().pipe(
      map(tutors => tutors.map(t => this.convertToLegacy(t)))
    );
  }

  searchTutorsWithFilters(filters: TutorSearchFilters): Observable<CompleteTutorProfile[]> {
    return this.tutorService.searchTutorsWithLegacyFilters(filters);
  }

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
        
        // Tutor specific fields
        experience: profile.experience,
        tarif: profile.tarifHoraire,
        diplomes: profile.diplomes,
        description: profile.description,
        specialites: profile.specialites || [], // Include specialties from backend
        
        // Rating and verification
        noteGlobale: profile.noteMoyenne,
        nombreEvaluations: profile.nombreEvaluations,
        isVerified: profile.verifie,
        
        // Location and availability
        locationCity: profile.ville,
        locationCountry: profile.pays,
        onlineTeaching: profile.coursEnLigne,
        inPersonTeaching: profile.coursPresentiel,
        availabilityStatus: profile.disponible ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.OFFLINE,
        
        // Computed fields
        fullName: profile.fullName || `${profile.prenom} ${profile.nom}`,
        availableForBooking: profile.availableForBooking
      } as CompleteTutorProfile))
    );
  }

  loadTutors(): void {
    this.getAllTutors().subscribe();
  }

  loadAvailableTutors(): void {
    this.getAvailableTutors().subscribe();
  }

  loadVerifiedTutors(): void {
    this.tutorService.getVerifiedTutors().subscribe();
  }

  refreshTutors(): void {
    this.loadTutors();
  }

  clearError(): void {
    // Clear error by reloading data
    this.loadTutors();
  }

  getTutorStatistics(): Observable<any> {
    return this.tutorService.getTutorStatistics();
  }

  getFormOptions(): Observable<any> {
    // Return basic form options since TutorService doesn't have this method
    return new Observable(observer => {
      observer.next({
        cities: ['Paris', 'Lyon', 'Marseille'],
        countries: ['France', 'Belgium', 'Switzerland'],
        subjects: ['Mathématiques', 'Français', 'Anglais']
      });
      observer.complete();
    });
  }

  private convertToLegacy(summary: TutorSummaryResponse): CompleteTutorProfile {
    // Convert single specialite string to specialites array if available
    const specialites: any[] = [];
    if (summary.specialite) {
      specialites.push({
        id: 0,
        tutorId: summary.id,
        matiereId: 0,
        matiereNom: summary.specialite,
        matiereDescription: ''
      });
    }

    return {
      id: summary.id,
      nom: summary.fullName?.split(' ')[1] || '',
      prenom: summary.fullName?.split(' ')[0] || '',
      email: `tutor${summary.id}@example.com`,
      statut: UserStatus.ACTIVE,
      dateInscription: new Date().toISOString(),
      
      // Basic tutor fields
      tarif: summary.tarifHoraire,
      description: summary.description,
      specialites: specialites, // Convert specialite to specialites array
      
      // Rating and verification
      noteGlobale: summary.noteMoyenne,
      nombreEvaluations: summary.nombreEvaluations,
      isVerified: summary.verifie,
      
      // Location and availability
      locationCity: summary.ville,
      locationCountry: summary.pays,
      onlineTeaching: summary.coursEnLigne,
      inPersonTeaching: summary.coursPresentiel,
      availabilityStatus: summary.disponible ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.OFFLINE,
      
      // Computed
      fullName: summary.fullName
    } as CompleteTutorProfile;
  }

  // ===== SEARCH SUGGESTIONS =====
  
  /**
   * Get search suggestions based on the query
   */
  getSearchSuggestions(query: string): Observable<SearchSuggestion[]> {
    if (!query || query.trim().length < 2) {
      return of([]);
    }

    const searchTerm = query.trim().toLowerCase();
    
    return this.tutorService.tutors$.pipe(
      map(tutors => {
        const suggestions: SearchSuggestion[] = [];
        const uniqueSubjects = new Set<string>();
        const uniqueLocations = new Set<string>();

        // Get tutor suggestions
        tutors
          .filter(tutor => 
            tutor.fullName?.toLowerCase().includes(searchTerm)
          )
          .slice(0, 3) // Limit to 3 tutor suggestions
          .forEach(tutor => {
            suggestions.push({
              type: 'tutor',
              id: tutor.id?.toString() || '',
              label: tutor.fullName || '',
              subtitle: tutor.description && tutor.description.length > 0 ? 
                tutor.description.substring(0, 60) + (tutor.description.length > 60 ? '...' : '') :
                tutor.specialite || 'Tutor',
              icon: 'user',
              data: tutor
            });
          });

        // Get subject suggestions
        tutors.forEach(tutor => {
          if (tutor.specialite && tutor.specialite.toLowerCase().includes(searchTerm)) {
            uniqueSubjects.add(tutor.specialite);
          }
        });

        Array.from(uniqueSubjects)
          .slice(0, 3) // Limit to 3 subject suggestions
          .forEach(subject => {
            suggestions.push({
              type: 'subject',
              id: subject,
              label: subject,
              subtitle: 'Subject',
              icon: 'book',
              data: { subject }
            });
          });

        // Get location suggestions  
        tutors.forEach(tutor => {
          if (tutor.ville && tutor.ville.toLowerCase().includes(searchTerm)) {
            uniqueLocations.add(tutor.ville);
          }
          if (tutor.pays && tutor.pays.toLowerCase().includes(searchTerm)) {
            uniqueLocations.add(tutor.pays);
          }
        });

        Array.from(uniqueLocations)
          .slice(0, 2) // Limit to 2 location suggestions
          .forEach(location => {
            suggestions.push({
              type: 'location',
              id: location,
              label: location,
              subtitle: 'Location',
              icon: 'location',
              data: { location }
            });
          });

        return suggestions.slice(0, 8); // Total limit of 8 suggestions
      })
    );
  }
}
