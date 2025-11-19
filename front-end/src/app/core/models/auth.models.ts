// Authentication models matching backend DTOs

export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  expiresIn: number;
  email: string;
  role: string;
  userId: number;
  userType?: 'STUDENT' | 'TUTOR' | 'ADMIN';
  userInfo?: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    roles: string[];
    userType: 'STUDENT' | 'TUTOR' | 'ADMIN';
  };
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

// Registration DTOs
export interface StudentRegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  telephone?: string;
  filiere: string;
  annee: number;
  niveau: NiveauAcademique;
}

export interface TutorRegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  telephone?: string;
  experience?: string;
  tarifHoraire: number; // Backend uses tarifHoraire instead of tarif
  diplomes?: string;
  description?: string;
  specialiteIds: number[]; // Required: list of matiere IDs
  ville?: string;
  pays?: string;
  coursEnLigne?: boolean;
  coursPresentiel?: boolean;
}

// Academic level enum matching backend
export enum NiveauAcademique {
  DEBUTANT = 'DEBUTANT',
  INTERMEDIAIRE = 'INTERMEDIAIRE',
  AVANCE = 'AVANCE'
}

// User models
export interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface Student extends User {
  filiere: string;
  annee: number;
  niveau: NiveauAcademique;
}

export interface Tutor extends User {
  experience?: string;
  tarif: number;
  diplomes?: string;
  description?: string;
  note?: number;
  nombreEvaluations?: number;
}

// User roles enum matching backend
export enum UserRole {
  STUDENT = 'STUDENT',
  TUTOR = 'TUTOR',
  ADMIN = 'ADMIN'
}

// User types enum
export enum UserType {
  STUDENT = 'STUDENT',
  TUTOR = 'TUTOR',
  ADMIN = 'ADMIN'
}

// Decoded JWT token interface
export interface DecodedToken {
  sub: string;
  email: string;
  role: UserRole;
  userId: number;
  userType: UserType;
  exp: number;
  iat: number;
}

// Current user info interface
export interface CurrentUser {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: UserRole;
  userType: UserType;
  roles: string[];
  isAuthenticated: boolean;
  profileComplete?: boolean;
}

// Login state interface
export interface LoginState {
  isLoggedIn: boolean;
  user: CurrentUser | null;
  token: string | null;
  refreshToken: string | null;
  loginTime: Date | null;
  expiryTime: Date | null;
}

// Route access interface for role-based routing
export interface RouteAccess {
  roles: UserRole[];
  redirectTo?: string;
  requiresAuth: boolean;
}

// Error response interface
export interface ApiError {
  error: string;
  message: string;
  details?: string[];
  timestamp?: string;
}

// Session management interfaces
export interface SessionInfo {
  sessionId: string;
  userId: number;
  userEmail: string;
  userRole: string;
  loginTime: Date;
  lastActivity: Date;
  expiryTime: Date;
  isActive: boolean;
  deviceInfo: DeviceInfo;
  ipAddress: string | null;
  sessionDuration: number;
}

export interface SessionActivity {
  timestamp: Date;
  type: string;
  page: string;
  idleTime: number;
  sessionDuration: number;
}

export interface DeviceInfo {
  userAgent: string;
  platform: string;
  language: string;
  screenResolution: string;
  timezone: string;
  browserName: string;
  browserVersion: string;
  isMobile: boolean;
  fingerprint?: string;
  trustedAt?: Date;
}

// Security management interfaces
export interface SecurityEvent {
  id: string;
  type: string;
  description: string;
  timestamp: Date;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  metadata: any;
  deviceFingerprint: string;
}

export interface SecurityAlert {
  id: string;
  type: string;
  message: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  timestamp: Date;
  dismissed: boolean;
  details: any;
}