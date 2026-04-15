export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING = 'PENDING'
}

export enum UserRole {
  STUDENT = 'STUDENT',
  TUTOR = 'TUTOR', 
  ADMIN = 'ADMIN'
}

export enum NiveauAcademique {
  DEBUTANT = 'DEBUTANT',
  INTERMEDIAIRE = 'INTERMEDIAIRE',
  AVANCE = 'AVANCE'
}

export interface BaseUser {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  dateInscription?: Date;
  statut: UserStatus;
  photo?: string;
  createdAt?: Date;
  updatedAt?: Date;
  version?: number;
}

export interface User extends BaseUser {
  motDePasse?: string; // Only for creation/update
}

export interface Etudiant extends User {
  filiere: string;
  annee: number;
  niveau: NiveauAcademique;
}

export interface Tuteur extends User {
  experience: string;
  tarif: number;
  diplomes: string;
  description: string;
  bio?: string;
  teachingMethodology?: string;
  certifications?: string;
  awards?: string;
  teachingSince?: Date;
  isVerified?: boolean;
  profileCompletion?: number;
  averageRating?: number;
  totalReviews?: number;
  hourlyRate?: number;
  preferredAgeGroup?: string;
  languagesSpoken?: string;
  city?: string;
  country?: string;
  socialMediaLinks?: string; // JSON string
  lastActive?: Date;
}

export interface Admin extends User {
  permissions: string;
  departement: string;
}

// Request DTOs for user creation
export interface CreateStudentRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  telephone?: string;
  filiere: string;
  annee: number;
  niveau: NiveauAcademique;
}

export interface CreateTutorRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  telephone?: string;
  experience: string;
  tarif: number;
  diplomes: string;
  description: string;
}

export interface CreateAdminRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  telephone?: string;
  permissions: string;
  departement: string;
}

// Update DTOs
export interface UpdateBasicProfileDTO {
  experience?: number;
  tarif?: number;
  diplomes?: string;
  description?: string;
  bio?: string;
  teachingMethodology?: string;
  certifications?: string;
  awards?: string;
  teachingSince?: Date;
}

export interface UpdateLocationDTO {
  city: string;
  country: string;
}

export interface UpdateTeachingPreferencesDTO {
  preferredAgeGroup: string;
  languagesSpoken: string;
}

// Combined user type for management interface
export interface UserManagement {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  dateInscription: Date;
  lastLogin?: Date;
  isActive: boolean;
  enabled: boolean;
  phone?: string;
  // Role-specific data (optional)
  studentData?: Partial<Etudiant>;
  tutorData?: Partial<Tuteur>;
  adminData?: Partial<Admin>;
}

// Search and filter interfaces
export interface UserSearchParams {
  pattern?: string;
  status?: UserStatus;
  role?: UserRole;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  blockedUsers: number;
  totalStudents: number;
  totalTutors: number;
  activeUsersPercentage?: number;
  blockedUsersPercentage?: number;
}

// DTOs for user status changes (block/unblock)
export interface UserStatusChangeRequest {
  reason?: string;
}

export interface UserWithRolesDTO {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  statut: UserStatus;
  dateInscription: Date;
  lastLogin?: Date;
  roles: string[];
  primaryRole?: string;
  userType?: string;
  // Additional user data
  photo?: string;
  createdAt?: Date;
  updatedAt?: Date;
}

// DTO for updating user information
export interface UpdateUserRequest {
  prenom?: string;
  nom?: string;
  email?: string;
  telephone?: string;
  statut?: UserStatus;
  photo?: string;
}