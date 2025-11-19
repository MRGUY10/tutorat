import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

import { SessionService } from '../../../services/session.service';
import { AuthService } from '../../../services/auth.service';
import {
  SessionRequestCard,
  SessionRequestDetails,
  SessionRequestFilters,
  RequestStatus,
  Urgence
} from '../../../core/models/session.model';

@Component({
  selector: 'app-session-requests',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './session-requests.component.html',
  styleUrls: ['./session-requests.component.css']
})
export class SessionRequestsComponent implements OnInit, OnDestroy {
  // Component state
  requests: SessionRequestCard[] = [];
  filteredRequests: SessionRequestCard[] = [];
  selectedRequest: SessionRequestDetails | null = null;
  loading = false;
  error: string | null = null;
  
  // User info
  userId!: number;
  userType: 'tutor' | 'student' = 'student';
  
  // UI state
  showFilters = false;
  viewMode: 'list' | 'grid' = 'list';
  filterStatus: RequestStatus | 'ALL' = 'ALL';
  sortBy: 'date' | 'urgency' | 'budget' = 'date';
  sortDirection: 'asc' | 'desc' = 'desc';
  
  // Response modal
  showResponseModal = false;
  responseType: 'accept' | 'reject' = 'accept';
  responseMessage = '';
  requestToRespond: SessionRequestCard | SessionRequestDetails | null = null;
  requestIdToRespond: number | null = null;
  
  // Detail modal
  showDetailModal = false;
  
  // Enums for template
  RequestStatus = RequestStatus;
  Urgence = Urgence;
  
  private destroy$ = new Subject<void>();

  constructor(
    private sessionService: SessionService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initializeUser();
    this.loadRequests();
    this.subscribeToRequests();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private initializeUser(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      this.userId = userId;
    }

    const userRole = this.authService.getUserRole();
    if (userRole === 'TUTOR') {
      this.userType = 'tutor';
    } else if (userRole === 'STUDENT') {
      this.userType = 'student';
    }
  }

  private subscribeToRequests(): void {
    this.sessionService.requestCards$
      .pipe(takeUntil(this.destroy$))
      .subscribe(requests => {
        this.requests = requests;
        this.applyFilters();
      });

    this.sessionService.requestsLoading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => this.loading = loading);

    this.sessionService.requestsError$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => this.error = error);
  }

  // ===============================================
  // DATA LOADING
  // ===============================================

  loadRequests(): void {
    if (!this.userId) {
      console.error('User ID not found');
      return;
    }

    const filters: SessionRequestFilters = {};

    if (this.userType === 'tutor') {
      this.sessionService.getSessionRequestsByTutor(this.userId, filters)
        .pipe(takeUntil(this.destroy$))
        .subscribe();
    } else {
      this.sessionService.getSessionRequestsByStudent(this.userId, filters)
        .pipe(takeUntil(this.destroy$))
        .subscribe();
    }
  }

  refreshRequests(): void {
    this.loadRequests();
  }

  // ===============================================
  // FILTERING AND SORTING
  // ===============================================

  applyFilters(): void {
    let filtered = [...this.requests];

    // Filter by status
    if (this.filterStatus !== 'ALL') {
      filtered = filtered.filter(req => req.status === this.filterStatus);
    }

    // Sort
    filtered.sort((a, b) => {
      let comparison = 0;

      switch (this.sortBy) {
        case 'date':
          comparison = new Date(a.requestedDate).getTime() - new Date(b.requestedDate).getTime();
          break;
        case 'urgency':
          const urgencyOrder: Record<Urgence, number> = { 
            [Urgence.HAUTE]: 3, 
            [Urgence.MOYENNE]: 2, 
            [Urgence.FAIBLE]: 1 
          };
          comparison = urgencyOrder[a.urgency] - urgencyOrder[b.urgency];
          break;
        case 'budget':
          comparison = a.budget - b.budget;
          break;
      }

      return this.sortDirection === 'asc' ? comparison : -comparison;
    });

    this.filteredRequests = filtered;
  }

  onFilterChange(status: RequestStatus | 'ALL'): void {
    this.filterStatus = status;
    this.applyFilters();
  }

  onSortChange(sortBy: 'date' | 'urgency' | 'budget'): void {
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDirection = 'desc';
    }
    this.applyFilters();
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'list' ? 'grid' : 'list';
  }

  // ===============================================
  // REQUEST ACTIONS
  // ===============================================

  viewRequestDetails(request: SessionRequestCard): void {
    this.loading = true;
    this.sessionService.getDetailedSessionRequestById(request.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (details) => {
          this.selectedRequest = details;
          this.showDetailModal = true;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading request details:', error);
          this.error = 'Failed to load request details';
          this.loading = false;
        }
      });
  }

  openResponseModal(request: SessionRequestCard | SessionRequestDetails, type: 'accept' | 'reject'): void {
    this.requestToRespond = request;
    this.requestIdToRespond = request.id;
    this.responseType = type;
    this.responseMessage = '';
    this.showResponseModal = true;
  }

  submitResponse(): void {
    if (!this.requestIdToRespond) return;

    this.loading = true;

    if (this.responseType === 'accept') {
      this.sessionService.acceptSessionRequest(this.requestIdToRespond, this.responseMessage)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.showResponseModal = false;
            this.responseMessage = '';
            this.requestToRespond = null;
            this.requestIdToRespond = null;
            this.loading = false;
            this.loadRequests();
          },
          error: (error) => {
            console.error('Error accepting request:', error);
            this.error = 'Failed to accept request';
            this.loading = false;
          }
        });
    } else {
      this.sessionService.rejectSessionRequest(this.requestIdToRespond, this.responseMessage)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.showResponseModal = false;
            this.responseMessage = '';
            this.requestToRespond = null;
            this.requestIdToRespond = null;
            this.loading = false;
            this.loadRequests();
          },
          error: (error) => {
            console.error('Error rejecting request:', error);
            this.error = 'Failed to reject request';
            this.loading = false;
          }
        });
    }
  }

  closeResponseModal(): void {
    this.showResponseModal = false;
    this.responseMessage = '';
    this.requestToRespond = null;
    this.requestIdToRespond = null;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedRequest = null;
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  getStatusDisplay(status: RequestStatus): string {
    const statusMap: Record<RequestStatus, string> = {
      [RequestStatus.EN_ATTENTE]: 'En attente',
      [RequestStatus.ACCEPTEE]: 'Acceptée',
      [RequestStatus.REFUSEE]: 'Refusée'
    };
    return statusMap[status] || status;
  }

  getStatusColor(status: RequestStatus): string {
    const colorMap: Record<RequestStatus, string> = {
      [RequestStatus.EN_ATTENTE]: 'bg-yellow-100 text-yellow-800',
      [RequestStatus.ACCEPTEE]: 'bg-green-100 text-green-800',
      [RequestStatus.REFUSEE]: 'bg-red-100 text-red-800'
    };
    return colorMap[status] || 'bg-gray-100 text-gray-800';
  }

  getUrgencyDisplay(urgency: Urgence): string {
    const urgencyMap: Record<Urgence, string> = {
      [Urgence.HAUTE]: 'Haute',
      [Urgence.MOYENNE]: 'Moyenne',
      [Urgence.FAIBLE]: 'Faible'
    };
    return urgencyMap[urgency] || urgency;
  }

  getUrgencyColor(urgency: Urgence): string {
    const colorMap: Record<Urgence, string> = {
      [Urgence.HAUTE]: 'bg-red-100 text-red-800',
      [Urgence.MOYENNE]: 'bg-orange-100 text-orange-800',
      [Urgence.FAIBLE]: 'bg-blue-100 text-blue-800'
    };
    return colorMap[urgency] || 'bg-gray-100 text-gray-800';
  }

  formatDate(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h${mins > 0 ? ` ${mins}min` : ''}`;
    }
    return `${mins}min`;
  }

  getPendingCount(): number {
    return this.requests.filter(r => r.status === RequestStatus.EN_ATTENTE).length;
  }

  getAcceptedCount(): number {
    return this.requests.filter(r => r.status === RequestStatus.ACCEPTEE).length;
  }

  getRejectedCount(): number {
    return this.requests.filter(r => r.status === RequestStatus.REFUSEE).length;
  }
}
