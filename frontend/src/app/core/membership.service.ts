import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { StudioMember } from './membership.model';

@Injectable({ providedIn: 'root' })
export class MembershipService {
  private readonly http = inject(HttpClient);

  findByStudio(studioId: number): Observable<StudioMember[]> {
    return this.http.get<StudioMember[]>(`/api/studios/${studioId}/members`);
  }
}
