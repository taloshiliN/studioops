import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CreatePrototype, Prototype } from './prototype.model';

@Injectable({ providedIn: 'root' })
export class PrototypeService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<Prototype[]> {
    return this.http.get<Prototype[]>(`/api/games/${gameId}/prototypes`);
  }

  create(request: CreatePrototype): Observable<Prototype> {
    return this.http.post<Prototype>('/api/prototypes', request);
  }
}
