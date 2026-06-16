import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreateTractionSnapshot,
  TractionSnapshot
} from './traction.model';

@Injectable({ providedIn: 'root' })
export class TractionService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<TractionSnapshot[]> {
    return this.http.get<TractionSnapshot[]>(`/api/games/${gameId}/traction`);
  }

  create(request: CreateTractionSnapshot): Observable<TractionSnapshot> {
    return this.http.post<TractionSnapshot>('/api/traction-snapshots', request);
  }
}
