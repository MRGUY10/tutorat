import { Component, Input } from '@angular/core';

import { NgClass } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
  standalone: true,
  imports: [NgClass]
})
export class SidebarComponent {
  @Input() sidebarCollapsed: boolean = false;
}
