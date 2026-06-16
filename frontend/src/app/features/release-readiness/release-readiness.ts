import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import { StudioMember } from '../../core/membership.model';
import { MembershipService } from '../../core/membership.service';
import {
  CreateReleaseChecklistItem,
  ReleaseChecklistItem,
  ReleaseReadiness
} from '../../core/release-checklist.model';
import { ReleaseChecklistService } from '../../core/release-checklist.service';
import { Studio } from '../../core/studio.model';
import { StudioService } from '../../core/studio.service';

@Component({
  selector: 'app-release-readiness',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './release-readiness.html',
  styleUrl: './release-readiness.scss'
})
export class ReleaseReadinessPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly studioService = inject(StudioService);
  private readonly gameService = inject(GameService);
  private readonly membershipService = inject(MembershipService);
  private readonly releaseChecklistService = inject(ReleaseChecklistService);
  private readonly router = inject(Router);

  readonly user = this.authService.user;
  readonly studios = signal<Studio[]>([]);
  readonly games = signal<Game[]>([]);
  readonly members = signal<StudioMember[]>([]);
  readonly checklistItems = signal<ReleaseChecklistItem[]>([]);
  readonly readiness = signal<ReleaseReadiness | null>(null);
  readonly selectedStudioId = signal<number | null>(null);
  readonly selectedGameId = signal<number | null>(null);
  readonly loadingWorkspace = signal(true);
  readonly loadingRelease = signal(false);
  readonly showCreateForm = signal(false);
  readonly savingItem = signal(false);
  readonly updatingItemIds = signal<Set<number>>(new Set());
  readonly errorMessage = signal('');
  readonly actionError = signal('');

  readonly selectedStudio = computed(() =>
    this.studios().find(studio => studio.id === this.selectedStudioId()) ?? null
  );

  readonly selectedGame = computed(() =>
    this.games().find(game => game.id === this.selectedGameId()) ?? null
  );

  readonly currentMembership = computed(() => {
    const userId = this.user()?.id;
    return this.members().find(member => member.userId === userId) ?? null;
  });

  readonly canManageRelease = computed(() =>
    ['OWNER', 'PRODUCER', 'DEVELOPER'].includes(this.currentMembership()?.role ?? '') &&
    this.selectedGame()?.validationStatus === 'VALIDATED'
  );

  readonly completedItems = computed(() =>
    this.checklistItems().filter(item => item.completed).length
  );

  readonly blockingOpenItems = computed(() =>
    this.checklistItems().filter(item => item.blocksRelease && !item.completed)
  );

  readonly createForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(160)]
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(2000)]
    }),
    blocksRelease: new FormControl(true, { nonNullable: true })
  });

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
        error: error => this.handleError(error, 'Unable to load release readiness.')
      });
  }

  changeStudio(event: Event): void {
    this.loadStudio(Number((event.target as HTMLSelectElement).value));
  }

  changeGame(event: Event): void {
    const gameId = Number((event.target as HTMLSelectElement).value);
    this.selectedGameId.set(gameId);
    this.showCreateForm.set(false);
    this.loadReleaseData(gameId);
  }

  toggleCreateForm(): void {
    if (!this.canManageRelease()) {
      return;
    }

    this.actionError.set('');
    this.showCreateForm.update(show => !show);
  }

  createChecklistItem(): void {
    const gameId = this.selectedGameId();

    if (!gameId || !this.canManageRelease() || this.createForm.invalid || this.savingItem()) {
      this.createForm.markAllAsTouched();
      return;
    }

    const value = this.createForm.getRawValue();
    const request: CreateReleaseChecklistItem = {
      title: value.title.trim(),
      description: value.description.trim() || null,
      blocksRelease: value.blocksRelease
    };

    this.actionError.set('');
    this.savingItem.set(true);

    this.releaseChecklistService.create(gameId, request)
      .pipe(finalize(() => this.savingItem.set(false)))
      .subscribe({
        next: item => {
          this.checklistItems.update(items => [...items, item]);
          this.createForm.reset({
            title: '',
            description: '',
            blocksRelease: true
          });
          this.showCreateForm.set(false);
          this.refreshReadiness(gameId);
        },
        error: error => this.handleActionError(error, 'Unable to create the release checklist item.')
      });
  }

  changeCompletion(item: ReleaseChecklistItem, event: Event): void {
    if (!this.canManageRelease() || this.isUpdating(item.id)) {
      (event.target as HTMLInputElement).checked = item.completed;
      return;
    }

    const completed = (event.target as HTMLInputElement).checked;

    if (completed === item.completed) {
      return;
    }

    this.setUpdating(item.id, true);
    this.actionError.set('');

    this.releaseChecklistService.updateCompletion(item.id, { completed })
      .pipe(finalize(() => this.setUpdating(item.id, false)))
      .subscribe({
        next: updated => {
          this.checklistItems.update(items =>
            items.map(current => current.id === updated.id ? updated : current)
          );

          const gameId = this.selectedGameId();
          if (gameId) {
            this.refreshReadiness(gameId);
          }
        },
        error: error => {
          (event.target as HTMLInputElement).checked = item.completed;
          this.handleActionError(error, 'Unable to update the release checklist item.');
        }
      });
  }

  openGame(): void {
    const gameId = this.selectedGameId();
    if (gameId) {
      void this.router.navigate(['/games', gameId], { fragment: 'release' });
    }
  }

  isUpdating(itemId: number): boolean {
    return this.updatingItemIds().has(itemId);
  }

  formatLabel(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  managementMessage(): string {
    if (!this.selectedGame()) {
      return 'Choose a game to manage its release checklist.';
    }

    if (this.selectedGame()?.validationStatus !== 'VALIDATED') {
      return 'This game must be greenlit before release checklist items can be created or completed.';
    }

    if (!this.canManageRelease()) {
      return 'Release checklist changes require the Owner, Producer, or Developer role.';
    }

    return '';
  }

  private loadStudio(studioId: number): void {
    this.selectedStudioId.set(studioId);
    this.selectedGameId.set(null);
    this.games.set([]);
    this.members.set([]);
    this.checklistItems.set([]);
    this.readiness.set(null);
    this.errorMessage.set('');
    this.loadingRelease.set(true);

    forkJoin({
      games: this.gameService.findByStudio(studioId),
      members: this.membershipService.findByStudio(studioId)
    })
      .pipe(finalize(() => this.loadingRelease.set(false)))
      .subscribe({
        next: ({ games, members }) => {
          this.games.set(games);
          this.members.set(members);

          if (games.length > 0) {
            this.selectedGameId.set(games[0].id);
            this.loadReleaseData(games[0].id);
          }
        },
        error: error => this.handleError(error, 'Unable to load games for this studio.')
      });
  }

  private loadReleaseData(gameId: number): void {
    this.checklistItems.set([]);
    this.readiness.set(null);
    this.errorMessage.set('');
    this.actionError.set('');
    this.loadingRelease.set(true);

    forkJoin({
      items: this.releaseChecklistService.findByGame(gameId),
      readiness: this.releaseChecklistService.getReadiness(gameId)
    })
      .pipe(finalize(() => this.loadingRelease.set(false)))
      .subscribe({
        next: ({ items, readiness }) => {
          this.checklistItems.set(items);
          this.readiness.set(readiness);
        },
        error: error => this.handleError(error, 'Unable to load release readiness for this game.')
      });
  }

  private refreshReadiness(gameId: number): void {
    this.releaseChecklistService.getReadiness(gameId).subscribe({
      next: readiness => this.readiness.set(readiness),
      error: error => this.handleActionError(
        error,
        'The checklist changed, but release readiness could not refresh.'
      )
    });
  }

  private setUpdating(itemId: number, updating: boolean): void {
    const ids = new Set(this.updatingItemIds());
    updating ? ids.add(itemId) : ids.delete(itemId);
    this.updatingItemIds.set(ids);
  }

  private handleError(error: HttpErrorResponse, message: string): void {
    if (error.status === 401) {
      this.authService.logout();
      void this.router.navigate(['/login']);
      return;
    }

    this.errorMessage.set(message);
  }

  private handleActionError(error: HttpErrorResponse, message: string): void {
    if (error.status === 401) {
      this.handleError(error, message);
      return;
    }

    if (error.status === 403) {
      this.actionError.set('Your studio role does not allow this change.');
      return;
    }

    if (error.status === 409) {
      this.actionError.set('Greenlight the game before changing release checklist items.');
      return;
    }

    this.actionError.set(message);
  }
}
