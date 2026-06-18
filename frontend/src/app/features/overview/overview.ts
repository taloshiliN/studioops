import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import { Studio } from '../../core/studio.model';
import { StudioService } from '../../core/studio.service';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.html',
  styleUrl: './overview.scss'
})
export class OverviewPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly studioService = inject(StudioService);
  private readonly gameService = inject(GameService);
  private readonly router = inject(Router);

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
    this.games().filter(game =>
      ['GAME_JAM', 'PROTOTYPE', 'VALIDATION'].includes(game.currentStage)
    ).length
  );

  readonly productionCount = computed(() =>
    this.games().filter(game =>
      ['PLANNING', 'PRODUCTION', 'PLAYTESTING'].includes(game.currentStage)
    ).length
  );

  ngOnInit(): void {
    this.studioService.findAll()
      .pipe(finalize(() => this.loadingWorkspace.set(false)))
      .subscribe({
        next: studios => {
          this.studios.set(studios);
          if (studios.length > 0) this.loadStudio(studios[0].id);
        },
        error: error => this.handleError(error, 'Unable to load your workspace.')
      });
  }

  changeStudio(event: Event): void {
    this.loadStudio(Number((event.target as HTMLSelectElement).value));
  }

  openGame(gameId: number): void {
    void this.router.navigate(['/games', gameId], { fragment: 'summary' });
  }

  createGame(): void {
    void this.router.navigate(['/games']);
  }

  formatLabel(value: string): string {
    return value.toLowerCase().split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
  }

  displayValue(value: string | null): string {
    return value?.trim() || 'Not set';
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
      this.authService.logout();
      void this.router.navigate(['/login']);
      return;
    }

    this.errorMessage.set(message);
  }
}
