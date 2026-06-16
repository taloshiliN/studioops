import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreateValidationDecision,
  ValidationDecision
} from './validation-decision.model';

@Injectable({ providedIn: 'root' })
export class ValidationDecisionService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<ValidationDecision[]> {
    return this.http.get<ValidationDecision[]>(
      `/api/games/${gameId}/validation-decisions`
    );
  }

  create(
    gameId: number,
    request: CreateValidationDecision
  ): Observable<ValidationDecision> {
    return this.http.post<ValidationDecision>(
      `/api/games/${gameId}/validation-decisions`,
      request
    );
  }
}
