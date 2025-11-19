import { NiveauAcademique } from '../core/models/auth.models';

export interface Matiere {
  id: number;
  nom: string;
  description?: string;
  niveau: NiveauAcademique;
  domaine?: string;
  createdAt?: Date;
  updatedAt?: Date;
  version?: number;
}

export interface CreateMatiereRequest {
  nom: string;
  description?: string;
  niveau: NiveauAcademique;
  domaine?: string;
}

export interface UpdateMatiereRequest {
  nom?: string;
  description?: string;
  niveau?: NiveauAcademique;
  domaine?: string;
}

export interface MatiereSearchRequest {
  nom?: string;
  domaine?: string;
  niveau?: NiveauAcademique;
}
