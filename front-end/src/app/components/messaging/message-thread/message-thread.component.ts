import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject as RxJSSubject, takeUntil, combineLatest, fromEvent, debounceTime } from 'rxjs';

import { MessagingService } from '../../../services/messaging.service';
import {
  ConversationResponse,
  MessageCard,
  TypingIndicator,
  CreateMessage,
  MessageType,
  formatMessageTime,
  isImageFile,
  getFileIcon
} from '../../../core/models/messaging.model';

@Component({
  selector: 'app-message-thread',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './message-thread.component.html',
  styleUrls: ['./message-thread.component.css']
})
export class MessageThreadComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() conversationId: number | null = null;
  @Output() backRequested = new EventEmitter<void>();
  @Output() conversationInfoRequested = new EventEmitter<void>();

  @ViewChild('messagesContainer', { static: false }) messagesContainer!: ElementRef;
  @ViewChild('messageInput', { static: false }) messageInput!: ElementRef;

  // Component state
  conversation: ConversationResponse | null = null;
  messages: MessageCard[] = [];
  typingUsers: TypingIndicator[] = [];
  loading = false;
  error: string | null = null;
  
  // Message composition
  messageText = '';
  isTyping = false;
  replyToMessage: MessageCard | null = null;
  
  // UI state
  showScrollToBottom = false;
  isNearBottom = true;
  hasMoreMessages = true;
  loadingMore = false;
  
  // File handling
  selectedFiles: File[] = [];
  dragActive = false;

  private destroy$ = new RxJSSubject<void>();
  private typingTimeout: any;
  private scrollDebounce: any;

  constructor(private messagingService: MessagingService) {}

  ngOnInit(): void {
    this.setupSubscriptions();
    this.loadConversationData();
  }

  ngAfterViewInit(): void {
    this.setupScrollListener();
    this.scrollToBottom(false);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.clearTypingTimeout();
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private setupSubscriptions(): void {
    // Active conversation
    this.messagingService.activeConversation$
      .pipe(takeUntil(this.destroy$))
      .subscribe(conversation => {
        this.conversation = conversation;
        if (conversation && conversation.id === this.conversationId) {
          this.scrollToBottom();
        }
      });

    // Messages
    this.messagingService.messageCards$
      .pipe(takeUntil(this.destroy$))
      .subscribe(messages => {
        const wasAtBottom = this.isNearBottom;
        this.messages = messages;
        
        if (wasAtBottom) {
          setTimeout(() => this.scrollToBottom(), 100);
        } else {
          this.checkScrollPosition();
        }
      });

    // Typing indicators
    combineLatest([
      this.messagingService.typingIndicators$,
      this.messagingService.activeConversation$
    ]).pipe(takeUntil(this.destroy$))
      .subscribe(([indicators, activeConv]) => {
        if (activeConv) {
          this.typingUsers = indicators.filter(t => 
            t.conversationId === activeConv.id && 
            t.userId !== this.getCurrentUserId()
          );
        }
      });

    // Loading state
    this.messagingService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.loading = loading;
      });

    // Error state
    this.messagingService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.error = error;
      });
  }

  private loadConversationData(): void {
    if (this.conversationId) {
      this.messagingService.setActiveConversation(this.conversationId);
    }
  }

  private setupScrollListener(): void {
    if (this.messagesContainer) {
      fromEvent(this.messagesContainer.nativeElement, 'scroll')
        .pipe(
          debounceTime(100),
          takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.checkScrollPosition();
        });
    }
  }

  // ===============================================
  // MESSAGE DISPLAY
  // ===============================================

  private checkScrollPosition(): void {
    if (!this.messagesContainer) return;

    const element = this.messagesContainer.nativeElement;
    const threshold = 100;
    const position = element.scrollTop + element.clientHeight;
    const height = element.scrollHeight;

    this.isNearBottom = position >= height - threshold;
    this.showScrollToBottom = !this.isNearBottom && this.messages.length > 5;

    // Load more messages if scrolled to top
    if (element.scrollTop === 0 && this.hasMoreMessages && !this.loadingMore) {
      this.loadMoreMessages();
    }
  }

  private scrollToBottom(smooth = true): void {
    if (!this.messagesContainer) return;

    setTimeout(() => {
      const element = this.messagesContainer.nativeElement;
      const scrollTop = element.scrollHeight - element.clientHeight;
      
      if (smooth) {
        element.scrollTo({
          top: scrollTop,
          behavior: 'smooth'
        });
      } else {
        element.scrollTop = scrollTop;
      }
      
      this.showScrollToBottom = false;
    }, 50);
  }

  public onScrollToBottom(): void {
    this.scrollToBottom();
  }

  private loadMoreMessages(): void {
    if (!this.conversationId || this.loadingMore || !this.hasMoreMessages) return;

    this.loadingMore = true;
    const currentScrollHeight = this.messagesContainer.nativeElement.scrollHeight;

    // Calculate next page based on current message count
    const page = Math.floor(this.messages.length / 50) + 1;

    this.messagingService.loadMessages(this.conversationId, page, 50)
      .subscribe(newMessages => {
        this.loadingMore = false;
        this.hasMoreMessages = newMessages.length === 50;

        // Maintain scroll position after loading more messages
        setTimeout(() => {
          const element = this.messagesContainer.nativeElement;
          const newScrollHeight = element.scrollHeight;
          element.scrollTop = newScrollHeight - currentScrollHeight;
        }, 100);
      });
  }

  // ===============================================
  // MESSAGE COMPOSITION
  // ===============================================

  public onMessageInput(event: any): void {
    this.messageText = event.target.value;
    this.handleTypingIndicator();
  }

  private handleTypingIndicator(): void {
    if (!this.conversationId) return;

    if (!this.isTyping && this.messageText.trim()) {
      this.isTyping = true;
      this.messagingService.sendTypingIndicator(this.conversationId, true);
    }

    // Clear previous timeout
    this.clearTypingTimeout();

    // Set new timeout to stop typing indicator
    this.typingTimeout = setTimeout(() => {
      if (this.isTyping) {
        this.isTyping = false;
        if (this.conversationId) {
          this.messagingService.sendTypingIndicator(this.conversationId, false);
        }
      }
    }, 3000);
  }

  private clearTypingTimeout(): void {
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
      this.typingTimeout = null;
    }
  }

  public onSendMessage(): void {
    if (!this.canSendMessage()) return;

    const messageData: CreateMessage = {
      conversationId: this.conversationId!,
      contenu: this.messageText.trim(),
      typeMessage: MessageType.TEXTE,
      replyToMessageId: this.replyToMessage?.id
    };

    this.messagingService.sendMessage(messageData).subscribe(message => {
      if (message) {
        this.messageText = '';
        this.replyToMessage = null;
        this.clearTypingIndicator();
        this.scrollToBottom();
        
        // Focus back to input
        if (this.messageInput) {
          this.messageInput.nativeElement.focus();
        }
      }
    });
  }

  private clearTypingIndicator(): void {
    if (this.isTyping && this.conversationId) {
      this.isTyping = false;
      this.messagingService.sendTypingIndicator(this.conversationId, false);
    }
    this.clearTypingTimeout();
  }

  public onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSendMessage();
    }
  }

  public canSendMessage(): boolean {
    return !!(this.conversationId && this.messageText.trim() && !this.loading);
  }

  // ===============================================
  // MESSAGE ACTIONS
  // ===============================================

  public onReplyToMessage(message: MessageCard): void {
    this.replyToMessage = message;
    if (this.messageInput) {
      this.messageInput.nativeElement.focus();
    }
  }

  public onCancelReply(): void {
    this.replyToMessage = null;
  }

  public onCopyMessage(message: MessageCard): void {
    navigator.clipboard.writeText(message.content).then(() => {
      // Show success feedback (could be implemented with a toast service)
      console.log('Message copied to clipboard');
    });
  }

  public onDeleteMessage(message: MessageCard): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce message ?')) {
      // Implementation would call the messaging service to delete the message
      console.log('Delete message:', message.id);
    }
  }

  // ===============================================
  // FILE HANDLING
  // ===============================================

  public onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
      this.processFiles();
    }
  }

  public onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragActive = true;
  }

  public onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.dragActive = false;
  }

  public onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragActive = false;
    
    if (event.dataTransfer?.files) {
      this.selectedFiles = Array.from(event.dataTransfer.files);
      this.processFiles();
    }
  }

  private processFiles(): void {
    // Here you would implement file upload logic
    // For now, just log the files
    console.log('Files selected:', this.selectedFiles);
    
    // Clear the selection after processing
    this.selectedFiles = [];
  }

  public removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  private getCurrentUserId(): number {
    // Get from localStorage or auth service
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
      const user = JSON.parse(userStr);
      return user.id;
    }
    return 1; // Default for testing
  }

  public getConversationTitle(): string {
    if (!this.conversation) return 'Conversation';
    
    if (this.conversation.isSupport) {
      return 'Support Technique';
    }
    
    if (this.conversation.participants.length > 2) {
      return this.conversation.sujet || 'Conversation de groupe';
    }
    
    const currentUserId = this.getCurrentUserId();
    const otherParticipant = this.conversation.participants.find(p => p.userId !== currentUserId);
    return otherParticipant ? `${otherParticipant.prenom} ${otherParticipant.nom}` : this.conversation.sujet;
  }

  public getConversationSubtitle(): string {
    if (!this.conversation) return '';
    
    if (this.conversation.isSupport) {
      return 'Assistance client';
    }
    
    const participantCount = this.conversation.participants.length;
    if (participantCount > 2) {
      return `${participantCount} participants`;
    }
    
    const currentUserId = this.getCurrentUserId();
    const otherParticipant = this.conversation.participants.find(p => p.userId !== currentUserId);
    if (otherParticipant) {
      const isOnline = false; // Would be determined from online users
      return isOnline ? 'En ligne' : 'Hors ligne';
    }
    
    return '';
  }

  public getTypingText(): string {
    if (this.typingUsers.length === 0) return '';
    
    if (this.typingUsers.length === 1) {
      return `${this.typingUsers[0].userName} écrit...`;
    }
    
    if (this.typingUsers.length === 2) {
      return `${this.typingUsers[0].userName} et ${this.typingUsers[1].userName} écrivent...`;
    }
    
    return `${this.typingUsers.length} personnes écrivent...`;
  }

  public formatTime = formatMessageTime;
  public isImageFile = isImageFile;
  public getFileIcon = getFileIcon;

  public shouldShowDateSeparator(message: MessageCard, index: number): boolean {
    if (index === 0) return true;
    
    const currentDate = message.timestamp.toDateString();
    const previousDate = this.messages[index - 1].timestamp.toDateString();
    
    return currentDate !== previousDate;
  }

  public shouldGroupMessage(message: MessageCard, index: number): boolean {
    if (index === 0) return false;
    
    const previous = this.messages[index - 1];
    const timeDiff = message.timestamp.getTime() - previous.timestamp.getTime();
    const maxGroupTime = 5 * 60 * 1000; // 5 minutes
    
    return previous.senderName === message.senderName && 
           timeDiff < maxGroupTime &&
           !this.shouldShowDateSeparator(message, index);
  }

  public getDateSeparatorText(date: Date): string {
    const today = new Date();
    const yesterday = new Date(today.getTime() - 86400000);
    
    if (date.toDateString() === today.toDateString()) {
      return 'Aujourd\'hui';
    }
    
    if (date.toDateString() === yesterday.toDateString()) {
      return 'Hier';
    }
    
    return new Intl.DateTimeFormat('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: date.getFullYear() !== today.getFullYear() ? 'numeric' : undefined
    }).format(date);
  }

  public onBack(): void {
    this.backRequested.emit();
  }

  public onShowInfo(): void {
    this.conversationInfoRequested.emit();
  }

  public clearError(): void {
    this.messagingService.clearError();
  }

  // ===============================================
  // PERFORMANCE TRACKING
  // ===============================================

  public trackMessage(index: number, message: MessageCard): number {
    return message.id;
  }
}