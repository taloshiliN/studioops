import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { GameDashboard } from './game-dashboard.model';
import { CreateGame, Game } from './game.model';

@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly http = inject(HttpClient);

  create(request: CreateGame): Observable<Game> {
    return this.http.post<Game>('/api/games', request);
  }

  findByStudio(studioId: number): Observable<Game[]> {
    return this.http.get<Game[]>(`/api/studios/${studioId}/games`);
  }

  findById(gameId: number): Observable<Game> {
    return this.http.get<Game>(`/api/games/${gameId}`);
  }

  getDashboard(gameId: number): Observable<GameDashboard> {
    return this.http.get<GameDashboard>(`/api/games/${gameId}/dashboard`);
  }
}
