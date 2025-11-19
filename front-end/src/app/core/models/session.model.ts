// Enums matching backend
export enum SessionStatus {
  DEMANDEE = 'DEMANDEE',
  CONFIRMEE = 'CONFIRMEE',
  EN_COURS = 'EN_COURS',
  TERMINEE = 'TERMINEE',
  ANNULEE = 'ANNULEE'
}

export enum SessionType {
  EN_LIGNE = 'EN_LIGNE',
  PRESENTIEL = 'PRESENTIEL'
}

export enum RequestStatus {
  EN_ATTENTE = 'EN_ATTENTE',
  ACCEPTEE = 'ACCEPTEE',
  REFUSEE = 'REFUSEE'
}

export enum Urgence {
  FAIBLE = 'FAIBLE',
  MOYENNE = 'MOYENNE',
  HAUTE = 'HAUTE'
}

// Core Session Interfaces
export interface SessionResponse {
  id: number;
  tuteurId: number;
  etudiantId: number;
  matiereId: number;
  demandeSessionId?: number;
  dateHeure: string;
  duree: number;
  statut: SessionStatus;
  prix: number;
  typeSession: SessionType;
  lienVisio?: string;
  notes?: string;
  salle?: string;
  createdAt: string;
  updatedAt?: string;

  // Enhanced session information
  sessionDescription?: string;
  preparationNotes?: string;
  raisonModification?: string;

  // Session feedback
  feedbackTuteur?: string;
  feedbackEtudiant?: string;
  noteTuteur?: number;
  noteEtudiant?: number;

  // Session summary
  resumeSeance?: string;
  objectifsAtteints?: string;
  recommandations?: string;
  prochaigesEtapes?: string;

  // Calculated fields
  dateFin?: string;
  canBeModified?: boolean;
  canBeCancelled?: boolean;
  canBeCompleted?: boolean;
}

export interface SessionDetails extends SessionResponse {
  // Student details
  etudiantNom: string;
  etudiantPrenom: string;
  etudiantEmail: string;
  etudiantTelephone?: string;
  
  // Tutor details
  tuteurNom: string;
  tuteurPrenom: string;
  tuteurEmail: string;
  tuteurTelephone?: string;
  tuteurSpecialite?: string;
  tuteurTarifHoraire?: number;
  tuteurVerified?: boolean;
  
  // Subject details
  matiereNom: string;
  
  // Additional computed fields
  participantNames?: {
    tutorName: string;
    studentName: string;
    subjectName: string;
  };
}

export interface CreateSession {
  tuteurId: number;
  etudiantId: number;
  matiereId: number;
  demandeSessionId?: number;
  dateHeure: string;
  duree: number;
  typeSession: SessionType;
  prix: number;
  lienVisio?: string;
  notes?: string;
  salle?: string;
  requiresConfirmation?: boolean;
  sendNotifications?: boolean;
  sessionDescription?: string;
  preparationNotes?: string;
}

export interface UpdateSession {
  dateHeure?: string;
  duree?: number;
  typeSession?: SessionType;
  prix?: number;
  lienVisio?: string;
  notes?: string;
  salle?: string;
  statut?: SessionStatus;
}

// Session Request Interfaces
export interface SessionRequestResponse {
  id: number;
  etudiantId: number;
  tuteurId: number;
  matiereId: number;
  dateCreation: string;
  dateVoulue: string;
  message: string;
  statut: RequestStatus;
  urgence: Urgence;
  dureeSouhaitee: number;
  budgetMax: number;
  reponseTuteur?: string;
  dateReponse?: string;
  createdAt: string;
  updatedAt?: string;
  
  // Enhanced fields
  notesAdditionnelles?: string;
  flexibleSurDate?: boolean;
  accepteSeanceEnLigne?: boolean;
  accepteSeancePresentiel?: boolean;
  dateAlternative1?: string;
  dateAlternative2?: string;
  dateAlternative3?: string;
  prixPropose?: number;
  dureeProposee?: number;

  // Calculated fields
  canBeModified?: boolean;
  isExpired?: boolean;
  hoursUntilDesiredDate?: number;
}

export interface CreateSessionRequest {
  tuteurId: number;
  matiereId: number;
  dateVoulue: string;
  message: string;
  urgence: Urgence;
  dureeSouhaitee: number;
  budgetMax: number;
  notesAdditionnelles?: string;
  flexibleSurDate?: boolean;
  accepteSeanceEnLigne?: boolean;
  accepteSeancePresentiel?: boolean;
  dateAlternative1?: string;
  dateAlternative2?: string;
  dateAlternative3?: string;
}

export interface UpdateSessionRequest {
  dateVoulue?: string;
  message?: string;
  urgence?: Urgence;
  dureeSouhaitee?: number;
  budgetMax?: number;
  statut?: RequestStatus;
  reponseTuteur?: string;
  notesAdditionnelles?: string;
  flexibleSurDate?: boolean;
  accepteSeanceEnLigne?: boolean;
  accepteSeancePresentiel?: boolean;
  dateAlternative1?: string;
  dateAlternative2?: string;
  dateAlternative3?: string;
  prixPropose?: number;
}

export interface SessionRequestDetails {
  id: number;
  dateCreation: string;
  dateVoulue: string;
  message: string;
  statut: RequestStatus;
  urgence: Urgence;
  dureeSouhaitee: number;
  budgetMax: number;
  reponseTuteur?: string | null;
  dateReponse?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  
  // Student details
  etudiantId: number;
  etudiantNom: string | null;
  etudiantPrenom: string | null;
  etudiantEmail: string | null;
  etudiantTelephone?: string | null;
  etudiantStatus?: string | null;
  
  // Tutor details
  tuteurId: number;
  tuteurNom: string | null;
  tuteurPrenom: string | null;
  tuteurEmail: string | null;
  tuteurTelephone?: string | null;
  tuteurStatus?: string | null;
  tuteurSpecialite?: string | null;
  tuteurTarifHoraire?: number;
  tuteurVerified?: boolean;
  
  // Subject details
  matiereId: number;
  matiereNom: string | null;
  matiereDescription?: string | null;
  
  // Enhanced fields
  notesAdditionnelles?: string | null;
  flexibleSurDate?: boolean;
  accepteSeanceEnLigne?: boolean;
  accepteSeancePresentiel?: boolean;
  dateAlternative1?: string | null;
  dateAlternative2?: string | null;
  dateAlternative3?: string | null;
  prixPropose?: number | null;
  dureeProposee?: number | null;
  estimatedPrice?: number;
  statusDescription?: string;
  canBeModified?: boolean;
  hoursUntilDesiredDate?: number;
  expired?: boolean;
  urgent?: boolean;
  
  // Related session (if created)
  sessionId?: number | null;
  sessionDateHeure?: string | null;
  sessionStatut?: string | null;
}

// UI-specific interfaces
export interface SessionCard {
  id: number;
  title: string;
  studentName: string;
  tutorName: string;
  subject: string;
  dateTime: Date;
  duration: number;
  status: SessionStatus;
  type: SessionType;
  price: number;
  location?: string;
  isUpcoming: boolean;
  canJoin: boolean;
  canCancel: boolean;
  statusColor: string;
  typeIcon: string;
}

export interface SessionRequestCard {
  id: number;
  studentName: string;
  tutorName: string;
  subject: string;
  requestedDate: Date;
  message: string;
  status: RequestStatus;
  urgency: Urgence;
  duration: number;
  budget: number;
  createdAt: Date;
  canRespond: boolean;
  isExpired: boolean;
  statusColor: string;
  urgencyColor: string;
}

export interface SessionFilters {
  status?: SessionStatus;
  type?: SessionType;
  startDate?: Date;
  endDate?: Date;
  tutorId?: number;
  studentId?: number;
  subjectId?: number;
  minPrice?: number;
  maxPrice?: number;
  searchTerm?: string;
}

export interface SessionRequestFilters {
  status?: RequestStatus;
  urgency?: Urgence;
  tutorId?: number;
  studentId?: number;
  subjectId?: number;
  startDate?: Date;
  endDate?: Date;
  minBudget?: number;
  maxBudget?: number;
  searchTerm?: string;
}

export interface SessionStatistics {
  totalSessions: number;
  upcomingSessions: number;
  completedSessions: number;
  cancelledSessions: number;
  averageRating: number;
  totalEarnings: number;
  sessionsByStatus: Record<SessionStatus, number>;
  sessionsByType: Record<SessionType, number>;
  monthlySessionCounts: { month: string; count: number }[];
}

export interface SessionRequestStatistics {
  totalRequests: number;
  pendingRequests: number;
  acceptedRequests: number;
  rejectedRequests: number;
  averageResponseTime: number;
  requestsByUrgency: Record<Urgence, number>;
  requestsByStatus: Record<RequestStatus, number>;
  monthlyRequestCounts: { month: string; count: number }[];
}

// Helper functions
export function getSessionStatusDisplay(status: SessionStatus): string {
  switch (status) {
    case SessionStatus.DEMANDEE:
      return 'Demandée';
    case SessionStatus.CONFIRMEE:
      return 'Confirmée';
    case SessionStatus.EN_COURS:
      return 'En cours';
    case SessionStatus.TERMINEE:
      return 'Terminée';
    case SessionStatus.ANNULEE:
      return 'Annulée';
    default:
      return 'Inconnu';
  }
}

export function getSessionStatusColor(status: SessionStatus): string {
  switch (status) {
    case SessionStatus.DEMANDEE:
      return 'bg-yellow-100 text-yellow-800';
    case SessionStatus.CONFIRMEE:
      return 'bg-blue-100 text-blue-800';
    case SessionStatus.EN_COURS:
      return 'bg-green-100 text-green-800';
    case SessionStatus.TERMINEE:
      return 'bg-gray-100 text-gray-800';
    case SessionStatus.ANNULEE:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

export function getRequestStatusDisplay(status: RequestStatus): string {
  switch (status) {
    case RequestStatus.EN_ATTENTE:
      return 'En attente';
    case RequestStatus.ACCEPTEE:
      return 'Acceptée';
    case RequestStatus.REFUSEE:
      return 'Refusée';
    default:
      return 'Inconnu';
  }
}

export function getRequestStatusColor(status: RequestStatus): string {
  switch (status) {
    case RequestStatus.EN_ATTENTE:
      return 'bg-yellow-100 text-yellow-800';
    case RequestStatus.ACCEPTEE:
      return 'bg-green-100 text-green-800';
    case RequestStatus.REFUSEE:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

export function getUrgencyDisplay(urgency: Urgence): string {
  switch (urgency) {
    case Urgence.FAIBLE:
      return 'Faible';
    case Urgence.MOYENNE:
      return 'Moyenne';
    case Urgence.HAUTE:
      return 'Haute';
    default:
      return 'Normale';
  }
}

export function getUrgencyColor(urgency: Urgence): string {
  switch (urgency) {
    case Urgence.FAIBLE:
      return 'bg-green-100 text-green-800';
    case Urgence.MOYENNE:
      return 'bg-yellow-100 text-yellow-800';
    case Urgence.HAUTE:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

export function getSessionTypeDisplay(type: SessionType): string {
  switch (type) {
    case SessionType.EN_LIGNE:
      return 'En ligne';
    case SessionType.PRESENTIEL:
      return 'Présentiel';
    default:
      return 'Inconnu';
  }
}

export function getSessionTypeIcon(type: SessionType): string {
  switch (type) {
    case SessionType.EN_LIGNE:
      return 'video';
    case SessionType.PRESENTIEL:
      return 'location_on';
    default:
      return 'help';
  }
}

export function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  
  if (hours === 0) {
    return `${mins}min`;
  } else if (mins === 0) {
    return `${hours}h`;
  } else {
    return `${hours}h${mins}min`;
  }
}

export function sessionToCard(session: SessionResponse, studentName: string, tutorName: string, subjectName: string): SessionCard {
  const sessionDate = new Date(session.dateHeure);
  const now = new Date();
  const isUpcoming = sessionDate > now;
  
  return {
    id: session.id,
    title: `${subjectName} - ${formatDuration(session.duree)}`,
    studentName,
    tutorName,
    subject: subjectName,
    dateTime: sessionDate,
    duration: session.duree,
    status: session.statut,
    type: session.typeSession,
    price: session.prix,
    location: session.typeSession === SessionType.PRESENTIEL ? session.salle : 'En ligne',
    isUpcoming,
    canJoin: session.statut === SessionStatus.CONFIRMEE && isUpcoming && Math.abs(sessionDate.getTime() - now.getTime()) <= 15 * 60 * 1000, // 15 minutes before
    canCancel: session.statut === SessionStatus.DEMANDEE || session.statut === SessionStatus.CONFIRMEE,
    statusColor: getSessionStatusColor(session.statut),
    typeIcon: getSessionTypeIcon(session.typeSession)
  };
}

export function sessionRequestToCard(request: SessionRequestDetails): SessionRequestCard {
  const requestedDate = new Date(request.dateVoulue);
  const createdAt = new Date(request.dateCreation);
  const now = new Date();
  const isExpired = (request.expired !== undefined ? request.expired : (request.statut === RequestStatus.EN_ATTENTE && requestedDate < now));
  
  return {
    id: request.id,
    studentName: `${request.etudiantPrenom || 'N/A'} ${request.etudiantNom || 'N/A'}`.trim() || 'Étudiant',
    tutorName: `${request.tuteurPrenom || 'N/A'} ${request.tuteurNom || 'N/A'}`.trim() || 'Tuteur',
    subject: request.matiereNom || 'Matière inconnue',
    requestedDate,
    message: request.message,
    status: request.statut,
    urgency: request.urgence,
    duration: request.dureeSouhaitee,
    budget: request.budgetMax,
    createdAt,
    canRespond: request.canBeModified !== undefined ? request.canBeModified : (request.statut === RequestStatus.EN_ATTENTE && !isExpired),
    isExpired,
    statusColor: getRequestStatusColor(request.statut),
    urgencyColor: getUrgencyColor(request.urgence)
  };
}