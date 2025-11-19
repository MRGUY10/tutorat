import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, takeUntil, interval } from 'rxjs';

import { MessagingService } from '../../../services/messaging.service';
import { AuthService } from '../../../services/auth.service';
import {
  Conversation,
  MessageResponse,
  CreateMessage,
  MessageType
} from '../../../core/models/messaging.model';

@Component({
  selector: 'app-messaging-main',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './messaging-main.component.html',
  styles: [`
    .messaging-container {
      height: calc(100vh - 64px);
    }
  `]
})
export class MessagingMainComponent implements OnInit, OnDestroy, AfterViewChecked {
  private destroy$ = new Subject<void>();
  private messagePollingInterval = 3000; // 3 seconds
  
  @ViewChild('messageContainer') private messageContainer?: ElementRef;
  private shouldScrollToBottom = false;
  private isNearBottom = true;
  
  // State
  conversations: Conversation[] = [];
  currentConversation: Conversation | null = null;
  messages: MessageResponse[] = [];
  currentUserId: number | null = null;
  
  // Message input
  newMessageContent: string = '';
  
  // UI State
  isLoading = false;
  error: string | null = null;
  isMobileView = false;
  showConversationList = true;
  
  // Search and filters
  searchQuery = '';
  showArchivedConversations = false;
  
  // Connection status
  isConnected = false;
  
  // Statistics
  totalUnreadCount = 0;
  
  // Online users tracking
  onlineUserIds: Set<number> = new Set();

  constructor(
    private messagingService: MessagingService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {
    this.checkMobileView();
  }

  ngOnInit(): void {
    // Get current user from auth service
    const user = this.authService.getCurrentUser();
    if (user && user.id) {
      this.currentUserId = user.id;
      this.initializeMessaging();
      
      // Check for conversationId query parameter
      this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
        const conversationId = params['conversationId'];
        if (conversationId) {
          // Load and select the conversation
          this.loadAndSelectConversation(Number(conversationId));
        }
      });
    } else {
      this.router.navigate(['/login']);
    }

    // Listen for window resize
    window.addEventListener('resize', () => this.checkMobileView());
  }

  private loadAndSelectConversation(conversationId: number): void {
    // First load conversations if not already loaded
    if (this.conversations.length === 0) {
      this.messagingService.getUserConversations(false)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (conversations) => {
            this.conversations = conversations;
            const conversation = conversations.find(c => c.id === conversationId);
            if (conversation) {
              this.onConversationSelected(conversation);
            }
          },
          error: (error: any) => {
            console.error('Error loading conversations:', error);
          }
        });
    } else {
      // Conversations already loaded, just select
      const conversation = this.conversations.find(c => c.id === conversationId);
      if (conversation) {
        this.onConversationSelected(conversation);
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    window.removeEventListener('resize', () => this.checkMobileView());
  }

  private initializeMessaging(): void {
    // Load conversations
    this.loadConversations();
    
    // Subscribe to real-time updates
    this.subscribeToUpdates();
    
    // Load statistics
    this.loadStatistics();
    
    // Start polling for new messages every 3 seconds
    this.startMessagePolling();
  }

  private loadConversations(): void {
    this.isLoading = true;
    this.messagingService.getUserConversations(this.showArchivedConversations)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (conversations) => {
          this.conversations = conversations;
          this.isLoading = false;
        },
        error: (error: any) => {
          this.error = 'Erreur lors du chargement des conversations';
          console.error('Error loading conversations:', error);
          this.isLoading = false;
        }
      });
  }

  private subscribeToUpdates(): void {
    // Subscribe to conversations updates
    this.messagingService.conversations$
      .pipe(takeUntil(this.destroy$))
      .subscribe(conversations => {
        this.conversations = conversations;
      });

    // Subscribe to messages updates (filtered by current conversation)
    this.messagingService.messages$
      .pipe(takeUntil(this.destroy$))
      .subscribe(messages => {
        console.log('🔔 messages$ subscription triggered with', messages.length, 'total messages');
        
        // Only show messages for the current conversation
        const previousCount = this.messages.length;
        if (this.currentConversation) {
          const filteredMessages = messages.filter(m => m.conversationId === this.currentConversation!.id);
          console.log('📬 Filtered messages for conversation', this.currentConversation.id, ':', filteredMessages.length, 'messages');
          console.log('   Previous count:', previousCount, '→ New count:', filteredMessages.length);
          
          this.messages = filteredMessages;
          
          // Manually trigger change detection to ensure UI updates
          this.cdr.detectChanges();
          console.log('🔄 Change detection triggered');
          
          // Always auto-scroll to show latest messages
          if (this.messages.length > previousCount) {
            console.log('🔽 New messages detected, scrolling to bottom');
            this.shouldScrollToBottom = true;
          }
        } else {
          console.log('⚠️ No current conversation, showing all messages');
          this.messages = messages;
          this.cdr.detectChanges();
        }
      });

    // Subscribe to current conversation
    this.messagingService.currentConversation$
      .pipe(takeUntil(this.destroy$))
      .subscribe(conversation => {
        this.currentConversation = conversation;
        if (conversation && this.isMobileView) {
          this.showConversationList = false;
        }
      });

    // Subscribe to connection status
    this.messagingService.connectionStatus$
      .pipe(takeUntil(this.destroy$))
      .subscribe(status => {
        this.isConnected = status;
      });

    // Subscribe to errors
    this.messagingService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.error = error;
        if (error) {
          setTimeout(() => this.error = null, 5000);
        }
      });

    // Subscribe to unread count
    this.messagingService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => {
        this.totalUnreadCount = count;
      });

    // Subscribe to chat events for real-time updates
    this.messagingService.chatEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe(event => {
        console.log('Chat event received:', event);
        // Handle specific events if needed
      });

    // Subscribe to online users
    this.messagingService.onlineUsers$
      .pipe(takeUntil(this.destroy$))
      .subscribe(users => {
        this.onlineUserIds = new Set(users.map(u => u.userId));
        console.log('👥 Online users updated:', Array.from(this.onlineUserIds));
        this.cdr.detectChanges();
      });
  }

  private loadStatistics(): void {
    this.messagingService.getConversationStats()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (stats: any) => {
          console.log('Conversation statistics:', stats);
        },
        error: (error: any) => {
          console.error('Error loading statistics:', error);
        }
      });
  }

  // Start polling for new messages every 3 seconds
  private startMessagePolling(): void {
    console.log('🔄 Starting message polling (every 3 seconds)');
    
    interval(this.messagePollingInterval)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        // Only poll if we have a current conversation
        if (this.currentConversation) {
          console.log('🔄 Polling for new messages in conversation', this.currentConversation.id);
          
          // Reload messages for current conversation
          this.messagingService.getRecentMessages(this.currentConversation.id, 50)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: () => {
                console.log('✅ Messages refreshed via polling');
                // Trigger change detection
                this.cdr.detectChanges();
              },
              error: (error: any) => {
                console.error('❌ Error polling messages:', error);
              }
            });
        }
      });
  }

  // Conversation selection
  onConversationSelected(conversation: Conversation): void {
    this.messagingService.getConversation(conversation.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.currentConversation = conversation;
          if (this.isMobileView) {
            this.showConversationList = false;
          }
          // Load messages for the conversation
          this.loadConversationMessages(conversation.id);
          // Mark messages as read
          this.messagingService.markMessagesAsRead(conversation.id).subscribe();
          
          // Force scroll to bottom after short delay to ensure messages are loaded
          setTimeout(() => {
            console.log('🔽 Conversation selected, forcing scroll to bottom');
            this.forceScrollToBottom();
          }, 200);
        },
        error: (error: any) => {
          this.error = 'Erreur lors de la sélection de la conversation';
          console.error('Error selecting conversation:', error);
        }
      });
  }

  // Load messages for a conversation
  private loadConversationMessages(conversationId: number): void {
    this.messagingService.getRecentMessages(conversationId, 50)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Messages will be updated via messages$ subscription
          console.log('✅ Messages loaded for conversation', conversationId);
          // Always scroll to bottom when loading a conversation (force scroll)
          setTimeout(() => {
            console.log('🔽 Forcing scroll to bottom for conversation load');
            this.shouldScrollToBottom = true;
          }, 150); // Increased delay to ensure DOM is ready
        },
        error: (error: any) => {
          console.error('❌ Error loading messages:', error);
          this.error = 'Erreur lors du chargement des messages';
        }
      });
  }

  // Message sending
  sendMessage(): void {
    if (!this.currentConversation || !this.newMessageContent.trim()) {
      return;
    }

    const message: CreateMessage = {
      conversationId: this.currentConversation.id,
      contenu: this.newMessageContent.trim(),
      typeMessage: MessageType.TEXTE
    };

    this.messagingService.sendMessage(message).subscribe({
      next: (sentMessage) => {
        console.log('Message sent:', sentMessage);
        this.messages.push(sentMessage);
        this.newMessageContent = '';
        // Always scroll to show sent message
        this.shouldScrollToBottom = true;
      },
      error: (error: any) => {
        this.error = 'Erreur lors de l\'envoi du message';
        console.error('Error sending message:', error);
      }
    });
  }

  // Handle enter key press in message input
  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  // Search
  onSearchChange(query: string): void {
    this.searchQuery = query;
    if (query.trim()) {
      this.messagingService.searchConversations(query).subscribe({
        next: (conversations) => {
          this.conversations = conversations;
        },
        error: (error: any) => {
          console.error('Error searching conversations:', error);
        }
      });
    } else {
      this.loadConversations();
    }
  }

  // Archive toggle
  toggleArchived(): void {
    this.showArchivedConversations = !this.showArchivedConversations;
    this.loadConversations();
  }

  // Archive conversation
  onArchiveConversation(conversationId: number): void {
    this.messagingService.archiveConversation(conversationId, true).subscribe({
      next: () => {
        this.loadConversations();
        if (this.currentConversation?.id === conversationId) {
          this.currentConversation = null;
        }
      },
      error: (error) => {
        this.error = 'Erreur lors de l\'archivage de la conversation';
        console.error('Error archiving conversation:', error);
      }
    });
  }

  // Delete conversation
  onDeleteConversation(conversationId: number): void {
    if (confirm('Êtes-vous sûr de vouloir quitter cette conversation ?')) {
      this.messagingService.leaveConversation(conversationId).subscribe({
        next: () => {
          this.loadConversations();
          if (this.currentConversation?.id === conversationId) {
            this.currentConversation = null;
          }
        },
        error: (error) => {
          this.error = 'Erreur lors de la suppression de la conversation';
          console.error('Error deleting conversation:', error);
        }
      });
    }
  }

  // Back to conversation list (mobile)
  onBackToList(): void {
    this.showConversationList = true;
    this.currentConversation = null;
  }

  // Refresh
  onRefresh(): void {
    this.loadConversations();
  }

  // Mobile view detection
  private checkMobileView(): void {
    this.isMobileView = window.innerWidth < 768;
    if (!this.isMobileView) {
      this.showConversationList = true;
    }
  }

  // Close error
  closeError(): void {
    this.error = null;
  }

  // Check if user is scrolled near bottom of message container
  private isUserNearBottom(): boolean {
    if (!this.messageContainer || !this.messageContainer.nativeElement) {
      return true; // If no container, assume yes (will scroll on first load)
    }
    
    const element = this.messageContainer.nativeElement;
    const threshold = 150; // pixels from bottom
    const position = element.scrollTop + element.clientHeight;
    const height = element.scrollHeight;
    
    const near = (height - position) <= threshold;
    console.debug('isUserNearBottom:', { scrollTop: element.scrollTop, clientHeight: element.clientHeight, scrollHeight: element.scrollHeight, near });
    return near;
  }

  // Track scroll position to know if user is manually scrolling
  onScroll(): void {
    this.isNearBottom = this.isUserNearBottom();
    console.debug('onScroll:', { isNearBottom: this.isNearBottom });
  }

  // Auto-scroll to bottom after view checked
  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      console.log('🔽 ngAfterViewChecked triggering scroll to bottom');
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  // Force scroll to bottom immediately (can be called manually)
  public forceScrollToBottom(): void {
    console.log('🔽 Force scroll to bottom called');
    this.shouldScrollToBottom = true;
    // Also try immediate scroll
    this.scrollToBottom();
  }

  // Check if a user is online
  isUserOnline(userId: number): boolean {
    return this.onlineUserIds.has(userId);
  }

  // Get online status for conversation participants
  getConversationOnlineStatus(conversation: Conversation): string {
    const onlineCount = conversation.participants.filter(p => 
      p.userId !== this.currentUserId && this.isUserOnline(p.userId)
    ).length;
    
    if (onlineCount === 0) return '';
    if (onlineCount === 1) return '🟢 1 en ligne';
    return `🟢 ${onlineCount} en ligne`;
  }

  // Scroll to bottom of message container
  private scrollToBottom(): void {
    try {
      if (this.messageContainer && this.messageContainer.nativeElement) {
        const el = this.messageContainer.nativeElement;
        console.debug('scrollToBottom before:', { scrollTop: el.scrollTop, clientHeight: el.clientHeight, scrollHeight: el.scrollHeight });
        
        // Use multiple methods to ensure scrolling works
        el.scrollTop = el.scrollHeight;
        
        // Also try smooth scrolling as backup
        el.scrollTo({
          top: el.scrollHeight,
          behavior: 'auto' // Use 'auto' for immediate scroll
        });
        
        // Verify scroll actually happened
        setTimeout(() => {
          console.debug('scrollToBottom after:', { scrollTop: el.scrollTop, clientHeight: el.clientHeight, scrollHeight: el.scrollHeight });
          const isAtBottom = (el.scrollHeight - el.scrollTop - el.clientHeight) < 5;
          console.log(isAtBottom ? '✅ Successfully scrolled to bottom' : '❌ Failed to scroll to bottom');
        }, 50);
      } else {
        console.warn('⚠️ Message container not found for scrolling');
      }
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }
}
