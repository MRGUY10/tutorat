import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { SessionService } from '../../../services/session.service';
import { AuthService } from '../../../services/auth.service';
import { 
  SessionResponse, 
  SessionStatus, 
  SessionType 
} from '../../../core/models/session.model';

interface CalendarDay {
  date: Date;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isPast: boolean;
  sessions: SessionResponse[];
}

interface CalendarWeek {
  days: CalendarDay[];
}

type CalendarView = 'month' | 'week' | 'day';

@Component({
  selector: 'app-session-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './session-calendar.component.html',
  styleUrls: ['./session-calendar.component.css']
})
export class SessionCalendarComponent implements OnInit, OnDestroy {
  @Input() userId!: number;
  @Input() userType: 'tutor' | 'student' = 'student';

  // Calendar state
  currentDate = new Date();
  selectedDate = new Date();
  viewMode: CalendarView = 'month';
  weeks: CalendarWeek[] = [];
  
  // Sessions
  sessions: SessionResponse[] = [];
  filteredSessions: SessionResponse[] = [];
  selectedSession: SessionResponse | null = null;
  
  // UI state
  loading = false;
  error: string | null = null;
  showSessionModal = false;
  
  // Month/Year navigation
  monthNames = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 
                'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
  dayNames = ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'];
  
  private destroy$ = new Subject<void>();

  constructor(
    private sessionService: SessionService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.initializeUser();
    this.loadSessions();
    this.generateCalendar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private initializeUser(): void {
    if (!this.userId) {
      const userId = this.authService.getUserId();
      if (userId) {
        this.userId = userId;
      }
    }

    const userRole = this.authService.getUserRole();
    if (userRole === 'TUTOR') {
      this.userType = 'tutor';
    } else if (userRole === 'STUDENT') {
      this.userType = 'student';
    }
  }

  private loadSessions(): void {
    if (!this.userId || this.userId <= 0) {
      console.warn('Invalid userId for calendar');
      return;
    }

    this.loading = true;
    this.error = null;

    const loadObservable = this.userType === 'tutor' 
      ? this.sessionService.getSessionsByTutor(this.userId)
      : this.sessionService.getSessionsByStudent(this.userId);

    loadObservable
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (sessions) => {
          this.sessions = sessions;
          this.generateCalendar();
          this.loading = false;
        },
        error: (error) => {
          console.error('Failed to load sessions:', error);
          this.error = 'Impossible de charger les sessions';
          this.loading = false;
        }
      });
  }

  // ===============================================
  // CALENDAR GENERATION
  // ===============================================

  private generateCalendar(): void {
    switch (this.viewMode) {
      case 'month':
        this.generateMonthView();
        break;
      case 'week':
        this.generateWeekView();
        break;
      case 'day':
        this.generateDayView();
        break;
    }
  }

  private generateMonthView(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    
    const weeks: CalendarWeek[] = [];
    let currentWeek: CalendarDay[] = [];
    
    const endDate = new Date(lastDay);
    endDate.setDate(endDate.getDate() + (6 - lastDay.getDay()));
    
    const current = new Date(startDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    while (current <= endDate) {
      const date = new Date(current);
      const dayNumber = date.getDate();
      const isCurrentMonth = date.getMonth() === month;
      const isToday = date.getTime() === today.getTime();
      const isPast = date < today;
      const sessions = this.getSessionsForDate(date);
      
      currentWeek.push({
        date,
        dayNumber,
        isCurrentMonth,
        isToday,
        isPast,
        sessions
      });
      
      if (currentWeek.length === 7) {
        weeks.push({ days: currentWeek });
        currentWeek = [];
      }
      
      current.setDate(current.getDate() + 1);
    }
    
    this.weeks = weeks;
  }

  private generateWeekView(): void {
    const startOfWeek = new Date(this.currentDate);
    startOfWeek.setDate(startOfWeek.getDate() - startOfWeek.getDay());
    
    const days: CalendarDay[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(date.getDate() + i);
      
      const isToday = date.getTime() === today.getTime();
      const isPast = date < today;
      const sessions = this.getSessionsForDate(date);
      
      days.push({
        date,
        dayNumber: date.getDate(),
        isCurrentMonth: true,
        isToday,
        isPast,
        sessions
      });
    }
    
    this.weeks = [{ days }];
  }

  private generateDayView(): void {
    const date = new Date(this.currentDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const isToday = date.getTime() === today.getTime();
    const isPast = date < today;
    const sessions = this.getSessionsForDate(date);
    
    this.weeks = [{
      days: [{
        date,
        dayNumber: date.getDate(),
        isCurrentMonth: true,
        isToday,
        isPast,
        sessions
      }]
    }];
    
    this.filteredSessions = sessions.sort((a, b) => 
      new Date(a.dateHeure).getTime() - new Date(b.dateHeure).getTime()
    );
  }

  private getSessionsForDate(date: Date): SessionResponse[] {
    const targetDate = new Date(date);
    targetDate.setHours(0, 0, 0, 0);
    
    return this.sessions.filter(session => {
      const sessionDate = new Date(session.dateHeure);
      sessionDate.setHours(0, 0, 0, 0);
      return sessionDate.getTime() === targetDate.getTime();
    });
  }

  // ===============================================
  // NAVIGATION
  // ===============================================

  previousPeriod(): void {
    switch (this.viewMode) {
      case 'month':
        this.currentDate.setMonth(this.currentDate.getMonth() - 1);
        break;
      case 'week':
        this.currentDate.setDate(this.currentDate.getDate() - 7);
        break;
      case 'day':
        this.currentDate.setDate(this.currentDate.getDate() - 1);
        break;
    }
    this.currentDate = new Date(this.currentDate);
    this.generateCalendar();
  }

  nextPeriod(): void {
    switch (this.viewMode) {
      case 'month':
        this.currentDate.setMonth(this.currentDate.getMonth() + 1);
        break;
      case 'week':
        this.currentDate.setDate(this.currentDate.getDate() + 7);
        break;
      case 'day':
        this.currentDate.setDate(this.currentDate.getDate() + 1);
        break;
    }
    this.currentDate = new Date(this.currentDate);
    this.generateCalendar();
  }

  goToToday(): void {
    this.currentDate = new Date();
    this.selectedDate = new Date();
    this.generateCalendar();
  }

  changeView(view: CalendarView): void {
    this.viewMode = view;
    this.generateCalendar();
  }

  selectDate(day: CalendarDay): void {
    this.selectedDate = new Date(day.date);
    this.currentDate = new Date(day.date);
    
    if (this.viewMode === 'month') {
      this.viewMode = 'day';
      this.generateCalendar();
    }
  }

  // ===============================================
  // SESSION DETAILS
  // ===============================================

  openSessionDetails(session: SessionResponse, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.selectedSession = session;
    this.showSessionModal = true;
  }

  closeSessionModal(): void {
    this.showSessionModal = false;
    this.selectedSession = null;
  }

  // ===============================================
  // HELPER METHODS
  // ===============================================

  getCurrentPeriodTitle(): string {
    const year = this.currentDate.getFullYear();
    const month = this.monthNames[this.currentDate.getMonth()];
    
    switch (this.viewMode) {
      case 'month':
        return `${month} ${year}`;
      case 'week':
        const startOfWeek = new Date(this.currentDate);
        startOfWeek.setDate(startOfWeek.getDate() - startOfWeek.getDay());
        const endOfWeek = new Date(startOfWeek);
        endOfWeek.setDate(endOfWeek.getDate() + 6);
        
        const startMonth = this.monthNames[startOfWeek.getMonth()];
        const endMonth = this.monthNames[endOfWeek.getMonth()];
        
        if (startMonth === endMonth) {
          return `${startOfWeek.getDate()}-${endOfWeek.getDate()} ${startMonth} ${year}`;
        } else {
          return `${startOfWeek.getDate()} ${startMonth} - ${endOfWeek.getDate()} ${endMonth} ${year}`;
        }
      case 'day':
        const day = this.currentDate.getDate();
        return `${day} ${month} ${year}`;
      default:
        return '';
    }
  }

  getSessionStatusColor(status: SessionStatus): string {
    const colors = {
      [SessionStatus.DEMANDEE]: 'bg-yellow-100 text-yellow-800 border-yellow-300',
      [SessionStatus.CONFIRMEE]: 'bg-blue-100 text-blue-800 border-blue-300',
      [SessionStatus.EN_COURS]: 'bg-green-100 text-green-800 border-green-300',
      [SessionStatus.TERMINEE]: 'bg-gray-100 text-gray-800 border-gray-300',
      [SessionStatus.ANNULEE]: 'bg-red-100 text-red-800 border-red-300'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  getSessionTypeIcon(type: SessionType): string {
    return type === SessionType.EN_LIGNE ? '💻' : '🏫';
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return hours > 0 ? `${hours}h${mins > 0 ? mins : ''}` : `${mins}min`;
  }

  getSessionCount(day: CalendarDay): number {
    return day.sessions.length;
  }

  hasSessionsOnDay(day: CalendarDay): boolean {
    return day.sessions.length > 0;
  }

  refreshCalendar(): void {
    this.loadSessions();
  }
}
