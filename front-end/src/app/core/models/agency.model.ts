export interface Agency {
  id?: number;
  code: string;
  libelle: string;
  adresse?: string;
  telephone?: string;
  email?: string;
  directeur?: number;
  reseauId: number;
  statut: AgencyStatus;
  suspensionEndDate?: Date;
  suspensionReason?: string;
  directeurNom?: string;
  reseauLibelle?: string;
  reseauRegion?: string;
  totalAgents?: number;
  createdAt?: Date;
  updatedAt?: Date;
}

export enum AgencyStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  INACTIVE = 'INACTIVE'
}

export interface CreateAgencyRequest {
  code: string;
  libelle: string;
  adresse?: string;
  telephone?: string;
  email?: string;
  directeur?: number;
  reseauId: number;
  statut?: AgencyStatus;
}

export interface UpdateAgencyRequest {
  libelle?: string;
  adresse?: string;
  telephone?: string;
  email?: string;
  directeur?: number;
  reseauId?: number;
  statut?: AgencyStatus;
  suspensionEndDate?: Date;
  suspensionReason?: string;
}

export interface AgencySearchCriteria {
  libelle?: string;
  code?: string;
  statut?: AgencyStatus;
  directeur?: string;
}

export interface AgencyListResponse {
  content: Agency[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
