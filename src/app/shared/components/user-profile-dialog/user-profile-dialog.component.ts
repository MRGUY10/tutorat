import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
// Removed unused AgentService after project cleanup

@Component({
  selector: 'app-user-profile-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-profile-dialog.component.html',
  styleUrls: ['./user-profile-dialog.component.scss']
})
export class UserProfileDialogComponent implements OnInit {
  @Input() isOpen = false;
  @Output() closeModal = new EventEmitter<void>();
  userProfile: any = null;
  agentId: string | null = null;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Get agentId from decoded token
    const tokenData = this.authService.getDecodedToken();
    console.log('Decoded token:', tokenData);
    this.agentId = tokenData && (tokenData.agentId || tokenData.agent_id || tokenData.sub || tokenData.id) ? (tokenData.agentId || tokenData.agent_id || tokenData.sub || tokenData.id) : null;
    console.log('Extracted agentId for profile modal:', this.agentId);
    // Removed agent profile fetch logic for deleted AgentService
  }

  close() {
    this.closeModal.emit();
  }
}
