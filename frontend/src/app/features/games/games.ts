import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { CreateGame, Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import { Studio } from '../../core/studio.model';
import { StudioService } from '../../core/studio.service';

@Component({
  selector: 'app-games',
  imports: [ReactiveFormsModule],
  templateUrl: './games.html',
  styleUrls: ['../../layout/workspace-page.scss', './games.scss']
})
export class GamesPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly studioService = inject(StudioService);
  private readonly gameService = inject(GameService);
  private readonly router = inject(Router);

  readonly fontOptions = [
    'Inter',
    'Montserrat',
    'Bebas Neue',
    'Merriweather',
    'Orbitron',
    'Cinzel',
    'Press Start 2P',
    'Nunito'
  ];

  readonly studios = signal<Studio[]>([]);
  readonly games = signal<Game[]>([]);
  readonly selectedStudioId = signal<number | null>(null);
  readonly loadingWorkspace = signal(true);
  readonly loadingGames = signal(false);
  readonly savingGame = signal(false);
  readonly showCreateForm = signal(false);
  readonly deletingGameIds = signal<Set<number>>(new Set());
  readonly errorMessage = signal('');
  readonly actionError = signal('');

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

  readonly createForm = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(160)] }),
    shortPitch: new FormControl('', { nonNullable: true }),
    genre: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(80)] }),
    targetPlatforms: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(240)] }),
    fontFamily: new FormControl('Inter', { nonNullable: true, validators: [Validators.required, Validators.maxLength(80)] })
  });

  ngOnInit(): void {
    this.studioService.findAll()
      .pipe(finalize(() => this.loadingWorkspace.set(false)))
      .subscribe({
        next: studios => {
          this.studios.set(studios);

          if (studios.length > 0) {
            this.loadStudio(studios[0].id);
          }
        },
        error: error => this.handleError(error, 'Unable to load your games workspace.')
      });
  }

  changeStudio(event: Event): void {
    this.showCreateForm.set(false);
    this.actionError.set('');
    this.loadStudio(Number((event.target as HTMLSelectElement).value));
  }

  toggleCreateForm(): void {
    this.actionError.set('');
    this.showCreateForm.update(value => !value);
  }

  createGame(): void {
    const studioId = this.selectedStudioId();

    if (studioId === null || this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const value = this.createForm.getRawValue();
    const request: CreateGame = {
      studioId,
      title: value.title.trim(),
      shortPitch: this.optionalText(value.shortPitch),
      genre: this.optionalText(value.genre),
      targetPlatforms: this.optionalText(value.targetPlatforms),
      fontFamily: value.fontFamily
    };

    this.savingGame.set(true);
    this.actionError.set('');

    this.gameService.create(request)
      .pipe(finalize(() => this.savingGame.set(false)))
      .subscribe({
        next: game => {
          this.games.update(games => [game, ...games]);
          this.createForm.reset({
            title: '',
            shortPitch: '',
            genre: '',
            targetPlatforms: '',
            fontFamily: 'Inter'
          });
          this.showCreateForm.set(false);
        },
        error: error => this.handleCreateError(error)
      });
  }

  openGame(gameId: number): void {
    void this.router.navigate(['/games', gameId], { fragment: 'summary' });
  }

  deleteGame(game: Game): void {
    const confirmed = window.confirm(
      `Delete "${game.title}" and all of its production records? This cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    this.setDeleting(game.id, true);
    this.actionError.set('');

    this.gameService.delete(game.id)
      .pipe(finalize(() => this.setDeleting(game.id, false)))
      .subscribe({
        next: () => this.games.update(games => games.filter(existingGame => existingGame.id !== game.id)),
        error: error => this.handleDeleteError(error)
      });
  }

  isDeleting(gameId: number): boolean {
    return this.deletingGameIds().has(gameId);
  }

  formatLabel(value: string): string {
    return value.toLowerCase().split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
  }

  displayValue(value: string | null): string {
    return value?.trim() || 'Not set';
  }

  displayFont(fontFamily: string | null): string {
    return fontFamily?.trim() || 'Inter';
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

  private optionalText(value: string): string | null {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }

  private setDeleting(gameId: number, deleting: boolean): void {
    this.deletingGameIds.update(ids => {
      const nextIds = new Set(ids);

      if (deleting) {
        nextIds.add(gameId);
      } else {
        nextIds.delete(gameId);
      }

      return nextIds;
    });
  }

  private handleCreateError(error: HttpErrorResponse): void {
    if (error.status === 401) {
      this.authService.logout();
      void this.router.navigate(['/login']);
      return;
    }

    if (error.status === 403) {
      this.actionError.set('Only studio Owners or Producers can create games.');
      return;
    }

    this.actionError.set('Unable to create this game right now.');
  }

  private handleDeleteError(error: HttpErrorResponse): void {
    if (error.status === 401) {
      this.authService.logout();
      void this.router.navigate(['/login']);
      return;
    }

    if (error.status === 403) {
      this.actionError.set('Only studio Owners or Producers can delete games.');
      return;
    }

    if (error.status === 404) {
      this.actionError.set('That game was already deleted or no longer exists.');
      return;
    }

    this.actionError.set('Unable to delete this game right now.');
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
