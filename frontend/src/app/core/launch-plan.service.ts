import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { LaunchPlan, UpsertLaunchPlan } from './launch-plan.model';

@Injectable({ providedIn: 'root' })
export class LaunchPlanService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<LaunchPlan> {
    return this.http.get<LaunchPlan>(`/api/games/${gameId}/launch-plan`);
  }

  update(gameId: number, request: UpsertLaunchPlan): Observable<LaunchPlan> {
    return this.http.put<LaunchPlan>(`/api/games/${gameId}/launch-plan`, request);
  }
}
