export enum NiveauAcademique {
  DEBUTANT = 'DEBUTANT',
  INTERMEDIAIRE = 'INTERMEDIAIRE',
  AVANCE = 'AVANCE'
}

export interface Subject {
  id?: number;
  nom: string;
  description?: string;
  niveau: NiveauAcademique;
  domaine: string;
  createdAt?: Date;
  updatedAt?: Date;
  version?: number;
}

export interface CreateSubjectRequest {
  nom: string;
  description?: string;
  niveau: NiveauAcademique;
  domaine: string;
}

export interface UpdateSubjectRequest {
  nom?: string;
  description?: string;
  niveau?: NiveauAcademique;
  domaine?: string;
}

export interface SubjectSearchParams {
  searchTerm?: string;
  domaine?: string;
  niveau?: NiveauAcademique;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export interface SubjectStatistics {
  totalSubjects: number;
  subjectsByDomain: { [domain: string]: number };
  subjectsByLevel: { [level: string]: number };
}

// Options for dropdowns
export interface SubjectFormOptions {
  niveauOptions: { value: NiveauAcademique; label: string }[];
  domaineOptions: string[];
}