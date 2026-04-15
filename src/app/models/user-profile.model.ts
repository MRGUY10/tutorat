// User Profile Models

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  INACTIVE = 'INACTIVE'
}

export interface UserProfile {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  statut: UserStatus;
  photo?: string;
  dateInscription: Date;
  createdAt: Date;
  updatedAt: Date;
  fullName?: string;
}

export interface StudentProfile extends UserProfile {
  filiere: string;
  annee: number;
  niveau: string;
  profileCompletion?: number;
}

export interface TutorProfile extends UserProfile {
  // Tutor-specific fields
  experience?: string;
  tarifHoraire: number;
  diplomes?: string;
  description?: string;
  specialites: TutorSpecialiteDTO[];
  
  // Rating and verification
  noteMoyenne: number;
  nombreEvaluations: number;
  verifie: boolean;
  dateVerification?: Date;
  
  // Availability and preferences
  disponible: boolean;
  coursEnLigne: boolean;
  coursPresentiel: boolean;
  ville?: string;
  pays?: string;
  
  // Computed fields
  availableForBooking?: boolean;
  profileCompletion?: number;
}

export interface TutorSpecialiteDTO {
  id: number;
  tutorId: number;
  matiereId: number;
  matiereName: string;
  niveau: string;
  domaine?: string;
}

export interface ProfileUpdateRequest {
  nom?: string;
  prenom?: string;
  telephone?: string;
  photo?: string;
}

export interface StudentProfileUpdateRequest extends ProfileUpdateRequest {
  filiere?: string;
  annee?: number;
  niveau?: string;
}

export interface TutorProfileUpdateRequest extends ProfileUpdateRequest {
  experience?: string;
  tarifHoraire?: number;
  diplomes?: string;
  description?: string;
  specialiteIds?: number[];
  ville?: string;
  pays?: string;
  coursEnLigne?: boolean;
  coursPresentiel?: boolean;
  disponible?: boolean;
}

export interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface PasswordUpdateResponse {
  success: boolean;
  message: string;
}
