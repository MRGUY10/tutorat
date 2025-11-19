// ===============================================
// MESSAGE SYSTEM MODELS
// ===============================================

/**
 * Message types enum matching backend
 */
export enum MessageType {
  TEXTE = 'TEXTE',
  FICHIER = 'FICHIER',
  IMAGE = 'IMAGE'
}

/**
 * Chat event types for WebSocket communication
 */
export enum ChatEventType {
  SEND_MESSAGE = 'SEND_MESSAGE',
  RECEIVE_MESSAGE = 'RECEIVE_MESSAGE',
  NEW_MESSAGE = 'NEW_MESSAGE',
  TYPING_START = 'TYPING_START',
  TYPING_STOP = 'TYPING_STOP',
  MARK_READ = 'MARK_READ',
  JOIN_CONVERSATION = 'JOIN_CONVERSATION',
  LEAVE_CONVERSATION = 'LEAVE_CONVERSATION',
  USER_ONLINE = 'USER_ONLINE',
  USER_OFFLINE = 'USER_OFFLINE',
  CONVERSATION_UPDATED = 'CONVERSATION_UPDATED',
  USER_TYPING = 'USER_TYPING',
  USER_STOPPED_TYPING = 'USER_STOPPED_TYPING',
  ERROR = 'ERROR'
}

/**
 * Conversation participant DTO matching backend exactly
 */
export interface ConversationParticipant {
  id: number;
  userId: number;
  conversationId: number;
  nom: string;
  prenom: string;
  email: string;
  roleUtilisateur?: string;
  dateRejoint: string;
  derniereVisite?: string;
  hasLeft: boolean;
  active: boolean;
  displayName: string;
  fullName: string;
  online: boolean;
  typing: boolean;
  etudiant: boolean;
  admin: boolean;
  tuteur: boolean;
}

/**
 * Complete conversation DTO matching backend exactly
 */
export interface Conversation {
  id: number;
  sujet: string;
  dateCreation: string;
  archivee: boolean;
  sessionId?: number;
  
  // Statistics
  totalMessages: number;
  unreadMessages: number;
  lastMessageDate?: string;
  lastMessageContent?: string;
  lastMessageSenderName?: string;
  lastMessagePreview?: string;
  
  // Participants
  participants: ConversationParticipant[];
  participantCount: number;
  
  // Current user context
  currentUserParticipant: boolean;
  currentUserLastRead?: string;
  
  // Conversation type flags
  support: boolean;
  group: boolean;
  sessionConversation: boolean;
}

/**
 * Message response DTO matching backend
 */
export interface MessageResponse {
  id: number;
  conversationId: number;
  expediteurId: number;
  contenu: string;
  dateEnvoi: string;
  lu: boolean;
  typeMessage: MessageType;
  fichierUrl?: string;
  replyToMessageId?: number;
  
  // Sender information
  expediteurNom: string;
  expediteurPrenom: string;
  expediteurEmail: string;
  
  // Reply message information
  replyToContent?: string;
  replyToSenderName?: string;
}

/**
 * Create message DTO for sending messages
 */
export interface CreateMessage {
  conversationId: number;
  contenu: string;
  typeMessage?: MessageType;
  fichierUrl?: string;
  replyToMessageId?: number;
}

/**
 * Create conversation DTO matching backend
 */
export interface CreateConversation {
  sujet: string;
  participantIds: number[];
  sessionId?: number;
  support?: boolean;
}

/**
 * Update conversation DTO
 */
export interface UpdateConversation {
  conversationId: number;
  sujet?: string;
  archivee?: boolean;
}

/**
 * WebSocket chat message format
 */
export interface ChatMessage {
  type: ChatEventType;
  conversationId?: number;
  messageId?: number;
  content?: string;
  userId?: number;
  timestamp?: string;
  data?: any;
}

/**
 * WebSocket chat event
 */
export interface ChatEvent {
  type: ChatEventType;
  conversationId?: number;
  userId?: number;
  message?: MessageResponse;
  conversation?: Conversation;
  timestamp: string;
  data?: any;
  error?: string;
}

/**
 * Typing indicator
 */
export interface TypingIndicator {
  conversationId: number;
  userId: number;
  userName: string;
  isTyping: boolean;
  timestamp: string;
}

/**
 * Online user presence
 */
export interface UserPresence {
  userId: number;
  userName: string;
  isOnline: boolean;
  lastSeen?: string;
}

/**
 * Conversation statistics
 */
export interface ConversationStats {
  totalConversations: number;
  unreadConversations: number;
  totalMessages: number;
  todayMessages: number;
  activeConversations: number;
}

/**
 * Message search result
 */
export interface MessageSearchResult {
  message: MessageResponse;
  conversation: Conversation;
  highlightedContent: string;
}

/**
 * Conversation filters for searching/filtering
 */
export interface ConversationFilters {
  searchQuery?: string;
  includeArchived?: boolean;
  onlyUnread?: boolean;
  sessionId?: number;
  participantId?: number;
  isSupport?: boolean;
  isGroup?: boolean;
  dateFrom?: string;
  dateTo?: string;
}

/**
 * Message pagination
 */
export interface MessagePagination {
  page: number;
  size: number;
  total?: number;
  hasMore?: boolean;
}

/**
 * File attachment info
 */
export interface FileAttachment {
  id?: number;
  name: string;
  type: string;
  size: number;
  url?: string;
  uploadProgress?: number;
  isUploading?: boolean;
  error?: string;
}

/**
 * Conversation card for list display
 */
export interface ConversationCard {
  id: number;
  title: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
  participants: string[];
  participantAvatars: string[];
  isOnline: boolean;
  isGroup: boolean;
  isSupport: boolean;
  isArchived: boolean;
  sessionId?: number;
}

/**
 * Message display format
 */
export interface MessageDisplay {
  id: number;
  content: string;
  sender: {
    id: number;
    name: string;
    avatar?: string;
    isCurrentUser: boolean;
  };
  timestamp: string;
  type: MessageType;
  isRead: boolean;
  isEdited?: boolean;
  replyTo?: {
    id: number;
    content: string;
    senderName: string;
  };
  attachment?: FileAttachment;
  reactions?: MessageReaction[];
}

/**
 * Message reaction (for future enhancement)
 */
export interface MessageReaction {
  emoji: string;
  count: number;
  users: number[];
  hasCurrentUserReacted: boolean;
}

// ===============================================
// UTILITY FUNCTIONS
// ===============================================

/**
 * Convert MessageResponse to MessageDisplay
 */
export function messageToDisplay(message: MessageResponse, currentUserId: number): MessageDisplay {
  return {
    id: message.id,
    content: message.contenu,
    sender: {
      id: message.expediteurId,
      name: `${message.expediteurPrenom} ${message.expediteurNom}`,
      isCurrentUser: message.expediteurId === currentUserId
    },
    timestamp: message.dateEnvoi,
    type: message.typeMessage,
    isRead: message.lu,
    replyTo: message.replyToMessageId ? {
      id: message.replyToMessageId,
      content: message.replyToContent || '',
      senderName: message.replyToSenderName || ''
    } : undefined,
    attachment: message.fichierUrl ? {
      name: extractFilenameFromUrl(message.fichierUrl),
      type: getFileTypeFromUrl(message.fichierUrl),
      size: 0, // Will be populated if needed
      url: message.fichierUrl
    } : undefined
  };
}

/**
 * Convert Conversation to ConversationCard
 */
export function conversationToCard(conversation: Conversation): ConversationCard {
  return {
    id: conversation.id,
    title: conversation.sujet,
    lastMessage: conversation.lastMessageContent || 'Aucun message',
    lastMessageTime: conversation.lastMessageDate || conversation.dateCreation,
    unreadCount: conversation.unreadMessages,
    participants: conversation.participants.map(p => `${p.prenom} ${p.nom}`),
    participantAvatars: [], // Will be populated with actual avatar URLs
    isOnline: conversation.participants.some(p => p.online),
    isGroup: conversation.group,
    isSupport: conversation.support,
    isArchived: conversation.archivee,
    sessionId: conversation.sessionId
  };
}

/**
 * Get message type display text
 */
export function getMessageTypeDisplay(type: MessageType): string {
  switch (type) {
    case MessageType.TEXTE:
      return 'Message texte';
    case MessageType.FICHIER:
      return 'Fichier';
    case MessageType.IMAGE:
      return 'Image';
    default:
      return 'Message';
  }
}

/**
 * Get message type icon
 */
export function getMessageTypeIcon(type: MessageType): string {
  switch (type) {
    case MessageType.TEXTE:
      return 'fas fa-comment';
    case MessageType.FICHIER:
      return 'fas fa-file';
    case MessageType.IMAGE:
      return 'fas fa-image';
    default:
      return 'fas fa-comment';
  }
}

/**
 * Format message timestamp for display
 */
export function formatMessageTime(timestamp: string): string {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // Less than 1 minute
  if (diff < 60000) {
    return 'À l\'instant';
  }
  
  // Less than 1 hour
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000);
    return `Il y a ${minutes} min`;
  }
  
  // Same day
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
  
  // Yesterday
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return `Hier ${date.toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })}`;
  }
  
  // Same year
  if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  // Different year
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

/**
 * Format conversation last message time
 */
export function formatConversationTime(timestamp: string): string {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // Less than 1 minute
  if (diff < 60000) {
    return 'maintenant';
  }
  
  // Less than 1 hour
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000);
    return `${minutes}min`;
  }
  
  // Same day
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
  
  // Yesterday
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return 'hier';
  }
  
  // This week
  const weekAgo = new Date(now);
  weekAgo.setDate(weekAgo.getDate() - 7);
  if (date > weekAgo) {
    return date.toLocaleDateString('fr-FR', { weekday: 'short' });
  }
  
  // Older
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: '2-digit'
  });
}

/**
 * Check if user can edit/delete message
 */
export function canUserModifyMessage(message: MessageResponse, currentUserId: number): boolean {
  // Users can only modify their own messages
  if (message.expediteurId !== currentUserId) {
    return false;
  }
  
  // Can only modify recent messages (within 1 hour)
  const messageTime = new Date(message.dateEnvoi);
  const now = new Date();
  const hourInMs = 60 * 60 * 1000;
  
  return (now.getTime() - messageTime.getTime()) < hourInMs;
}

/**
 * Generate conversation title for group chats
 */
export function generateConversationTitle(participants: ConversationParticipant[], currentUserId: number): string {
  const otherParticipants = participants.filter(p => p.userId !== currentUserId);
  
  if (otherParticipants.length === 0) {
    return 'Conversation vide';
  }
  
  if (otherParticipants.length === 1) {
    return `${otherParticipants[0].prenom} ${otherParticipants[0].nom}`;
  }
  
  if (otherParticipants.length === 2) {
    return `${otherParticipants[0].prenom} ${otherParticipants[0].nom} et ${otherParticipants[1].prenom} ${otherParticipants[1].nom}`;
  }
  
  return `${otherParticipants[0].prenom} ${otherParticipants[0].nom} et ${otherParticipants.length - 1} autres`;
}

/**
 * Get conversation type label
 */
export function getConversationTypeLabel(conversation: Conversation): string {
  if (conversation.support) {
    return 'Support';
  }
  if (conversation.group) {
    return 'Groupe';
  }
  if (conversation.sessionId) {
    return 'Session';
  }
  return 'Privée';
}

/**
 * Get conversation type color class
 */
export function getConversationTypeColor(conversation: Conversation): string {
  if (conversation.support) {
    return 'bg-purple-100 text-purple-800';
  }
  if (conversation.group) {
    return 'bg-blue-100 text-blue-800';
  }
  if (conversation.sessionId) {
    return 'bg-green-100 text-green-800';
  }
  return 'bg-gray-100 text-gray-800';
}

// ===============================================
// UTILITY HELPERS
// ===============================================

function extractFilenameFromUrl(url: string): string {
  return url.split('/').pop() || 'fichier';
}

function getFileTypeFromUrl(url: string): string {
  const extension = url.split('.').pop()?.toLowerCase();
  
  const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'svg', 'webp'];
  const documentTypes = ['pdf', 'doc', 'docx', 'txt', 'rtf'];
  const videoTypes = ['mp4', 'avi', 'mov', 'wmv', 'flv'];
  const audioTypes = ['mp3', 'wav', 'ogg', 'aac'];
  
  if (extension) {
    if (imageTypes.includes(extension)) return 'image';
    if (documentTypes.includes(extension)) return 'document';
    if (videoTypes.includes(extension)) return 'video';
    if (audioTypes.includes(extension)) return 'audio';
  }
  
  return 'file';
}
