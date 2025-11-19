import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { MessagingService } from '../../../services/messaging.service';
import { Subject, takeUntil } from 'rxjs';

export interface NavigationItem {
  icon: string;
  label: string;
  route: string;
  active?: boolean;
  children?: NavigationItem[];
  badge?: {
    count: number;
    color: 'red' | 'blue' | 'green' | 'yellow';
  };
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit, OnChanges, OnDestroy {
  @Input() user: any = null;
  @Input() isOpen: boolean = true;
  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  navigationItems: NavigationItem[] = [];
  unreadMessagesCount = 0;
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private messagingService: MessagingService
  ) {}

  ngOnInit() {
    this.setupNavigation();
    this.setupMessagingSubscriptions();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user'] && changes['user'].currentValue) {
      console.log('User changed in sidebar:', changes['user'].currentValue); // Debug log
      this.setupNavigation();
      this.setupMessagingSubscriptions();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupMessagingSubscriptions() {
    if (this.user) {
      // Subscribe to unread messages count
      this.messagingService.conversations$
        .pipe(takeUntil(this.destroy$))
        .subscribe(conversations => {
          this.unreadMessagesCount = conversations.reduce((total, conv) => total + conv.unreadMessages, 0);
          this.updateMessagesNavigationBadge();
        });
    }
  }

  private updateMessagesNavigationBadge() {
    const messagesItem = this.navigationItems.find(item => item.route === '/messages');
    if (messagesItem) {
      if (this.unreadMessagesCount > 0) {
        messagesItem.badge = {
          count: this.unreadMessagesCount,
          color: 'red'
        };
      } else {
        delete messagesItem.badge;
      }
    }
  }

  setupNavigation() {
    console.log('Setting up navigation for user:', this.user); // Debug log
    console.log('User role:', this.user?.role); // Debug log
    
    // Student navigation
    const studentItems: NavigationItem[] = [
      { icon: 'home', label: 'Dashboard', route: '/dashboard', active: true },
      { icon: 'search', label: 'Find Tutors', route: '/tutors' },
      { icon: 'calendar', label: 'My Sessions', route: '/sessions' },
      { icon: 'clock', label: 'Session Requests', route: '/session-requests' },
      { icon: 'message-circle', label: 'Messages', route: '/messages' }
    ];

    // Tutor navigation
    const tutorItems: NavigationItem[] = [
      { icon: 'home', label: 'Dashboard', route: '/dashboard', active: true },
      { icon: 'calendar', label: 'My Sessions', route: '/sessions' },
      { icon: 'inbox', label: 'Session Requests', route: '/session-requests' },
      { icon: 'message-circle', label: 'Messages', route: '/messages' },
    ];

    // Admin navigation with simplified User Management
    const adminItems: NavigationItem[] = [
      { icon: 'home', label: 'Dashboard', route: '/dashboard', active: true },
      { icon: 'users', label: 'User Management', route: '/users' },
      { icon: 'user-check', label: 'Tutor Verification', route: '/admin/tutor-verification' },
      { icon: 'book', label: 'Subject Management', route: '/subjects' },

    ];

    switch (this.user?.role) {
      case 'STUDENT':
        this.navigationItems = studentItems;
        break;
      case 'TUTOR':
        this.navigationItems = tutorItems;
        break;
      case 'ADMIN':
        this.navigationItems = adminItems;
        break;
      default:
        this.navigationItems = [{ icon: 'home', label: 'Dashboard', route: '/dashboard', active: true }];
    }
  }

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }

  onLogout() {
    this.logout.emit();
  }

  getIconClass(icon: string): string {
    const iconMap: { [key: string]: string } = {
      'home': 'fas fa-home',
      'users': 'fas fa-users',
      'search': 'fas fa-search',
      'calendar': 'fas fa-calendar',
      'plus-circle': 'fas fa-plus-circle',
      'clock': 'fas fa-clock',
      'inbox': 'fas fa-inbox',
      'message-circle': 'fas fa-comments',
      'star': 'fas fa-star',
      'credit-card': 'fas fa-credit-card',
      'book-open': 'fas fa-book-open',
      'brain': 'fas fa-brain',
      'clipboard-check': 'fas fa-clipboard-check',
      'dollar-sign': 'fas fa-dollar-sign',
      'book': 'fas fa-book',
      'bar-chart': 'fas fa-chart-bar',
      'settings': 'fas fa-cog',
      'shield': 'fas fa-shield-alt',
      'list': 'fas fa-list',
      'user-plus': 'fas fa-user-plus',
      'user-check': 'fas fa-user-check',
      'graduation-cap': 'fas fa-graduation-cap'
    };
    return iconMap[icon] || 'fas fa-circle';
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }
}