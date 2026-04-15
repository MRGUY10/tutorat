import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges, SimpleChanges, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { AuthService } from "../../../services/auth.service";
import { NotificationService, Notification } from "../../../services/notification.service";
import { Subscription } from 'rxjs';
import { ToastNotificationComponent } from '../toast-notification/toast-notification.component';

// Notification interface now matches backend

@Component({
  selector: "app-notifications-modal",
  standalone: true,
  imports: [CommonModule, ToastNotificationComponent],
  templateUrl: "./notifications-modal.component.html",
  styleUrls: ["./notifications-modal.component.scss"],
})
export class NotificationsModalComponent implements OnInit, OnDestroy, OnChanges {
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && changes['isOpen'].currentValue === true) {
      this.fetchNotifications();
    }
  }
  @Input() isOpen = false;
  @Output() closeModal = new EventEmitter<void>();
  @ViewChild(ToastNotificationComponent, { static: true }) toast!: ToastNotificationComponent;


  notifications: Notification[] = [];
  loading = true;
  private notificationSub?: Subscription;
  private userId: string;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.userId = this.authService.getDecodedToken()?.sub || '';
  }

  ngOnInit(): void {
    if (!this.userId) {
      console.error('User ID not found, cannot fetch notifications.');
      this.loading = false;
      return;
    }
    this.fetchNotifications();
  }


  private fetchNotifications(): void {
    this.loading = true;
    this.notificationSub = this.notificationService.getNotificationsForUser(this.userId).subscribe({
      next: (notifications) => {
        console.log('Fetched notifications:', notifications);
        // Sort by createdAt descending
        this.notifications = notifications.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to fetch notifications:', err);
        this.notifications = [];
        this.loading = false;
      }
    });
  }

  // private connectToWebSocket(): void {
  //   this.websocketService.connect(environment.websocketUrl, this.userId).subscribe({
  //     next: (connected) => {
  //       if (connected) {
  //         this.loading = false;
  //         this.notificationSub = this.websocketService.notifications$.subscribe({
  //           next: (notification) => this.handleNewNotification(notification),
  //           error: (err) => console.error('Notification stream error:', err)
  //         });
  //       }
  //     },
  //     error: (err) => {
  //       console.error('WebSocket connection error:', err);
  //       this.loading = false;
  //     }
  //   });
  // }

  private handleNewNotification(notification: any): void {
    const newNotification: Notification = {
      id: notification.id || Date.now().toString(),
      userId: notification.userId || this.userId,
      title: notification.title || 'New Notification',
      message: notification.message || '',
      type: notification.type || '',
      entityId: notification.entityId || 0,
      entityType: notification.entityType || '',
      priority: notification.priority || 'medium',
      read: false,
      createdAt: notification.createdAt || new Date().toISOString(),
    };

    if (!this.notifications.some(n => n.id === newNotification.id)) {
      this.notifications.unshift(newNotification);
      this.playNotificationSound();

      // Show toast notification
      this.showToast(newNotification);
    }
  }

  private showToast(notification: Notification): void {
    if (this.toast) {
      this.toast.show({
        title: notification.title,
  message: notification.message,
        type: 'info', // or 'success', 'warning', 'error' based on priority
        duration: 5000 // 5 seconds
      });
    }
  }
  private playNotificationSound(): void {
    const audio = new Audio('assets/sounds/notification.wav');
    audio.play().catch(e => console.warn('Sound playback failed:', e));
  }


  markAsRead(notification: Notification): void {
    if (notification.read) return;
  this.notificationService.markAsRead(Number(notification.id)).subscribe({
      next: () => {
        notification.read = true;
      },
      error: (err) => {
        console.error('Failed to mark notification as read:', err);
      }
    });
  }


  markAllAsRead(): void {
    this.notificationService.markAllAsReadForUser(this.userId).subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
      },
      error: (err) => {
        console.error('Failed to mark all notifications as read:', err);
      }
    });
  }


  ngOnDestroy(): void {
    this.notificationSub?.unsubscribe();
  }

  onClose(): void {
    this.markAllAsRead();
    this.closeModal.emit();
  }
}
