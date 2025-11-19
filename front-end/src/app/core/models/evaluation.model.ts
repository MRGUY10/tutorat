/**
 * Evaluation models for rating tutors and students
 */

export enum EvaluationType {
  ETUDIANT_VERS_TUTEUR = 'ETUDIANT_VERS_TUTEUR',
  TUTEUR_VERS_ETUDIANT = 'TUTEUR_VERS_ETUDIANT'
}

export interface CreateEvaluation {
  sessionId: number;
  evaluateurId: number;
  evalueId: number;
  note: number;
  commentaire?: string;
  typeEvaluation: EvaluationType;
  
  // Detailed criteria (optional)
  qualiteEnseignement?: number;
  communication?: number;
  ponctualite?: number;
  preparation?: number;
  patience?: number;
  
  // Recommendation
  recommanderais?: boolean;
}

export interface EvaluationResponse {
  id: number;
  sessionId: number;
  evaluateurId: number;
  evalueId: number;
  note: number;
  commentaire?: string;
  typeEvaluation: EvaluationType;
  dateCreation: string;
  
  // Detailed criteria
  qualiteEnseignement?: number;
  communication?: number;
  ponctualite?: number;
  preparation?: number;
  patience?: number;
  
  // Recommendation
  recommanderais?: boolean;
  
  // Additional info
  evaluateurNom?: string;
  evaluateurPrenom?: string;
  evalueNom?: string;
  evaluePrenom?: string;
}

export interface EvaluationSummary {
  userId: number;
  type: EvaluationType;
  averageRating: number;
  totalEvaluations: number;
  ratingDistribution: {
    [key: number]: number; // 1-5 stars count
  };
  
  // Detailed criteria averages
  averageQualiteEnseignement?: number;
  averageCommunication?: number;
  averagePonctualite?: number;
  averagePreparation?: number;
  averagePatience?: number;
  
  // Recommendation percentage
  recommendationRate?: number;
}

export interface SessionEvaluationStatus {
  studentEvaluationExists: boolean;
  tutorEvaluationExists: boolean;
  evaluationComplete: boolean;
}

export interface SessionFeedback {
  sessionId: number;
  studentEvaluation?: EvaluationResponse;
  tutorEvaluation?: EvaluationResponse;
  complete: boolean;
}
