import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface AuthUser {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

const TOKEN_KEY = 'studioops.basic-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly userState = signal<AuthUser | null>(null);

  readonly user = this.userState.asReadonly();
  readonly isAuthenticated = computed(() => this.getAuthorization() !== null);

  login(email: string, password: string): Observable<AuthUser> {
    const credentials = btoa(`${email.trim().toLowerCase()}:${password}`);
    const headers = new HttpHeaders({
      Authorization: `Basic ${credentials}`
    });

    return this.http
      .post<AuthUser>('/api/auth/login', null, { headers })
      .pipe(
        tap(user => {
          sessionStorage.setItem(TOKEN_KEY, credentials);
          this.userState.set(user);
        })
      );
  }

  loadCurrentUser(): Observable<AuthUser> {
    return this.http.get<AuthUser>('/api/auth/me').pipe(
      tap(user => this.userState.set(user))
    );
  }

  getAuthorization(): string | null {
    const credentials = sessionStorage.getItem(TOKEN_KEY);
    return credentials ? `Basic ${credentials}` : null;
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    this.userState.set(null);
  }
}
