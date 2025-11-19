// Enums
export enum AvailabilityStatus {
  AVAILABLE = 'AVAILABLE',
  BUSY = 'BUSY',
  OFFLINE = 'OFFLINE'
}

export enum AgeGroup {
  ENFANTS = 'ENFANTS',           // Children (5-12)
  ADOLESCENTS = 'ADOLESCENTS',   // Teenagers (13-17)
  JEUNES_ADULTES = 'JEUNES_ADULTES', // Young adults (18-25)
  ADULTES = 'ADULTES',           // Adults (26-60)
  SENIORS = 'SENIORS'            // Seniors (60+)
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING = 'PENDING'
}

// Tutor Specialty Interface (matching TutorSpecialiteDTO)
export interface TutorSpecialty {
  id: number;
  tutorId: number;
  matiereId: number;
  matiereNom: string;
  matiereDescription?: string;
}

// Main Tutor Profile Interface (matching TutorProfileResponse)
export interface TutorProfile {
  // Personal information
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  statut: UserStatus;
  dateInscription: string;

  // Tutor specific information
  experience?: string;
  tarifHoraire?: number;
  diplomes?: string;
  description?: string;
  specialites?: TutorSpecialty[];

  // Rating and verification
  noteMoyenne?: number;
  nombreEvaluations?: number;
  verifie?: boolean;
  dateVerification?: string;

  // Availability and preferences
  disponible?: boolean;
  coursEnLigne?: boolean;
  coursPresentiel?: boolean;
  ville?: string;
  pays?: string;

  // Audit fields
  createdAt?: string;
  updatedAt?: string;

  // Computed fields
  fullName?: string;
  availableForBooking?: boolean;
  profileCompletion?: number;
}

// Legacy interface for backward compatibility
export interface CompleteTutorProfile extends TutorProfile {
  // Legacy fields mapped to new structure
  tarif?: number; // Maps to tarifHoraire
  noteGlobale?: number; // Maps to noteMoyenne
  
  // Enhanced profile fields
  bio?: string;
  profilePhotoUrl?: string;
  teachingMethodology?: string;
  languagesSpoken?: string;
  locationCity?: string;
  locationCountry?: string;
  availabilityStatus?: AvailabilityStatus;
  onlineTeaching?: boolean;
  inPersonTeaching?: boolean;
  preferredAgeGroup?: AgeGroup;
  responseTimeHours?: number;
  socialMediaLinks?: string;
  certifications?: string;
  awards?: string;
  teachingSince?: string;
  minSessionDuration?: number;
  maxSessionDuration?: number;
  profileCompletionPercentage?: number;
  isVerified?: boolean;
  verificationDate?: string;
  lastActive?: string;
}

// Tutor Search Filters
export interface TutorSearchFilters {
  minTarif?: number;
  maxTarif?: number;
  minRating?: number;
  availabilityStatus?: AvailabilityStatus;
  verified?: boolean;
  city?: string;
  country?: string;
  language?: string;
  preferredAgeGroup?: AgeGroup;
  onlineTeaching?: boolean;
  inPersonTeaching?: boolean;
  searchTerm?: string;
  minProfileCompletion?: number;
  maxResponseHours?: number;
}

// Search Parameters for API calls
export interface TutorSearchParams {
  searchTerm?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Form Options for dropdowns
export interface TutorSearchFormOptions {
  availabilityOptions: { value: AvailabilityStatus; label: string }[];
  ageGroupOptions: { value: AgeGroup; label: string }[];
  cities: string[];
  countries: string[];
  languages: string[];
}

// Tutor Statistics
export interface TutorStatistics {
  totalTutors: number;
  availableTutors: number;
  verifiedTutors: number;
  averageRating: number;
  tutorsByCity: { [city: string]: number };
  tutorsByCountry: { [country: string]: number };
  tutorsByAgeGroup: { [ageGroup: string]: number };
  tutorsByAvailability: { [status: string]: number };
}

// Tutor Review Interface
export interface TutorReview {
  id: number;
  tuteurId: number;
  etudiantId: number;
  sessionId?: number;
  rating: number;
  comment?: string;
  isVerified: boolean;
  isHelpful?: boolean;
  helpfulCount: number;
  createdAt: string;
  updatedAt?: string;
  
  // Populated student info
  studentName?: string;
  studentInitials?: string;
}

// Create Review Request
export interface CreateReviewRequest {
  tuteurId: number;
  etudiantId: number;
  sessionId?: number;
  rating: number;
  comment?: string;
}

// Tutor Card Display Data (simplified for UI)
export interface TutorCard {
  id: number;
  name: string;
  profileImage?: string;  // Alternative name for profilePhotoUrl
  profilePhotoUrl?: string;
  rating: number;
  reviewCount: number;
  averageRating?: number;  // Alternative name for rating
  totalRatings?: number;   // Alternative name for reviewCount
  tarif?: number;
  location?: string;
  languages?: string[];
  subjects?: string[];
  isVerified: boolean;
  isOnline: boolean;
  responseTime?: number;
  shortDescription?: string;
  experienceYears?: number;  // Years of experience
}

// Helper function to get availability status display
export function getAvailabilityStatusDisplay(status: AvailabilityStatus): string {
  switch (status) {
    case AvailabilityStatus.AVAILABLE:
      return 'Disponible';
    case AvailabilityStatus.BUSY:
      return 'Occupé';
    case AvailabilityStatus.OFFLINE:
      return 'Hors ligne';
    default:
      return 'Inconnu';
  }
}

// Helper function to get age group display
export function getAgeGroupDisplay(ageGroup: AgeGroup): string {
  switch (ageGroup) {
    case AgeGroup.ENFANTS:
      return 'Enfants (5-12 ans)';
    case AgeGroup.ADOLESCENTS:
      return 'Adolescents (13-17 ans)';
    case AgeGroup.JEUNES_ADULTES:
      return 'Jeunes Adultes (18-25 ans)';
    case AgeGroup.ADULTES:
      return 'Adultes (26-60 ans)';
    case AgeGroup.SENIORS:
      return 'Seniors (60+ ans)';
    default:
      return 'Non spécifié';
  }
}

// Helper function to convert CompleteTutorProfile to TutorCard
export function tutorProfileToCard(profile: CompleteTutorProfile): TutorCard {
  // Calculate experience years from teachingSince if available
  let experienceYears = 0;
  if (profile.teachingSince) {
    const startYear = new Date(profile.teachingSince).getFullYear();
    const currentYear = new Date().getFullYear();
    experienceYears = Math.max(0, currentYear - startYear);
  }

  return {
    id: profile.id,
    name: `${profile.prenom} ${profile.nom}`,
    profileImage: profile.profilePhotoUrl,
    profilePhotoUrl: profile.profilePhotoUrl,
    rating: profile.noteGlobale || 0,
    averageRating: profile.noteGlobale || 0,
    reviewCount: profile.nombreEvaluations || 0,
    totalRatings: profile.nombreEvaluations || 0,
    tarif: profile.tarif,
    location: profile.locationCity ? `${profile.locationCity}, ${profile.locationCountry}` : profile.locationCountry,
    languages: profile.languagesSpoken ? profile.languagesSpoken.split(',').map(l => l.trim()) : [],
    subjects: [], // Would need to be fetched separately from competences
    isVerified: profile.isVerified || false,
    isOnline: profile.availabilityStatus === AvailabilityStatus.AVAILABLE,
    responseTime: profile.responseTimeHours,
    experienceYears: experienceYears,
    shortDescription: profile.description?.length ? 
      profile.description.length > 150 ? 
        profile.description.substring(0, 150) + '...' : 
        profile.description : 
      profile.bio?.length ? 
        profile.bio.length > 150 ? 
          profile.bio.substring(0, 150) + '...' : 
          profile.bio : 
        undefined
  };
}

// Search suggestion interfaces
export interface SearchSuggestion {
  type: 'tutor' | 'subject' | 'location';
  id: string;
  label: string;
  subtitle?: string;
  icon?: string;
  data?: any;
}

export interface TutorSearchSuggestions {
  tutors: SearchSuggestion[];
  subjects: SearchSuggestion[];
  locations: SearchSuggestion[];
}