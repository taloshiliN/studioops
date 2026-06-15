import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreateWorkItem,
  WorkItem,
  WorkItemStatus
} from './work-item.model';

@Injectable({ providedIn: 'root' })
export class WorkItemService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<WorkItem[]> {
    return this.http.get<WorkItem[]>(`/api/games/${gameId}/work-items`);
  }

  create(gameId: number, request: CreateWorkItem): Observable<WorkItem> {
    return this.http.post<WorkItem>(`/api/games/${gameId}/work-items`, request);
  }

  updateStatus(workItemId: number, status: WorkItemStatus): Observable<WorkItem> {
    return this.http.patch<WorkItem>(`/api/work-items/${workItemId}/status`, {
      status
    });
  }

  assign(workItemId: number, assigneeUserId: number): Observable<WorkItem> {
    return this.http.patch<WorkItem>(`/api/work-items/${workItemId}/assignee`, {
      assigneeUserId
    });
  }
}
