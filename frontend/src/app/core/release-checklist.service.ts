import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreateReleaseChecklistItem,
  ReleaseChecklistItem,
  ReleaseReadiness,
  UpdateReleaseChecklistCompletion
} from './release-checklist.model';

@Injectable({ providedIn: 'root' })
export class ReleaseChecklistService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<ReleaseChecklistItem[]> {
    return this.http.get<ReleaseChecklistItem[]>(`/api/games/${gameId}/release-checklist`);
  }

  getReadiness(gameId: number): Observable<ReleaseReadiness> {
    return this.http.get<ReleaseReadiness>(`/api/games/${gameId}/release-readiness`);
  }

  create(gameId: number, request: CreateReleaseChecklistItem): Observable<ReleaseChecklistItem> {
    return this.http.post<ReleaseChecklistItem>(`/api/games/${gameId}/release-checklist`, request);
  }

  updateCompletion(
    itemId: number,
    request: UpdateReleaseChecklistCompletion
  ): Observable<ReleaseChecklistItem> {
    return this.http.patch<ReleaseChecklistItem>(
      `/api/release-checklist/${itemId}/completion`,
      request
    );
  }
}
