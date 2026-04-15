import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, BehaviorSubject, Subject, interval, fromEvent } from 'rxjs';
  private readonly apiUrl = environment.BASE_URL + '/api/chat';
  private readonly wsUrl = environment.BASE_URL.replace('http', 'ws') + '/ws/chat';
import {
  Conversation,
  MessageResponse,
  CreateMessage,
  CreateConversation,
  UpdateConversation,
  ConversationFilters,
  MessagePagination,
  ConversationStats,
  MessageSearchResult,
  ChatEvent,
  ChatEventType,
  ChatMessage,
  TypingIndicator,
  UserPresence,
  ConversationCard,
  MessageDisplay,
  conversationToCard,
  messageToDisplay
} from '../models/messaging.model';

@Injectable({
  providedIn: 'root'
})
export class MessagingService {
  private readonly apiUrl = 'http://localhost:8080/api/chat';
  private readonly wsUrl = 'ws://localhost:8080/ws/chat';
  
  // WebSocket connection
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 5000;
  
  // State management
  private conversationsSubject = new BehaviorSubject<Conversation[]>([]);
  private messagesSubject = new BehaviorSubject<MessageResponse[]>([]);
  private currentConversationSubject = new BehaviorSubject<Conversation | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  
  // Real-time events
  private chatEventsSubject = new Subject<ChatEvent>();
  private typingIndicatorsSubject = new BehaviorSubject<TypingIndicator[]>([]);
  private onlineUsersSubject = new BehaviorSubject<UserPresence[]>([]);
  
  // Statistics and metadata
  private conversationStatsSubject = new BehaviorSubject<ConversationStats | null>(null);
  private unreadCountSubject = new BehaviorSubject<number>(0);
  
  // Public observables
  public conversations$ = this.conversationsSubject.asObservable();
  public messages$ = this.messagesSubject.asObservable();
  public currentConversation$ = this.currentConversationSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public error$ = this.errorSubject.asObservable();
  public connectionStatus$ = this.connectionStatusSubject.asObservable();
  public chatEvents$ = this.chatEventsSubject.asObservable();
  public typingIndicators$ = this.typingIndicatorsSubject.asObservable();
  public onlineUsers$ = this.onlineUsersSubject.asObservable();
  public conversationStats$ = this.conversationStatsSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {
    // Initialize WebSocket connection when service is created
    this.initializeWebSocket();
    
    // Set up periodic statistics refresh
    interval(30000).subscribe(() => {
      this.refreshConversationStats();
    });
  }

  // ===============================================
  // WEBSOCKET MANAGEMENT
  // ===============================================

  private initializeWebSocket(): void {
    // Get current user ID from localStorage
    const userId = this.getCurrentUserId();
    if (!userId) {
      console.warn('❌ No user ID available for WebSocket connection. Please login first.');
      this.errorSubject.next('Veuillez vous connecter pour utiliser la messagerie en temps réel.');
      return;
    }

    try {
      const wsFullUrl = `${this.wsUrl}?userId=${userId}`;
      console.log(`🔌 Attempting WebSocket connection to: ${wsFullUrl}`);
      
      this.ws = new WebSocket(wsFullUrl);

      this.ws.onopen = () => {
        console.log('✅ WebSocket connected successfully');
        this.connectionStatusSubject.next(true);
        this.reconnectAttempts = 0;
        this.errorSubject.next(null); // Clear any previous errors
        this.sendHeartbeat();
      };

      this.ws.onmessage = (event) => {
        try {
          const chatEvent: ChatEvent = JSON.parse(event.data);
          this.handleChatEvent(chatEvent);
        } catch (error) {
          console.error('❌ Error parsing WebSocket message:', error);
        }
      };

      this.ws.onclose = (event) => {
        console.log(`🔌 WebSocket disconnected (Code: ${event.code}, Reason: ${event.reason || 'No reason provided'})`);
        this.connectionStatusSubject.next(false);
        this.scheduleReconnect();
      };

      this.ws.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
        console.error(`   URL: ${this.wsUrl}?userId=${userId}`);
        console.error('   Make sure the backend server is running on http://localhost:8080');
        this.connectionStatusSubject.next(false);
      };

    } catch (error) {
      console.error('❌ Failed to initialize WebSocket:', error);
      this.connectionStatusSubject.next(false);
      this.errorSubject.next('Impossible de se connecter au serveur de messagerie.');
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = this.reconnectInterval * this.reconnectAttempts;
      console.log(`🔄 Scheduling reconnect attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${delay/1000}s...`);
      
      setTimeout(() => {
        console.log(`🔄 Reconnect attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
        this.initializeWebSocket();
      }, delay);
    } else {
      console.error('❌ Max reconnect attempts reached. WebSocket connection failed.');
      this.errorSubject.next('Connexion temps réel perdue. Les messages ne seront pas mis à jour automatiquement. Veuillez rafraîchir la page.');
    }
  }

  private sendHeartbeat(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ type: 'HEARTBEAT' }));
      setTimeout(() => this.sendHeartbeat(), 30000);
    }
  }

  private handleChatEvent(event: ChatEvent): void {
    console.log('📨 Received WebSocket event:', event.type, event);
    this.chatEventsSubject.next(event);

    switch (event.type) {
      case ChatEventType.RECEIVE_MESSAGE:
      case ChatEventType.NEW_MESSAGE:
        if (event.message) {
          console.log('💬 Adding new message to state:', event.message);
          this.addMessageToState(event.message);
          this.updateConversationLastMessage(event.message);
        }
        break;

      case ChatEventType.TYPING_START:
      case ChatEventType.USER_TYPING:
        this.updateTypingIndicators(event);
        break;

      case ChatEventType.TYPING_STOP:
      case ChatEventType.USER_STOPPED_TYPING:
        this.updateTypingIndicators(event);
        break;

      case ChatEventType.USER_ONLINE:
      case ChatEventType.USER_OFFLINE:
        this.updateUserPresence(event);
        break;

      case ChatEventType.CONVERSATION_UPDATED:
        if (event.conversation) {
          this.updateConversationInState(event.conversation);
        }
        break;

      case ChatEventType.MARK_READ:
        this.handleMessageReadEvent(event);
        break;

      case ChatEventType.ERROR:
        console.error('❌ WebSocket error event:', event.error);
        this.errorSubject.next(event.error || 'Une erreur est survenue');
        break;

      default:
        console.log('ℹ️ Unhandled event type:', event.type);
    }
  }

  // ===============================================
  // CONVERSATION MANAGEMENT
  // ===============================================

  getUserConversations(includeArchived: boolean = false): Observable<Conversation[]> {
    this.loadingSubject.next(true);
    
    const params = includeArchived ? '?includeArchived=true' : '';
    
    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations${params}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(conversations => {
        this.conversationsSubject.next(conversations);
        this.updateUnreadCount(conversations);
      }),
      catchError(error => this.handleError('Erreur lors du chargement des conversations', error)),
      tap(() => this.loadingSubject.next(false))
    );
  }

  getConversation(conversationId: number): Observable<Conversation> {
    return this.http.get<Conversation>(`${this.apiUrl}/conversations/${conversationId}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(conversation => {
        this.currentConversationSubject.next(conversation);
        this.joinConversation(conversationId);
      }),
      catchError(error => this.handleError('Erreur lors du chargement de la conversation', error))
    );
  }

  createConversation(createDto: CreateConversation): Observable<Conversation> {
    this.loadingSubject.next(true);
    
    return this.http.post<Conversation>(`${this.apiUrl}/conversations`, createDto, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(conversation => {
        // Add to conversations list
        const currentConversations = this.conversationsSubject.value;
        this.conversationsSubject.next([conversation, ...currentConversations]);
        this.currentConversationSubject.next(conversation);
      }),
      catchError(error => this.handleError('Erreur lors de la création de la conversation', error)),
      tap(() => this.loadingSubject.next(false))
    );
  }

  updateConversation(updateDto: UpdateConversation): Observable<Conversation> {
    return this.http.put<Conversation>(`${this.apiUrl}/conversations/${updateDto.conversationId}`, updateDto, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(conversation => {
        this.updateConversationInState(conversation);
        if (this.currentConversationSubject.value?.id === conversation.id) {
          this.currentConversationSubject.next(conversation);
        }
      }),
      catchError(error => this.handleError('Erreur lors de la mise à jour de la conversation', error))
    );
  }

  archiveConversation(conversationId: number, archived: boolean): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/conversations/${conversationId}/archive?archived=${archived}`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        // Update conversation in state
        const conversations = this.conversationsSubject.value;
        const updatedConversations = conversations.map(conv => 
          conv.id === conversationId ? { ...conv, archivee: archived } : conv
        );
        this.conversationsSubject.next(updatedConversations);
      }),
      catchError(error => this.handleError('Erreur lors de l\'archivage de la conversation', error))
    );
  }

  leaveConversation(conversationId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/conversations/${conversationId}/leave`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        // Remove conversation from state
        const conversations = this.conversationsSubject.value;
        this.conversationsSubject.next(conversations.filter(conv => conv.id !== conversationId));
        
        // Clear current conversation if it's the one being left
        if (this.currentConversationSubject.value?.id === conversationId) {
          this.currentConversationSubject.next(null);
        }
      }),
      catchError(error => this.handleError('Erreur lors de la sortie de la conversation', error))
    );
  }

  searchConversations(query: string): Observable<Conversation[]> {
    if (!query.trim()) {
      return this.conversations$;
    }

    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations/search?q=${encodeURIComponent(query)}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors de la recherche de conversations', error))
    );
  }

  getUnreadConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.apiUrl}/conversations/unread`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors du chargement des conversations non lues', error))
    );
  }

  getConversationStats(): Observable<ConversationStats> {
    return this.http.get<ConversationStats>(`${this.apiUrl}/conversations/stats`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(stats => this.conversationStatsSubject.next(stats)),
      catchError(error => this.handleError('Erreur lors du chargement des statistiques', error))
    );
  }

  // ===============================================
  // MESSAGE MANAGEMENT
  // ===============================================

  sendMessage(message: CreateMessage): Observable<MessageResponse> {
    // Send via HTTP (fallback) and WebSocket (real-time)
    this.sendMessageViaWebSocket(message);
    
    return this.http.post<MessageResponse>(`${this.apiUrl}/messages`, message, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(sentMessage => {
        this.addMessageToState(sentMessage);
        this.updateConversationLastMessage(sentMessage);
      }),
      catchError(error => this.handleError('Erreur lors de l\'envoi du message', error))
    );
  }

  getConversationMessages(conversationId: number, pagination: MessagePagination): Observable<MessageResponse[]> {
    const params = `?page=${pagination.page}&size=${pagination.size}`;
    
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/conversations/${conversationId}/messages${params}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(messages => {
        if (pagination.page === 0) {
          // First page - replace messages
          this.messagesSubject.next(messages);
        } else {
          // Additional pages - append to existing messages
          const currentMessages = this.messagesSubject.value;
          this.messagesSubject.next([...messages, ...currentMessages]);
        }
      }),
      catchError(error => this.handleError('Erreur lors du chargement des messages', error))
    );
  }

  getRecentMessages(conversationId: number, limit: number = 20): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/conversations/${conversationId}/messages/recent?limit=${limit}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(messages => {
        console.log(`📥 Loaded ${messages.length} messages for conversation ${conversationId}`);
        // Instead of replacing all messages, merge them with existing ones
        const currentMessages = this.messagesSubject.value;
        // Remove old messages from this conversation
        const otherMessages = currentMessages.filter(m => m.conversationId !== conversationId);
        // Add new messages from this conversation
        const allMessages = [...otherMessages, ...messages].sort((a, b) => 
          new Date(a.dateEnvoi).getTime() - new Date(b.dateEnvoi).getTime()
        );
        this.messagesSubject.next(allMessages);
      }),
      catchError(error => this.handleError('Erreur lors du chargement des messages récents', error))
    );
  }

  markMessagesAsRead(conversationId: number): Observable<void> {
    // Send via WebSocket for real-time updates
    this.sendWebSocketMessage({
      type: ChatEventType.MARK_READ,
      conversationId: conversationId,
      timestamp: new Date().toISOString()
    });

    return this.http.put<void>(`${this.apiUrl}/conversations/${conversationId}/messages/read`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        // Update unread count in conversation
        const conversations = this.conversationsSubject.value;
        const updatedConversations = conversations.map(conv => 
          conv.id === conversationId ? { ...conv, unreadMessages: 0 } : conv
        );
        this.conversationsSubject.next(updatedConversations);
        this.updateUnreadCount(updatedConversations);
      }),
      catchError(error => this.handleError('Erreur lors du marquage des messages comme lus', error))
    );
  }

  getUnreadMessageCount(conversationId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/conversations/${conversationId}/messages/unread/count`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors du comptage des messages non lus', error))
    );
  }

  searchMessages(query: string, limit: number = 50): Observable<MessageSearchResult[]> {
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/messages/search?q=${encodeURIComponent(query)}&limit=${limit}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(messages => messages.map(message => ({
        message,
        conversation: this.findConversationForMessage(message),
        highlightedContent: this.highlightSearchTerm(message.contenu, query)
      } as MessageSearchResult))),
      catchError(error => this.handleError('Erreur lors de la recherche de messages', error))
    );
  }

  deleteMessage(messageId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/messages/${messageId}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        // Remove message from state
        const messages = this.messagesSubject.value;
        this.messagesSubject.next(messages.filter(msg => msg.id !== messageId));
      }),
      catchError(error => this.handleError('Erreur lors de la suppression du message', error))
    );
  }

  // ===============================================
  // REAL-TIME FEATURES
  // ===============================================

  joinConversation(conversationId: number): void {
    this.sendWebSocketMessage({
      type: ChatEventType.JOIN_CONVERSATION,
      conversationId: conversationId,
      timestamp: new Date().toISOString()
    });
  }

  leaveConversationRealtime(conversationId: number): void {
    this.sendWebSocketMessage({
      type: ChatEventType.LEAVE_CONVERSATION,
      conversationId: conversationId,
      timestamp: new Date().toISOString()
    });
  }

  startTyping(conversationId: number): void {
    this.sendWebSocketMessage({
      type: ChatEventType.TYPING_START,
      conversationId: conversationId,
      timestamp: new Date().toISOString()
    });
  }

  stopTyping(conversationId: number): void {
    this.sendWebSocketMessage({
      type: ChatEventType.TYPING_STOP,
      conversationId: conversationId,
      timestamp: new Date().toISOString()
    });
  }

  private sendMessageViaWebSocket(message: CreateMessage): void {
    this.sendWebSocketMessage({
      type: ChatEventType.SEND_MESSAGE,
      conversationId: message.conversationId,
      content: message.contenu,
      timestamp: new Date().toISOString(),
      data: message
    });
  }

  private sendWebSocketMessage(message: ChatMessage): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  // ===============================================
  // PRESENCE AND STATUS
  // ===============================================

  getOnlineUsers(): Observable<UserPresence[]> {
    return this.http.get<UserPresence[]>(`${this.apiUrl}/presence/online`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(users => this.onlineUsersSubject.next(users)),
      catchError(error => this.handleError('Erreur lors du chargement des utilisateurs en ligne', error))
    );
  }

  isUserOnline(userId: number): Observable<boolean> {
    return this.http.get<{userId: number, online: boolean}>(`${this.apiUrl}/presence/online/${userId}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      map(response => response.online),
      catchError(error => this.handleError('Erreur lors de la vérification du statut utilisateur', error))
    );
  }

  // ===============================================
  // UTILITY METHODS
  // ===============================================

  getConversationCards(): Observable<ConversationCard[]> {
    return this.conversations$.pipe(
      map(conversations => conversations.map(conv => conversationToCard(conv)))
    );
  }

  getMessageDisplays(currentUserId: number): Observable<MessageDisplay[]> {
    return this.messages$.pipe(
      map(messages => messages.map(msg => messageToDisplay(msg, currentUserId)))
    );
  }

  filterConversations(filters: ConversationFilters): Observable<Conversation[]> {
    return this.conversations$.pipe(
      map(conversations => {
        let filtered = [...conversations];

        if (filters.searchQuery) {
          const query = filters.searchQuery.toLowerCase();
          filtered = filtered.filter(conv => 
            conv.sujet.toLowerCase().includes(query) ||
            conv.lastMessageContent?.toLowerCase().includes(query) ||
            conv.participants.some(p => 
              `${p.prenom} ${p.nom}`.toLowerCase().includes(query)
            )
          );
        }

        if (!filters.includeArchived) {
          filtered = filtered.filter(conv => !conv.archivee);
        }

        if (filters.onlyUnread) {
          filtered = filtered.filter(conv => conv.unreadMessages > 0);
        }

        if (filters.sessionId) {
          filtered = filtered.filter(conv => conv.sessionId === filters.sessionId);
        }

        if (filters.participantId) {
          filtered = filtered.filter(conv => 
            conv.participants.some(p => p.userId === filters.participantId)
          );
        }

        if (filters.isSupport !== undefined) {
          filtered = filtered.filter(conv => conv.support === filters.isSupport);
        }

        if (filters.isGroup !== undefined) {
          filtered = filtered.filter(conv => conv.group === filters.isGroup);
        }

        if (filters.dateFrom) {
          filtered = filtered.filter(conv => 
            new Date(conv.dateCreation) >= new Date(filters.dateFrom!)
          );
        }

        if (filters.dateTo) {
          filtered = filtered.filter(conv => 
            new Date(conv.dateCreation) <= new Date(filters.dateTo!)
          );
        }

        return filtered;
      })
    );
  }

  refreshConversationStats(): void {
    this.getConversationStats().subscribe();
  }

  // ===============================================
  // PRIVATE HELPER METHODS
  // ===============================================

  private addMessageToState(message: MessageResponse): void {
    const currentMessages = this.messagesSubject.value;
    
    // Check if message already exists (avoid duplicates)
    if (!currentMessages.some(msg => msg.id === message.id)) {
      // Insert message in chronological order
      const newMessages = [...currentMessages, message].sort((a, b) => 
        new Date(a.dateEnvoi).getTime() - new Date(b.dateEnvoi).getTime()
      );
      console.log('💾 Adding message to state:', {
        messageId: message.id,
        conversationId: message.conversationId,
        content: message.contenu,
        totalMessages: newMessages.length,
        messagesInConversation: newMessages.filter(m => m.conversationId === message.conversationId).length
      });
      this.messagesSubject.next(newMessages);
      console.log('✅ messagesSubject.next() called with', newMessages.length, 'messages');
    } else {
      console.log('⚠️ Message already exists in state, skipping:', message.id);
    }
  }

  private updateConversationLastMessage(message: MessageResponse): void {
    const conversations = this.conversationsSubject.value;
    const updatedConversations = conversations.map(conv => {
      if (conv.id === message.conversationId) {
        return {
          ...conv,
          lastMessageContent: message.contenu,
          lastMessageDate: message.dateEnvoi,
          lastMessageSenderName: `${message.expediteurPrenom} ${message.expediteurNom}`,
          totalMessages: conv.totalMessages + 1,
          unreadMessages: message.expediteurId !== this.getCurrentUserId() ? conv.unreadMessages + 1 : conv.unreadMessages
        };
      }
      return conv;
    });
    
    this.conversationsSubject.next(updatedConversations);
    this.updateUnreadCount(updatedConversations);
  }

  private updateConversationInState(conversation: Conversation): void {
    const conversations = this.conversationsSubject.value;
    const updatedConversations = conversations.map(conv => 
      conv.id === conversation.id ? conversation : conv
    );
    this.conversationsSubject.next(updatedConversations);
  }

  private updateTypingIndicators(event: ChatEvent): void {
    const currentIndicators = this.typingIndicatorsSubject.value;
    const isTyping = event.type === ChatEventType.TYPING_START;
    
    if (isTyping) {
      // Add or update typing indicator
      const indicator: TypingIndicator = {
        conversationId: event.conversationId!,
        userId: event.userId!,
        userName: 'Utilisateur', // Will need to be populated with actual name
        isTyping: true,
        timestamp: event.timestamp
      };
      
      const filtered = currentIndicators.filter(ind => 
        !(ind.conversationId === indicator.conversationId && ind.userId === indicator.userId)
      );
      
      this.typingIndicatorsSubject.next([...filtered, indicator]);
    } else {
      // Remove typing indicator
      const filtered = currentIndicators.filter(ind => 
        !(ind.conversationId === event.conversationId && ind.userId === event.userId)
      );
      
      this.typingIndicatorsSubject.next(filtered);
    }
  }

  private updateUserPresence(event: ChatEvent): void {
    const currentUsers = this.onlineUsersSubject.value;
    const isOnline = event.type === ChatEventType.USER_ONLINE;
    
    if (isOnline) {
      // Add or update user presence
      const userPresence: UserPresence = {
        userId: event.userId!,
        userName: 'Utilisateur', // Will need to be populated
        isOnline: true
      };
      
      const filtered = currentUsers.filter(user => user.userId !== event.userId);
      this.onlineUsersSubject.next([...filtered, userPresence]);
    } else {
      // Update user as offline
      const updated = currentUsers.map(user => 
        user.userId === event.userId ? { ...user, isOnline: false, lastSeen: event.timestamp } : user
      );
      
      this.onlineUsersSubject.next(updated);
    }
  }

  private handleMessageReadEvent(event: ChatEvent): void {
    if (event.conversationId) {
      const conversations = this.conversationsSubject.value;
      const updatedConversations = conversations.map(conv => 
        conv.id === event.conversationId ? { ...conv, unreadMessages: 0 } : conv
      );
      this.conversationsSubject.next(updatedConversations);
      this.updateUnreadCount(updatedConversations);
    }
  }

  private updateUnreadCount(conversations: Conversation[]): void {
    const totalUnread = conversations.reduce((sum, conv) => sum + conv.unreadMessages, 0);
    this.unreadCountSubject.next(totalUnread);
  }

  private findConversationForMessage(message: MessageResponse): Conversation | null {
    return this.conversationsSubject.value.find(conv => conv.id === message.conversationId) || null;
  }

  private highlightSearchTerm(content: string, searchTerm: string): string {
    if (!searchTerm.trim()) return content;
    
    const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    return content.replace(regex, '<mark>$1</mark>');
  }

  private getCurrentUserId(): number | null {
    // Get user ID from localStorage (stored by AuthService)
    const userId = localStorage.getItem('user_id');
    return userId ? parseInt(userId, 10) : null;
  }

  private getAuthHeaders(): HttpHeaders {
    const userId = this.getCurrentUserId();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Id': userId?.toString() || ''
    });
  }

  private handleError(message: string, error: any): Observable<never> {
    console.error(message, error);
    this.errorSubject.next(message);
    throw error;
  }

  // ===============================================
  // CLEANUP
  // ===============================================

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.connectionStatusSubject.next(false);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
