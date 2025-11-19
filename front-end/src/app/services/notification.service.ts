import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Notification {
  id: string | number;
  userId: string;
  title: string;
  message: string;
  type: string;
  entityId: number;
  entityType: string;
  priority: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = environment.BASE_URL + '/api/notifications';

  constructor(private http: HttpClient) {}

  getNotificationsForUser(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}`);
  }

  getUnreadNotificationsForUser(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/unread`);
  }

  countUnreadForUser(userId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/user/${userId}/count`);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllAsReadForUser(userId: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/user/${userId}/read-all`, {});
  }

  cleanupOldNotifications(userId: string, daysOld: number = 30): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/user/${userId}/cleanup?daysOld=${daysOld}`);
  }
}
