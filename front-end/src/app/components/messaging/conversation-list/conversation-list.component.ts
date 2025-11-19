import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, combineLatest } from 'rxjs';

import { MessagingService } from '../../../services/messaging.service';
import {
  Conversation,
  ConversationCard,
  ConversationFilters,
  ConversationStats,
  conversationToCard,
  formatConversationTime,
  getConversationTypeLabel,
  getConversationTypeColor
} from '../../../core/models/messaging.model';

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './conversation-list.component.html',
  styleUrls: ['./conversation-list.component.css']
})
export class ConversationListComponent implements OnInit, OnDestroy {
  @Input() selectedConversationId: number | null = null;
  @Output() conversationSelected = new EventEmitter<Conversation>();
  @Output() newConversation = new EventEmitter<void>();

  // Component state
  loading = false;
  error: string | null = null;
  viewMode: 'list' | 'compact' = 'list';
  showArchived = false;

  // Data
  conversations: Conversation[] = [];
  filteredConversations: Conversation[] = [];
  conversationCards: ConversationCard[] = [];
  stats: ConversationStats | null = null;
  unreadCount = 0;

  // Search and filters
  searchForm: FormGroup;
  activeFilters: ConversationFilters = {};

  // Real-time updates
  onlineUsers: Set<number> = new Set();
  typingUsers: Map<number, string[]> = new Map();

  private destroy$ = new Subject<void>();

  constructor(
    public messagingService: MessagingService,
    private formBuilder: FormBuilder,
    private router: Router
  ) {
    this.searchForm = this.createSearchForm();
  }

  ngOnInit(): void {
    this.setupSubscriptions();
    this.setupFormSubscriptions();
    this.loadConversations();
    this.loadStats();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private createSearchForm(): FormGroup {
    return this.formBuilder.group({
      searchQuery: [''],
      onlyUnread: [false],
      isSupport: [''],
      isGroup: [''],
      dateFrom: [''],
      dateTo: ['']
    });
  }

  private setupSubscriptions(): void {
    // Conversations data
    this.messagingService.conversations$
      .pipe(takeUntil(this.destroy$))
      .subscribe(conversations => {
        this.conversations = conversations;
        this.conversationCards = conversations.map(conv => conversationToCard(conv));
        this.applyFilters();
      });

    // Loading state
    this.messagingService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => this.loading = loading);

    // Error state
    this.messagingService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => this.error = error);

    // Statistics
    this.messagingService.conversationStats$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => this.stats = stats);

    // Unread count
    this.messagingService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);

    // Online users
    this.messagingService.onlineUsers$
      .pipe(takeUntil(this.destroy$))
      .subscribe(users => {
        this.onlineUsers = new Set(users.filter(u => u.isOnline).map(u => u.userId));
      });

    // Typing indicators
    this.messagingService.typingIndicators$
      .pipe(takeUntil(this.destroy$))
      .subscribe(indicators => {
        this.typingUsers.clear();
        indicators.forEach(indicator => {
          if (indicator.isTyping) {
            const existing = this.typingUsers.get(indicator.conversationId) || [];
            this.typingUsers.set(indicator.conversationId, [...existing, indicator.userName]);
          }
        });
      });

    // Real-time chat events
    this.messagingService.chatEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe(event => {
        // Handle real-time updates that might affect the conversation list
        if (event.type === 'RECEIVE_MESSAGE' || event.type === 'CONVERSATION_UPDATED') {
          // Conversations will be updated via the conversations$ observable
        }
      });
  }

  private setupFormSubscriptions(): void {
    this.searchForm.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(formValue => {
        this.activeFilters = {
          searchQuery: formValue.searchQuery,
          onlyUnread: formValue.onlyUnread,
          includeArchived: this.showArchived,
          isSupport: formValue.isSupport !== '' ? formValue.isSupport === 'true' : undefined,
          isGroup: formValue.isGroup !== '' ? formValue.isGroup === 'true' : undefined,
          dateFrom: formValue.dateFrom || undefined,
          dateTo: formValue.dateTo || undefined
        };
        this.applyFilters();
      });
  }

  // ===============================================
  // DATA LOADING
  // ===============================================

  private loadConversations(): void {
    this.messagingService.getUserConversations(this.showArchived).subscribe();
  }

  private loadStats(): void {
    this.messagingService.getConversationStats().subscribe();
  }

  public refreshConversations(): void {
    this.loadConversations();
    this.loadStats();
  }

  // ===============================================
  // FILTERING AND SEARCH
  // ===============================================

  private applyFilters(): void {
    this.messagingService.filterConversations(this.activeFilters)
      .pipe(takeUntil(this.destroy$))
      .subscribe(filtered => {
        this.filteredConversations = filtered.sort((a, b) => {
          // Sort by last message date, unread first
          if (a.unreadMessages > 0 && b.unreadMessages === 0) return -1;
          if (a.unreadMessages === 0 && b.unreadMessages > 0) return 1;
          
          const dateA = new Date(a.lastMessageDate || a.dateCreation);
          const dateB = new Date(b.lastMessageDate || b.dateCreation);
          return dateB.getTime() - dateA.getTime();
        });
      });
  }

  public clearSearch(): void {
    this.searchForm.reset();
    this.activeFilters = { includeArchived: this.showArchived };
    this.applyFilters();
  }

  public toggleArchived(): void {
    this.showArchived = !this.showArchived;
    this.activeFilters.includeArchived = this.showArchived;
    this.loadConversations();
  }

  public setViewMode(mode: 'list' | 'compact'): void {
    this.viewMode = mode;
  }

  // ===============================================
  // CONVERSATION ACTIONS
  // ===============================================

  public selectConversation(conversation: Conversation): void {
    this.selectedConversationId = conversation.id;
    this.conversationSelected.emit(conversation);
    
    // Mark as read if it has unread messages
    if (conversation.unreadMessages > 0) {
      this.messagingService.markMessagesAsRead(conversation.id).subscribe();
    }
  }

  public createNewConversation(): void {
    this.newConversation.emit();
  }

  public archiveConversation(conversation: Conversation, event: Event): void {
    event.stopPropagation();
    
    const archived = !conversation.archivee;
    this.messagingService.archiveConversation(conversation.id, archived).subscribe({
      next: () => {
        // Success feedback could be added here
      },
      error: (error) => {
        console.error('Failed to archive conversation:', error);
      }
    });
  }

  public deleteConversation(conversation: Conversation, event: Event): void {
    event.stopPropagation();
    
    if (confirm('Êtes-vous sûr de vouloir quitter cette conversation ?')) {
      this.messagingService.leaveConversation(conversation.id).subscribe({
        next: () => {
          // Success feedback could be added here
        },
        error: (error) => {
          console.error('Failed to leave conversation:', error);
        }
      });
    }
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  public formatTime = formatConversationTime;
  public getTypeLabel = getConversationTypeLabel;
  public getTypeColor = getConversationTypeColor;

  public getConversationTitle(conversation: Conversation): string {
    if (conversation.sujet && conversation.sujet.trim()) {
      return conversation.sujet;
    }
    
    // Generate title from participants
    const currentUserId = this.getCurrentUserId();
    const otherParticipants = conversation.participants.filter(p => p.userId !== currentUserId);
    
    if (otherParticipants.length === 0) {
      return 'Conversation vide';
    }
    
    if (otherParticipants.length === 1) {
      return `${otherParticipants[0].prenom} ${otherParticipants[0].nom}`;
    }
    
    return `${otherParticipants[0].prenom} ${otherParticipants[0].nom} et ${otherParticipants.length - 1} autres`;
  }

  public getConversationSubtitle(conversation: Conversation): string {
    if (conversation.lastMessageContent) {
      return conversation.lastMessageContent.length > 50 
        ? conversation.lastMessageContent.substring(0, 50) + '...'
        : conversation.lastMessageContent;
    }
    
    if (conversation.totalMessages === 0) {
      return 'Aucun message';
    }
    
    return `${conversation.totalMessages} message${conversation.totalMessages > 1 ? 's' : ''}`;
  }

  public getParticipantAvatars(conversation: Conversation): string[] {
    const currentUserId = this.getCurrentUserId();
    return conversation.participants
      .filter(p => p.userId !== currentUserId)
      .slice(0, 3)
      .map(p => this.getAvatarUrl(p.userId, `${p.prenom} ${p.nom}`));
  }

  public isConversationOnline(conversation: Conversation): boolean {
    const currentUserId = this.getCurrentUserId();
    return conversation.participants
      .filter(p => p.userId !== currentUserId)
      .some(p => this.onlineUsers.has(p.userId));
  }

  public getTypingIndicator(conversationId: number): string {
    const typingInConversation = this.typingUsers.get(conversationId);
    if (!typingInConversation || typingInConversation.length === 0) {
      return '';
    }
    
    if (typingInConversation.length === 1) {
      return `${typingInConversation[0]} tape...`;
    }
    
    if (typingInConversation.length === 2) {
      return `${typingInConversation[0]} et ${typingInConversation[1]} tapent...`;
    }
    
    return `${typingInConversation.length} personnes tapent...`;
  }

  public hasActiveFilters(): boolean {
    return !!(
      this.activeFilters.searchQuery ||
      this.activeFilters.onlyUnread ||
      this.activeFilters.isSupport !== undefined ||
      this.activeFilters.isGroup !== undefined ||
      this.activeFilters.dateFrom ||
      this.activeFilters.dateTo
    );
  }

  public getFilterCount(): number {
    let count = 0;
    if (this.activeFilters.searchQuery) count++;
    if (this.activeFilters.onlyUnread) count++;
    if (this.activeFilters.isSupport !== undefined) count++;
    if (this.activeFilters.isGroup !== undefined) count++;
    if (this.activeFilters.dateFrom) count++;
    if (this.activeFilters.dateTo) count++;
    return count;
  }

  private getCurrentUserId(): number {
    // TODO: Get from auth service
    return 1; // Placeholder
  }

  private getAvatarUrl(userId: number, userName: string): string {
    // Generate avatar URL - could be from a service or use initials
    const initials = userName.split(' ').map(n => n[0]).join('').toUpperCase();
    return `https://ui-avatars.com/api/?name=${initials}&background=3B82F6&color=fff&size=40`;
  }

  public trackConversation(index: number, conversation: Conversation): number {
    return conversation.id;
  }
}
