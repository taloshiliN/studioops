import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import { Studio } from '../../core/studio.model';
import { StudioService } from '../../core/studio.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly studioService = inject(StudioService);
  private readonly gameService = inject(GameService);
  private readonly router = inject(Router);

  readonly user = this.authService.user;
  readonly studios = signal<Studio[]>([]);
  readonly games = signal<Game[]>([]);
  readonly selectedStudioId = signal<number | null>(null);
  readonly loadingWorkspace = signal(true);
  readonly loadingGames = signal(false);
  readonly errorMessage = signal('');

  readonly selectedStudio = computed(() =>
    this.studios().find(studio => studio.id === this.selectedStudioId()) ?? null
  );

  readonly validationCount = computed(() =>
    this.games().filter(game => game.currentStage === 'VALIDATION').length
  );

  readonly productionCount = computed(() =>
    this.games().filter(game => game.currentStage === 'PRODUCTION').length
  );

  ngOnInit(): void {
    forkJoin({
      user: this.authService.loadCurrentUser(),
      studios: this.studioService.findAll()
    })
      .pipe(finalize(() => this.loadingWorkspace.set(false)))
      .subscribe({
        next: ({ studios }) => {
          this.studios.set(studios);

          if (studios.length > 0) {
            this.loadStudio(studios[0].id);
          }
        },
        error: error => this.handleError(error, 'Unable to load your workspace.')
      });
  }

  changeStudio(event: Event): void {
    const studioId = Number((event.target as HTMLSelectElement).value);
    this.loadStudio(studioId);
  }

  openGame(gameId: number): void {
    void this.router.navigate(['/games', gameId]);
  }

  signOut(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  private loadStudio(studioId: number): void {
    this.selectedStudioId.set(studioId);
    this.games.set([]);
    this.errorMessage.set('');
    this.loadingGames.set(true);

    this.gameService.findByStudio(studioId)
      .pipe(finalize(() => this.loadingGames.set(false)))
      .subscribe({
        next: games => this.games.set(games),
        error: error => this.handleError(error, 'Unable to load games for this studio.')
      });
  }

  private handleError(error: HttpErrorResponse, message: string): void {
    if (error.status === 401) {
      this.signOut();
      return;
    }

    this.errorMessage.set(message);
  }
}
