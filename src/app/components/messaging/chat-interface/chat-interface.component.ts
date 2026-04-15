import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, fromEvent } from 'rxjs';

import { MessagingService } from '../../../services/messaging.service';
import {
  Conversation,
  MessageResponse,
  CreateMessage,
  MessageDisplay,
  MessageType,
  TypingIndicator,
  messageToDisplay,
  formatMessageTime,
  getMessageTypeIcon,
  canUserModifyMessage
} from '../../../core/models/messaging.model';

@Component({
  selector: 'app-chat-interface',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './chat-interface.component.html',
  styleUrls: ['./chat-interface.component.css']
})
export class ChatInterfaceComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() conversation: Conversation | null = null;
  @Input() currentUserId!: number;
  @Output() conversationUpdated = new EventEmitter<Conversation>();
  @Output() backToList = new EventEmitter<void>();

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  @ViewChild('messageInput') messageInput!: ElementRef;

  // Component state
  loading = false;
  error: string | null = null;
  isTyping = false;
  showEmojiPicker = false;
  showAttachmentMenu = false;

  // Messages and display
  messages: MessageResponse[] = [];
  messageDisplays: MessageDisplay[] = [];
  typingIndicators: TypingIndicator[] = [];
  replyToMessage: MessageDisplay | null = null;

  // Forms
  messageForm: FormGroup;
  isComposing = false;

  // Pagination and loading
  hasMoreMessages = true;
  loadingMore = false;
  currentPage = 0;
  pageSize = 50;

  // File upload
  uploadingFiles: File[] = [];
  uploadProgress: Map<string, number> = new Map();

  // Auto-scroll management
  shouldAutoScroll = true;
  isAtBottom = true;

  private destroy$ = new Subject<void>();
  private typingTimeout: any;
  private markAsReadTimeout: any;

  constructor(
    public messagingService: MessagingService,
    private formBuilder: FormBuilder
  ) {
    this.messageForm = this.createMessageForm();
  }

  ngOnInit(): void {
    this.setupSubscriptions();
    this.setupFormSubscriptions();
    if (this.conversation) {
      this.loadMessages();
    }
  }

  ngAfterViewInit(): void {
    this.setupScrollListener();
    this.focusMessageInput();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.clearTypingTimeout();
    this.clearMarkAsReadTimeout();
  }

  // ===============================================
  // INITIALIZATION
  // ===============================================

  private createMessageForm(): FormGroup {
    return this.formBuilder.group({
      content: ['', [Validators.required, Validators.maxLength(2000)]],
      attachments: [[]]
    });
  }

  private setupSubscriptions(): void {
    // Messages
    this.messagingService.messages$
      .pipe(takeUntil(this.destroy$))
      .subscribe(messages => {
        this.messages = messages;
        this.messageDisplays = messages.map(msg => messageToDisplay(msg, this.currentUserId));
        
        if (this.shouldAutoScroll) {
          setTimeout(() => this.scrollToBottom(), 100);
        }
        
        // Mark messages as read if at bottom
        if (this.isAtBottom && this.conversation) {
          this.scheduleMarkAsRead();
        }
      });

    // Loading state
    this.messagingService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => this.loading = loading);

    // Error state
    this.messagingService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => this.error = error);

    // Typing indicators
    this.messagingService.typingIndicators$
      .pipe(takeUntil(this.destroy$))
      .subscribe(indicators => {
        if (this.conversation) {
          this.typingIndicators = indicators.filter(
            ind => ind.conversationId === this.conversation!.id && ind.userId !== this.currentUserId
          );
          
          if (this.typingIndicators.length > 0 && this.shouldAutoScroll) {
            setTimeout(() => this.scrollToBottom(), 100);
          }
        }
      });

    // Real-time chat events
    this.messagingService.chatEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe(event => {
        if (event.type === 'RECEIVE_MESSAGE' && event.message) {
          // Auto-scroll for new messages from others if user is at bottom
          if (event.message.expediteurId !== this.currentUserId && this.isAtBottom) {
            setTimeout(() => this.scrollToBottom(true), 100);
          }
        }
      });
  }

  private setupFormSubscriptions(): void {
    // Typing indicator
    this.messageForm.get('content')?.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(value => {
        if (this.conversation) {
          if (value && value.trim() && !this.isTyping) {
            this.startTyping();
          } else if ((!value || !value.trim()) && this.isTyping) {
            this.stopTyping();
          }
        }
      });

    // Auto-save draft (future enhancement)
    this.messageForm.valueChanges
      .pipe(
        debounceTime(1000),
        takeUntil(this.destroy$)
      )
      .subscribe(value => {
        if (value.content && value.content.trim()) {
          this.saveDraft(value.content);
        }
      });
  }

  private setupScrollListener(): void {
    if (this.messagesContainer) {
      fromEvent(this.messagesContainer.nativeElement, 'scroll')
        .pipe(
          debounceTime(100),
          takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.handleScroll();
        });
    }
  }

  // ===============================================
  // MESSAGE LOADING
  // ===============================================

  private loadMessages(): void {
    if (!this.conversation) return;

    this.messagingService.getRecentMessages(this.conversation.id, this.pageSize)
      .subscribe({
        next: () => {
          setTimeout(() => this.scrollToBottom(), 100);
          this.scheduleMarkAsRead();
        },
        error: (error) => {
          console.error('Failed to load messages:', error);
        }
      });
  }

  public loadMoreMessages(): void {
    if (!this.conversation || this.loadingMore || !this.hasMoreMessages) return;

    this.loadingMore = true;
    this.currentPage++;

    this.messagingService.getConversationMessages(this.conversation.id, {
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (messages) => {
        this.loadingMore = false;
        this.hasMoreMessages = messages.length === this.pageSize;
        
        // Maintain scroll position
        const scrollHeight = this.messagesContainer.nativeElement.scrollHeight;
        setTimeout(() => {
          const newScrollHeight = this.messagesContainer.nativeElement.scrollHeight;
          this.messagesContainer.nativeElement.scrollTop = newScrollHeight - scrollHeight;
        }, 100);
      },
      error: (error) => {
        this.loadingMore = false;
        console.error('Failed to load more messages:', error);
      }
    });
  }

  // ===============================================
  // MESSAGE SENDING
  // ===============================================

  public sendMessage(): void {
    if (!this.messageForm.valid || !this.conversation) return;

    const content = this.messageForm.get('content')?.value?.trim();
    if (!content) return;

    const message: CreateMessage = {
      conversationId: this.conversation.id,
      contenu: content,
      typeMessage: MessageType.TEXTE,
      replyToMessageId: this.replyToMessage?.id
    };

    this.messagingService.sendMessage(message).subscribe({
      next: () => {
        this.messageForm.reset();
        this.clearReply();
        this.stopTyping();
        this.focusMessageInput();
        setTimeout(() => this.scrollToBottom(true), 100);
      },
      error: (error) => {
        console.error('Failed to send message:', error);
        this.error = 'Échec de l\'envoi du message';
      }
    });
  }

  public sendQuickReply(text: string): void {
    if (!this.conversation) return;

    const message: CreateMessage = {
      conversationId: this.conversation.id,
      contenu: text,
      typeMessage: MessageType.TEXTE
    };

    this.messagingService.sendMessage(message).subscribe();
  }

  // ===============================================
  // TYPING INDICATORS
  // ===============================================

  private startTyping(): void {
    if (!this.conversation || this.isTyping) return;

    this.isTyping = true;
    this.messagingService.startTyping(this.conversation.id);
    
    // Auto-stop typing after 5 seconds
    this.typingTimeout = setTimeout(() => {
      this.stopTyping();
    }, 5000);
  }

  private stopTyping(): void {
    if (!this.conversation || !this.isTyping) return;

    this.isTyping = false;
    this.messagingService.stopTyping(this.conversation.id);
    this.clearTypingTimeout();
  }

  private clearTypingTimeout(): void {
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
      this.typingTimeout = null;
    }
  }

  // ===============================================
  // MESSAGE ACTIONS
  // ===============================================

  public setReplyToMessage(message: MessageDisplay): void {
    this.replyToMessage = message;
    this.focusMessageInput();
  }

  public clearReply(): void {
    this.replyToMessage = null;
  }

  public editMessage(message: MessageDisplay): void {
    // TODO: Implement message editing
    console.log('Edit message:', message.id);
  }

  public deleteMessage(message: MessageDisplay): void {
    if (!canUserModifyMessage(this.messages.find(m => m.id === message.id)!, this.currentUserId)) {
      return;
    }

    if (confirm('Êtes-vous sûr de vouloir supprimer ce message ?')) {
      this.messagingService.deleteMessage(message.id).subscribe({
        next: () => {
          // Message will be removed from state automatically
        },
        error: (error) => {
          console.error('Failed to delete message:', error);
          this.error = 'Échec de la suppression du message';
        }
      });
    }
  }

  public reactToMessage(message: MessageDisplay, emoji: string): void {
    // TODO: Implement message reactions
    console.log('React to message:', message.id, emoji);
  }

  // ===============================================
  // FILE HANDLING
  // ===============================================

  public onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      Array.from(input.files).forEach(file => {
        this.uploadFile(file);
      });
    }
  }

  public onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  public onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    
    if (event.dataTransfer?.files) {
      Array.from(event.dataTransfer.files).forEach(file => {
        this.uploadFile(file);
      });
    }
  }

  private uploadFile(file: File): void {
    // TODO: Implement file upload
    console.log('Upload file:', file.name);
    this.uploadingFiles.push(file);
    
    // Simulate upload progress
    const fileId = file.name + Date.now();
    this.uploadProgress.set(fileId, 0);
    
    const interval = setInterval(() => {
      const current = this.uploadProgress.get(fileId) || 0;
      if (current >= 100) {
        clearInterval(interval);
        this.uploadProgress.delete(fileId);
        this.uploadingFiles = this.uploadingFiles.filter(f => f !== file);
      } else {
        this.uploadProgress.set(fileId, current + 10);
      }
    }, 200);
  }

  // ===============================================
  // SCROLL MANAGEMENT
  // ===============================================

  private handleScroll(): void {
    const container = this.messagesContainer.nativeElement;
    const scrollTop = container.scrollTop;
    const scrollHeight = container.scrollHeight;
    const clientHeight = container.clientHeight;

    // Check if at bottom
    this.isAtBottom = (scrollTop + clientHeight >= scrollHeight - 10);
    this.shouldAutoScroll = this.isAtBottom;

    // Load more messages when scrolled to top
    if (scrollTop === 0 && this.hasMoreMessages && !this.loadingMore) {
      this.loadMoreMessages();
    }

    // Mark as read when scrolled to bottom
    if (this.isAtBottom && this.conversation) {
      this.scheduleMarkAsRead();
    }
  }

  private scrollToBottom(smooth: boolean = false): void {
    if (!this.messagesContainer) return;

    const container = this.messagesContainer.nativeElement;
    const scrollOptions: ScrollToOptions = {
      top: container.scrollHeight,
      behavior: smooth ? 'smooth' : 'auto'
    };

    container.scrollTo(scrollOptions);
  }

  public scrollToMessage(messageId: number): void {
    const messageElement = document.getElementById(`message-${messageId}`);
    if (messageElement) {
      messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      
      // Highlight message briefly
      messageElement.classList.add('highlight');
      setTimeout(() => {
        messageElement.classList.remove('highlight');
      }, 2000);
    }
  }

  // ===============================================
  // READ STATUS
  // ===============================================

  private scheduleMarkAsRead(): void {
    if (this.markAsReadTimeout) {
      clearTimeout(this.markAsReadTimeout);
    }

    this.markAsReadTimeout = setTimeout(() => {
      if (this.conversation && this.isAtBottom) {
        this.messagingService.markMessagesAsRead(this.conversation.id).subscribe();
      }
    }, 2000);
  }

  private clearMarkAsReadTimeout(): void {
    if (this.markAsReadTimeout) {
      clearTimeout(this.markAsReadTimeout);
      this.markAsReadTimeout = null;
    }
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  public formatTime = formatMessageTime;
  public getTypeIcon = getMessageTypeIcon;
  public canModifyMessage = (message: MessageDisplay) => 
    canUserModifyMessage(this.messages.find(m => m.id === message.id)!, this.currentUserId);

  public getTypingText(): string {
    if (this.typingIndicators.length === 0) return '';
    
    if (this.typingIndicators.length === 1) {
      return `${this.typingIndicators[0].userName} tape...`;
    }
    
    if (this.typingIndicators.length === 2) {
      return `${this.typingIndicators[0].userName} et ${this.typingIndicators[1].userName} tapent...`;
    }
    
    return `${this.typingIndicators.length} personnes tapent...`;
  }

  public getMessageGroups(): MessageDisplay[][] {
    const groups: MessageDisplay[][] = [];
    let currentGroup: MessageDisplay[] = [];
    let lastSenderId: number | null = null;
    let lastTime: Date | null = null;

    this.messageDisplays.forEach(message => {
      const messageTime = new Date(message.timestamp);
      const timeDiff = lastTime ? messageTime.getTime() - lastTime.getTime() : 0;
      const shouldGroup = message.sender.id === lastSenderId && timeDiff < 300000; // 5 minutes

      if (shouldGroup && currentGroup.length > 0) {
        currentGroup.push(message);
      } else {
        if (currentGroup.length > 0) {
          groups.push(currentGroup);
        }
        currentGroup = [message];
      }

      lastSenderId = message.sender.id;
      lastTime = messageTime;
    });

    if (currentGroup.length > 0) {
      groups.push(currentGroup);
    }

    return groups;
  }

  public isMessageFromCurrentUser(message: MessageDisplay): boolean {
    return message.sender.id === this.currentUserId;
  }

  public getConversationTitle(): string {
    if (!this.conversation) return '';
    
    if (this.conversation.sujet && this.conversation.sujet.trim()) {
      return this.conversation.sujet;
    }
    
    const otherParticipants = this.conversation.participants.filter(p => p.userId !== this.currentUserId);
    if (otherParticipants.length === 1) {
      return `${otherParticipants[0].prenom} ${otherParticipants[0].nom}`;
    }
    
    return `Conversation avec ${otherParticipants.length} personnes`;
  }

  public getParticipantCount(): number {
    return this.conversation?.participants.length || 0;
  }

  private focusMessageInput(): void {
    setTimeout(() => {
      if (this.messageInput) {
        this.messageInput.nativeElement.focus();
      }
    }, 100);
  }

  private saveDraft(content: string): void {
    if (this.conversation) {
      localStorage.setItem(`draft_${this.conversation.id}`, content);
    }
  }

  private loadDraft(): void {
    if (this.conversation) {
      const draft = localStorage.getItem(`draft_${this.conversation.id}`);
      if (draft) {
        this.messageForm.patchValue({ content: draft });
      }
    }
  }

  private clearDraft(): void {
    if (this.conversation) {
      localStorage.removeItem(`draft_${this.conversation.id}`);
    }
  }

  public trackMessage(index: number, message: MessageDisplay): number {
    return message.id;
  }

  public trackMessageGroup(index: number, group: MessageDisplay[]): string {
    return group.length > 0 ? `${group[0].sender.id}-${group[0].timestamp}` : index.toString();
  }

  public openImageModal(imageUrl: string): void {
    // Open image in a modal or new tab
    window.open(imageUrl, '_blank');
  }

  public getCurrentTimestamp(): number {
    return Date.now();
  }

  public onEnterKeydown(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    if (!keyboardEvent.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}