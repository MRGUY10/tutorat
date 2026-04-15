import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil, debounceTime } from 'rxjs';
import { MessagingService } from '../../../services/messaging.service';
import { CreateMessage, MessageType } from '../../../core/models/messaging.model';

// Temporary interface for file attachments
interface MessageAttachment {
  name: string;
  url: string;
  type: string;
  size: number;
}

@Component({
  selector: 'app-message-composer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './message-composer.component.html',
  styleUrls: ['./message-composer.component.css']
})
export class MessageComposerComponent implements OnInit, OnDestroy {
  @Input() conversationId!: number;
  @Input() replyToMessageId?: string;
  @Input() placeholder: string = 'Tapez votre message...';
  @Input() allowAttachments: boolean = true;
  @Input() allowVoiceMessages: boolean = true;
  @Input() maxFileSize: number = 10 * 1024 * 1024; // 10MB
  @Input() allowedFileTypes: string[] = ['image/*', 'application/pdf', '.doc', '.docx', '.txt'];

  @Output() messageSent = new EventEmitter<any>();
  @Output() typingStart = new EventEmitter<void>();
  @Output() typingStop = new EventEmitter<void>();
  @Output() attachmentSelected = new EventEmitter<File[]>();
  @Output() composerHeightChange = new EventEmitter<number>();

  @ViewChild('textArea') textArea!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  @ViewChild('emojiPicker') emojiPicker!: ElementRef<HTMLDivElement>;

  messageForm: FormGroup;
  
  // State
  loading = false;
  error: string | null = null;
  isTyping = false;
  showEmojiPicker = false;
  showMentions = false;
  showQuickReplies = false;
  isRecordingVoice = false;
  recordingDuration = 0;
  
  // File handling
  selectedFiles: File[] = [];
  uploadingFiles: File[] = [];
  uploadProgress = new Map<string, number>();
  previewUrls = new Map<string, string>();
  
  // Voice recording
  mediaRecorder: MediaRecorder | null = null;
  audioChunks: Blob[] = [];
  recordingTimer: any;
  
  // UI state
  composerHeight = 60;
  isDragOver = false;
  showAdvancedOptions = false;
  
  // Emojis and quick replies
  recentEmojis: string[] = ['😊', '👍', '❤️', '😂', '🔥', '💯'];
  quickReplies: string[] = [
    'Merci !', 'D\'accord', 'Parfait', 'À bientôt', 
    'Je reviens', 'Compris', 'Super !', 'OK'
  ];
  
  // Mentions
  mentionUsers: any[] = [];
  mentionQuery = '';
  activeMentionIndex = -1;
  
  private destroy$ = new Subject<void>();
  private typingTimeout: any;

  constructor(
    private fb: FormBuilder,
    public messagingService: MessagingService
  ) {
    this.messageForm = this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(2000)]],
      type: ['TEXTE']
    });
  }

  ngOnInit() {
    this.setupFormSubscriptions();
    this.setupTypingDetection();
    this.loadRecentEmojis();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.cleanupRecording();
    this.clearTypingTimeout();
    this.revokePreviewUrls();
  }

  private setupFormSubscriptions() {
    // Auto-resize textarea
    this.messageForm.get('content')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.autoResizeTextarea();
        this.updateComposerHeight();
      });
  }

  private setupTypingDetection() {
    this.messageForm.get('content')?.valueChanges
      .pipe(
        debounceTime(300),
        takeUntil(this.destroy$)
      )
      .subscribe(content => {
        if (content && content.trim() && !this.isTyping) {
          this.startTyping();
        } else if ((!content || !content.trim()) && this.isTyping) {
          this.stopTyping();
        }
      });
  }

  private startTyping() {
    this.isTyping = true;
    this.typingStart.emit();
    // TODO: Implement typing indicator when method is available
    // this.messagingService.sendTypingIndicator(this.conversationId, true);
    
    this.clearTypingTimeout();
    this.typingTimeout = setTimeout(() => {
      this.stopTyping();
    }, 3000);
  }

  private stopTyping() {
    if (this.isTyping) {
      this.isTyping = false;
      this.typingStop.emit();
      // TODO: Implement typing indicator when method is available
      // this.messagingService.sendTypingIndicator(this.conversationId, false);
      this.clearTypingTimeout();
    }
  }

  private clearTypingTimeout() {
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
      this.typingTimeout = null;
    }
  }

  // Message sending
  async sendMessage() {
    if (!this.messageForm.valid || this.loading) return;

    const content = this.messageForm.get('content')?.value?.trim();
    if (!content && this.selectedFiles.length === 0) return;

    this.loading = true;
    this.error = null;

    try {
      const message: CreateMessage = {
        conversationId: this.conversationId,
        contenu: content || '',
        typeMessage: this.messageForm.get('type')?.value || MessageType.TEXTE,
        replyToMessageId: this.replyToMessageId ? parseInt(this.replyToMessageId) : undefined
      };

      // Handle file attachments
      if (this.selectedFiles.length > 0) {
        const attachments = await this.uploadFiles();
        if (attachments.length > 0) {
          message.fichierUrl = attachments[0].url;
          message.typeMessage = MessageType.FICHIER;
        }
      }

      await this.messagingService.sendMessage(message);
      
      this.messageSent.emit(message);
      this.resetComposer();
      this.focusTextarea();
      
    } catch (error: any) {
      this.error = error.message || 'Erreur lors de l\'envoi du message';
      console.error('Send message error:', error);
    } finally {
      this.loading = false;
      this.stopTyping();
    }
  }

  sendQuickReply(reply: string) {
    this.messageForm.patchValue({ content: reply });
    this.sendMessage();
  }

  // File handling
  onFileSelected(event: any) {
    const files = Array.from(event.target.files) as File[];
    this.handleFiles(files);
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    
    const files = Array.from(event.dataTransfer?.files || []);
    this.handleFiles(files);
  }

  private handleFiles(files: File[]) {
    const validFiles = files.filter(file => this.validateFile(file));
    
    if (validFiles.length > 0) {
      this.selectedFiles.push(...validFiles);
      this.generatePreviewUrls(validFiles);
      this.attachmentSelected.emit(validFiles);
      this.updateComposerHeight();
    }

    // Reset file input
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  private validateFile(file: File): boolean {
    // Check file size
    if (file.size > this.maxFileSize) {
      this.error = `Le fichier "${file.name}" est trop volumineux. Taille maximale: ${this.formatFileSize(this.maxFileSize)}`;
      return false;
    }

    // Check file type
    const isValidType = this.allowedFileTypes.some(type => {
      if (type.startsWith('.')) {
        return file.name.toLowerCase().endsWith(type.toLowerCase());
      } else if (type.includes('/*')) {
        return file.type.startsWith(type.split('/*')[0]);
      } else {
        return file.type === type;
      }
    });

    if (!isValidType) {
      this.error = `Type de fichier non autorisé: "${file.name}"`;
      return false;
    }

    return true;
  }

  private generatePreviewUrls(files: File[]) {
    files.forEach(file => {
      if (file.type.startsWith('image/')) {
        const url = URL.createObjectURL(file);
        this.previewUrls.set(file.name, url);
      }
    });
  }

  private revokePreviewUrls() {
    this.previewUrls.forEach(url => URL.revokeObjectURL(url));
    this.previewUrls.clear();
  }

  removeFile(file: File) {
    this.selectedFiles = this.selectedFiles.filter(f => f !== file);
    
    const previewUrl = this.previewUrls.get(file.name);
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      this.previewUrls.delete(file.name);
    }
    
    this.updateComposerHeight();
  }

  private async uploadFiles(): Promise<MessageAttachment[]> {
    const attachments: MessageAttachment[] = [];
    this.uploadingFiles = [...this.selectedFiles];

    for (const file of this.selectedFiles) {
      try {
        this.uploadProgress.set(file.name, 0);
        
        // Simulate upload progress for demo
        const progressInterval = setInterval(() => {
          const current = this.uploadProgress.get(file.name) || 0;
          if (current < 90) {
            this.uploadProgress.set(file.name, current + 10);
          }
        }, 100);

        // Upload file (placeholder implementation)
        // TODO: Implement actual file upload when backend endpoint is available
        const attachment: MessageAttachment = {
          name: file.name,
          url: URL.createObjectURL(file), // Temporary URL for demo
          type: file.type,
          size: file.size
        };

        clearInterval(progressInterval);
        this.uploadProgress.set(file.name, 100);
        attachments.push(attachment);

      } catch (error) {
        console.error('File upload error:', error);
        this.error = `Erreur lors de l'upload de "${file.name}"`;
      }
    }

    return attachments;
  }

  // Voice recording
  async startVoiceRecording() {
    if (!this.allowVoiceMessages) return;

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(stream);
      this.audioChunks = [];
      this.recordingDuration = 0;
      
      this.mediaRecorder.ondataavailable = (event) => {
        this.audioChunks.push(event.data);
      };

      this.mediaRecorder.onstop = () => {
        this.handleVoiceRecordingStop();
      };

      this.mediaRecorder.start();
      this.isRecordingVoice = true;
      
      // Start recording timer
      this.recordingTimer = setInterval(() => {
        this.recordingDuration++;
      }, 1000);

    } catch (error) {
      console.error('Voice recording error:', error);
      this.error = 'Impossible d\'accéder au microphone';
    }
  }

  stopVoiceRecording() {
    if (this.mediaRecorder && this.isRecordingVoice) {
      this.mediaRecorder.stop();
      this.cleanupRecording();
    }
  }

  cancelVoiceRecording() {
    this.cleanupRecording();
    this.audioChunks = [];
  }

  private handleVoiceRecordingStop() {
    if (this.audioChunks.length > 0) {
      const audioBlob = new Blob(this.audioChunks, { type: 'audio/wav' });
      const audioFile = new File([audioBlob], `voice-message-${Date.now()}.wav`, { type: 'audio/wav' });
      this.handleFiles([audioFile]);
    }
  }

  private cleanupRecording() {
    this.isRecordingVoice = false;
    this.recordingDuration = 0;
    
    if (this.recordingTimer) {
      clearInterval(this.recordingTimer);
      this.recordingTimer = null;
    }

    if (this.mediaRecorder) {
      this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
      this.mediaRecorder = null;
    }
  }

  // Emoji handling
  toggleEmojiPicker() {
    this.showEmojiPicker = !this.showEmojiPicker;
    if (this.showEmojiPicker) {
      this.showMentions = false;
      this.showQuickReplies = false;
    }
  }

  insertEmoji(emoji: string) {
    const textarea = this.textArea.nativeElement;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const currentValue = this.messageForm.get('content')?.value || '';
    
    const newValue = currentValue.substring(0, start) + emoji + currentValue.substring(end);
    this.messageForm.patchValue({ content: newValue });
    
    // Update recent emojis
    this.updateRecentEmojis(emoji);
    
    // Focus back to textarea and set cursor position
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + emoji.length, start + emoji.length);
    });
  }

  private updateRecentEmojis(emoji: string) {
    this.recentEmojis = [emoji, ...this.recentEmojis.filter(e => e !== emoji)].slice(0, 6);
    localStorage.setItem('recent-emojis', JSON.stringify(this.recentEmojis));
  }

  private loadRecentEmojis() {
    const saved = localStorage.getItem('recent-emojis');
    if (saved) {
      this.recentEmojis = JSON.parse(saved);
    }
  }

  // UI helpers
  autoResizeTextarea() {
    if (this.textArea) {
      const textarea = this.textArea.nativeElement;
      textarea.style.height = 'auto';
      textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    }
  }

  private updateComposerHeight() {
    setTimeout(() => {
      let height = 60; // Base height
      
      if (this.textArea) {
        height += Math.max(0, this.textArea.nativeElement.scrollHeight - 44);
      }
      
      if (this.selectedFiles.length > 0) {
        height += 80; // File preview height
      }
      
      if (this.showEmojiPicker || this.showQuickReplies) {
        height += 200; // Picker height
      }
      
      this.composerHeight = height;
      this.composerHeightChange.emit(height);
    });
  }

  private resetComposer() {
    this.messageForm.reset({ content: '', type: 'TEXTE' });
    this.selectedFiles = [];
    this.uploadingFiles = [];
    this.uploadProgress.clear();
    this.revokePreviewUrls();
    this.showEmojiPicker = false;
    this.showQuickReplies = false;
    this.error = null;
    this.updateComposerHeight();
  }

  private focusTextarea() {
    setTimeout(() => {
      if (this.textArea) {
        this.textArea.nativeElement.focus();
      }
    });
  }

  // Utility methods
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  getFileIcon(file: File): string {
    if (file.type.startsWith('image/')) return 'fas fa-image';
    if (file.type.startsWith('video/')) return 'fas fa-video';
    if (file.type.startsWith('audio/')) return 'fas fa-music';
    if (file.type.includes('pdf')) return 'fas fa-file-pdf';
    if (file.type.includes('word')) return 'fas fa-file-word';
    if (file.type.includes('excel')) return 'fas fa-file-excel';
    return 'fas fa-file';
  }

  // Keyboard shortcuts
  onKeyDown(event: Event) {
    const keyboardEvent = event as KeyboardEvent;
    // Send message with Ctrl+Enter
    if (keyboardEvent.ctrlKey && keyboardEvent.key === 'Enter') {
      event.preventDefault();
      this.sendMessage();
    }
    
    // Escape to close pickers
    if (keyboardEvent.key === 'Escape') {
      this.showEmojiPicker = false;
      this.showMentions = false;
      this.showQuickReplies = false;
    }
  }

  // Click outside to close pickers
  onClickOutside(event: Event) {
    if (!this.emojiPicker?.nativeElement?.contains(event.target as Node)) {
      this.showEmojiPicker = false;
    }
  }

  // TrackBy functions for performance
  trackByFile(index: number, file: File): string {
    return file.name + file.size + file.lastModified;
  }
}
