/**
 * TypeScript interfaces matching the backend TutorController DTOs
 * This ensures type safety when consuming the backend API
 */

// Enums matching backend
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING = 'PENDING'
}

/**
 * Tutor specialty DTO matching TutorSpecialiteDTO.java
 */
export interface TutorSpecialiteDTO {
  id: number;
  tutorId: number;
  matiereId: number;
  matiereNom: string;
  matiereDescription?: string;
}

/**
 * Complete tutor profile response from backend
 * Maps to TutorProfileResponse.java
 */
export interface TutorProfileResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  statut: UserStatus;
  dateInscription: string; // LocalDateTime as ISO string

  // Tutor-specific fields
  experience?: string;
  tarifHoraire?: number;
  diplomes?: string;
  description?: string;
  specialites?: TutorSpecialiteDTO[];

  // Rating and verification
  noteMoyenne?: number;
  nombreEvaluations?: number;
  verifie?: boolean;
  dateVerification?: string; // LocalDateTime as ISO string

  // Availability and preferences
  disponible?: boolean;
  coursEnLigne?: boolean;
  coursPresentiel?: boolean;
  ville?: string;
  pays?: string;

  // Audit fields
  createdAt?: string; // LocalDateTime as ISO string
  updatedAt?: string; // LocalDateTime as ISO string

  // Computed fields
  fullName?: string;
  availableForBooking?: boolean;
  profileCompletion?: number;
}

/**
 * Tutor summary response for lists and search results
 * Maps to TutorSummaryResponse.java
 */
export interface TutorSummaryResponse {
  id: number;
  fullName: string;
  specialite?: string;
  tarifHoraire?: number;
  noteMoyenne?: number;
  nombreEvaluations?: number;
  verifie?: boolean;
  disponible?: boolean;
  coursEnLigne?: boolean;
  coursPresentiel?: boolean;
  ville?: string;
  pays?: string;
  description?: string;
}

/**
 * Tutor search request with filters
 * Maps to TutorSearchRequest.java
 */
export interface TutorSearchRequest {
  keyword?: string;
  specialite?: string;
  ville?: string;
  pays?: string;
  
  minTarif?: number;
  maxTarif?: number;
  minRating?: number;
  minEvaluations?: number;
  
  verifiedOnly?: boolean;
  availableOnly?: boolean;
  onlineOnly?: boolean;
  inPersonOnly?: boolean;
  
  // Sorting options
  sortBy?: string; // rating, price, name, date
  sortDirection?: string; // asc, desc
  
  // Pagination
  page?: number;
  size?: number;
}

/**
 * Tutor profile update request
 * Maps to TutorProfileUpdateRequest.java
 */
export interface TutorProfileUpdateRequest {
  nom?: string;
  prenom?: string;
  telephone?: string;
  experience?: string;
  tarifHoraire?: number;
  diplomes?: string;
  description?: string;
  specialite?: string;
  ville?: string;
  pays?: string;
  coursEnLigne?: boolean;
  coursPresentiel?: boolean;
}

/**
 * Tutor statistics response
 * Maps to TutorStatisticsResponse.java
 */
export interface TutorStatisticsResponse {
  totalTutors: number;
  activeTutors: number;
  availableTutors: number;
  verifiedTutors: number;
  averageRating: number;
  popularSpecialties: string[];
  popularCities: string[];
  averageHourlyRate: number;
  tutorsWithRatings: number;
}

/**
 * Error response from backend
 */
export interface ErrorResponse {
  code: string;
  message: string;
}

/**
 * Conversion utilities between old and new models
 */

/**
 * Convert TutorProfileResponse to the existing CompleteTutorProfile format
 * for backward compatibility during migration
 */
export function convertTutorProfileToLegacy(profile: TutorProfileResponse): any {
  return {
    id: profile.id,
    nom: profile.nom,
    prenom: profile.prenom,
    email: profile.email,
    telephone: profile.telephone,
    statut: profile.statut,
    dateInscription: profile.dateInscription,
    
    experience: profile.experience,
    tarif: profile.tarifHoraire, // Note: different field name
    diplomes: profile.diplomes,
    description: profile.description,
    noteGlobale: profile.noteMoyenne, // Note: different field name
    nombreEvaluations: profile.nombreEvaluations,
    
    bio: profile.description, // Map description to bio
    locationCity: profile.ville,
    locationCountry: profile.pays,
    onlineTeaching: profile.coursEnLigne,
    inPersonTeaching: profile.coursPresentiel,
    isVerified: profile.verifie,
    verificationDate: profile.dateVerification,
    profileCompletionPercentage: profile.profileCompletion,
    
    // Default values for fields not in backend
    availabilityStatus: profile.disponible ? 'AVAILABLE' : 'OFFLINE',
    lastActive: profile.updatedAt
  };
}

/**
 * Convert TutorSummaryResponse to the existing TutorCard format
 * for backward compatibility during migration
 */
export function convertTutorSummaryToCard(summary: TutorSummaryResponse): any {
  return {
    id: summary.id,
    fullName: summary.fullName,
    specialite: summary.specialite,
    tarif: summary.tarifHoraire, // Note: different field name
    noteGlobale: summary.noteMoyenne, // Note: different field name
    nombreEvaluations: summary.nombreEvaluations,
    isVerified: summary.verifie,
    disponible: summary.disponible,
    locationCity: summary.ville,
    locationCountry: summary.pays,
    onlineTeaching: summary.coursEnLigne,
    inPersonTeaching: summary.coursPresentiel,
    description: summary.description,
    
    // Default values for fields not in backend
    profilePhotoUrl: undefined,
    availabilityStatus: summary.disponible ? 'AVAILABLE' : 'OFFLINE'
  };
}

/**
 * Convert legacy search filters to TutorSearchRequest format
 */
export function convertLegacyFiltersToSearchRequest(filters: any): TutorSearchRequest {
  return {
    keyword: filters.searchTerm,
    specialite: filters.specialty,
    ville: filters.city,
    pays: filters.country,
    minTarif: filters.minTarif,
    maxTarif: filters.maxTarif,
    minRating: filters.minRating,
    verifiedOnly: filters.verified,
    availableOnly: filters.availabilityStatus === 'AVAILABLE',
    onlineOnly: filters.onlineTeaching === true && filters.inPersonTeaching !== true,
    inPersonOnly: filters.inPersonTeaching === true && filters.onlineTeaching !== true,
    sortBy: filters.sortBy || 'rating',
    sortDirection: filters.sortDirection || 'desc',
    page: filters.page || 0,
    size: filters.size || 20
  };
}