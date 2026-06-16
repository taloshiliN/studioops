import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CompleteMarketingActivity,
  CreateMarketingActivity,
  MarketingActivity
} from './marketing-activity.model';

@Injectable({ providedIn: 'root' })
export class MarketingActivityService {
  private readonly http = inject(HttpClient);

  findByGame(gameId: number): Observable<MarketingActivity[]> {
    return this.http.get<MarketingActivity[]>(`/api/games/${gameId}/marketing-activities`);
  }

  create(gameId: number, request: CreateMarketingActivity): Observable<MarketingActivity> {
    return this.http.post<MarketingActivity>(`/api/games/${gameId}/marketing-activities`, request);
  }

  complete(activityId: number, request: CompleteMarketingActivity): Observable<MarketingActivity> {
    return this.http.patch<MarketingActivity>(`/api/marketing-activities/${activityId}/complete`, request);
  }
}
