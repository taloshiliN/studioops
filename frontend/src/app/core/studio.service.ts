import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Studio } from './studio.model';

@Injectable({ providedIn: 'root' })
export class StudioService {
  private readonly http = inject(HttpClient);

  findAll(): Observable<Studio[]> {
    return this.http.get<Studio[]>('/api/studios');
  }
}
