import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  @Input() user: any = null;
  @Input() title: string = 'Dashboard';
  @Output() toggleSidebar = new EventEmitter<void>();
  
  currentTime = new Date();

  constructor(private router: Router) {
    // Update time every minute
    setInterval(() => {
      this.currentTime = new Date();
    }, 60000);
  }

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  }

  navigateToProfile() {
    this.router.navigate(['/profile']);
  }

  navigateToSettings() {
    this.router.navigate(['/settings']);
  }
}