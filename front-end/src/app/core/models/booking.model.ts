import { SessionType, Urgence } from './session.model';
import { Subject } from './subject.model';
import { CompleteTutorProfile, TutorSpecialty } from './tutor.model';

// ================================================
// BOOKING FLOW MODELS
// ================================================

/**
 * Represents the different steps in the booking flow
 */
export type BookingStep = 'subjects' | 'details' | 'review' | 'success';

/**
 * Form data structure for booking a session
 */
export interface BookingFormData {
  matiereId: number | null;
  dateVoulue: string;
  dureeSouhaitee: number;
  budgetMax: number;
  urgence: Urgence;
  message: string;
  notesAdditionnelles?: string;
  flexibleSurDate?: boolean;
  accepteSeanceEnLigne?: boolean;
  accepteSeancePresentiel?: boolean;
}

/**
 * Complete booking context including selected entities
 */
export interface BookingContext {
  selectedTutor: CompleteTutorProfile;
  selectedSubject: Subject | null;
  tutorSpecialties: TutorSpecialty[];
  availableSubjects: Subject[];
  formData: BookingFormData;
  currentStep: BookingStep;
}

/**
 * Booking state management
 */
export interface BookingState {
  isActive: boolean;
  currentStep: BookingStep;
  isSubmitting: boolean;
  isSuccess: boolean;
  error: string | null;
  context: BookingContext | null;
}

/**
 * Options for session types in booking
 */
export interface SessionTypeOption {
  value: SessionType;
  label: string;
  description?: string;
  icon?: string;
}

/**
 * Options for urgency levels in booking
 */
export interface UrgencyOption {
  value: Urgence;
  label: string;
  description?: string;
  color?: string;
}

/**
 * Duration options for sessions
 */
export interface DurationOption {
  value: number; // in minutes
  label: string;
  recommended?: boolean;
}

/**
 * Booking validation result
 */
export interface BookingValidation {
  isValid: boolean;
  errors: {
    [key: string]: string[];
  };
  warnings?: {
    [key: string]: string[];
  };
}

/**
 * Booking summary for review step
 */
export interface BookingSummary {
  tutor: {
    id: number;
    name: string;
    avatar?: string;
    rating?: number;
    pricePerHour?: number;
  };
  subject: {
    id: number;
    name: string;
    level?: string;
  };
  session: {
    dateTime: string;
    duration: number;
    budget: number;
    priority: Urgence;
    message: string;
    sessionTypes: {
      online: boolean;
      inPerson: boolean;
    };
    flexibility: {
      dateFlexible: boolean;
    };
  };
  estimatedCost?: number;
}

// ================================================
// HELPER FUNCTIONS
// ================================================

/**
 * Default duration options for session booking
 */
export const DEFAULT_DURATION_OPTIONS: DurationOption[] = [
  { value: 30, label: '30 minutes' },
  { value: 60, label: '1 hour', recommended: true },
  { value: 90, label: '1.5 hours' },
  { value: 120, label: '2 hours' },
  { value: 150, label: '2.5 hours' },
  { value: 180, label: '3 hours' }
];

/**
 * Default urgency options for session booking
 */
export const DEFAULT_URGENCY_OPTIONS: UrgencyOption[] = [
  { 
    value: Urgence.FAIBLE, 
    label: 'Low Priority', 
    description: 'I can wait a few days',
    color: 'green'
  },
  { 
    value: Urgence.MOYENNE, 
    label: 'Medium Priority', 
    description: 'Within a week would be good',
    color: 'yellow'
  },
  { 
    value: Urgence.HAUTE, 
    label: 'High Priority', 
    description: 'I need help soon',
    color: 'red'
  }
];

/**
 * Default session type options
 */
export const DEFAULT_SESSION_TYPE_OPTIONS: SessionTypeOption[] = [
  {
    value: SessionType.EN_LIGNE,
    label: 'Online Session',
    description: 'Video call via Zoom, Teams, or similar',
    icon: 'video'
  },
  {
    value: SessionType.PRESENTIEL,
    label: 'In-Person Session',
    description: 'Meet in person at an agreed location',
    icon: 'user'
  }
];

/**
 * Creates initial booking form data with defaults
 */
export function createInitialBookingFormData(): BookingFormData {
  const now = new Date();
  const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
  const defaultDateTime = tomorrow.toISOString().slice(0, 16);

  return {
    matiereId: null,
    dateVoulue: defaultDateTime,
    dureeSouhaitee: 60,
    budgetMax: 50,
    urgence: Urgence.MOYENNE,
    message: '',
    notesAdditionnelles: '',
    flexibleSurDate: false,
    accepteSeanceEnLigne: true,
    accepteSeancePresentiel: false
  };
}

/**
 * Creates initial booking state
 */
export function createInitialBookingState(): BookingState {
  return {
    isActive: false,
    currentStep: 'subjects',
    isSubmitting: false,
    isSuccess: false,
    error: null,
    context: null
  };
}

/**
 * Validates booking form data
 */
export function validateBookingForm(formData: BookingFormData): BookingValidation {
  const errors: { [key: string]: string[] } = {};
  const warnings: { [key: string]: string[] } = {};

  // Required field validation
  if (!formData.matiereId) {
    errors['matiereId'] = ['Subject is required'];
  }

  if (!formData.dateVoulue) {
    errors['dateVoulue'] = ['Date and time is required'];
  } else {
    const selectedDate = new Date(formData.dateVoulue);
    const now = new Date();
    
    if (selectedDate <= now) {
      errors['dateVoulue'] = ['Date must be in the future'];
    }
    
    // Warning for very soon dates
    const hoursDiff = (selectedDate.getTime() - now.getTime()) / (1000 * 60 * 60);
    if (hoursDiff < 2) {
      warnings['dateVoulue'] = ['Very short notice - consider if the tutor will be available'];
    }
  }

  if (!formData.message || formData.message.trim().length === 0) {
    errors['message'] = ['Message to tutor is required'];
  } else if (formData.message.trim().length < 10) {
    warnings['message'] = ['Consider providing more details about what you want to learn'];
  }

  if (formData.budgetMax <= 0) {
    errors['budgetMax'] = ['Budget must be greater than 0'];
  }

  if (formData.dureeSouhaitee < 30) {
    errors['dureeSouhaitee'] = ['Minimum session duration is 30 minutes'];
  } else if (formData.dureeSouhaitee > 240) {
    errors['dureeSouhaitee'] = ['Maximum session duration is 4 hours'];
  }

  // Session type validation
  if (!formData.accepteSeanceEnLigne && !formData.accepteSeancePresentiel) {
    errors['sessionType'] = ['You must accept at least one session type'];
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
    warnings: Object.keys(warnings).length > 0 ? warnings : undefined
  };
}

/**
 * Creates booking summary from form data and context
 */
export function createBookingSummary(
  tutor: CompleteTutorProfile,
  subject: Subject,
  formData: BookingFormData
): BookingSummary {
  return {
    tutor: {
      id: tutor.id,
      name: `${tutor.prenom} ${tutor.nom}`,
      avatar: tutor.profilePhotoUrl,
      rating: tutor.noteGlobale,
      pricePerHour: tutor.tarif
    },
    subject: {
      id: subject.id!,
      name: subject.nom,
      level: subject.niveau
    },
    session: {
      dateTime: formData.dateVoulue,
      duration: formData.dureeSouhaitee,
      budget: formData.budgetMax,
      priority: formData.urgence,
      message: formData.message,
      sessionTypes: {
        online: formData.accepteSeanceEnLigne || false,
        inPerson: formData.accepteSeancePresentiel || false
      },
      flexibility: {
        dateFlexible: formData.flexibleSurDate || false
      }
    },
    estimatedCost: calculateEstimatedCost(tutor.tarif, formData.dureeSouhaitee)
  };
}

/**
 * Calculates estimated cost based on tutor rate and duration
 */
function calculateEstimatedCost(hourlyRate?: number, durationMinutes?: number): number | undefined {
  if (!hourlyRate || !durationMinutes) return undefined;
  
  const hours = durationMinutes / 60;
  return Math.round(hourlyRate * hours * 100) / 100; // Round to 2 decimal places
}

/**
 * Formats duration in a human-readable way
 */
export function formatDuration(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} minutes`;
  }
  
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  
  if (remainingMinutes === 0) {
    return `${hours} hour${hours > 1 ? 's' : ''}`;
  }
  
  return `${hours}h ${remainingMinutes}m`;
}

/**
 * Formats date and time for display
 */
export function formatBookingDateTime(dateTimeString: string): string {
  const date = new Date(dateTimeString);
  
  return date.toLocaleString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Gets display text for urgency level
 */
export function getUrgencyDisplay(urgence: Urgence): string {
  const option = DEFAULT_URGENCY_OPTIONS.find(opt => opt.value === urgence);
  return option?.label || urgence;
}

/**
 * Gets display text for session type
 */
export function getSessionTypeDisplay(type: SessionType): string {
  const option = DEFAULT_SESSION_TYPE_OPTIONS.find(opt => opt.value === type);
  return option?.label || type;
}