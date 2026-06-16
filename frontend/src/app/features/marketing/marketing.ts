import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import {
  CreateMarketingActivity,
  MarketingActivity,
  MarketingActivityType
} from '../../core/marketing-activity.model';
import { MarketingActivityService } from '../../core/marketing-activity.service';
import { StudioMember } from '../../core/membership.model';
import { MembershipService } from '../../core/membership.service';
import { Studio } from '../../core/studio.model';
import { StudioService } from '../../core/studio.service';

@Component({
  selector: 'app-marketing',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './marketing.html',
  styleUrl: './marketing.scss'
})
export class MarketingPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly studioService = inject(StudioService);
  private readonly gameService = inject(GameService);
  private readonly membershipService = inject(MembershipService);
  private readonly marketingService = inject(MarketingActivityService);
  private readonly router = inject(Router);

  readonly user = this.authService.user;
  readonly studios = signal<Studio[]>([]);
  readonly games = signal<Game[]>([]);
  readonly members = signal<StudioMember[]>([]);
  readonly activities = signal<MarketingActivity[]>([]);
  readonly selectedStudioId = signal<number | null>(null);
  readonly selectedGameId = signal<number | null>(null);
  readonly loadingWorkspace = signal(true);
  readonly loadingActivities = signal(false);
  readonly savingActivity = signal(false);
  readonly completingActivityId = signal<number | null>(null);
  readonly completionFormActivityId = signal<number | null>(null);
  readonly showCreateForm = signal(false);
  readonly errorMessage = signal('');
  readonly actionError = signal('');

  readonly activityTypes: MarketingActivityType[] = [
    'EVENT',
    'SOCIAL_POST',
    'DEVLOG',
    'TRAILER',
    'PRESS_EMAIL',
    'DISCORD_ANNOUNCEMENT',
    'STEAM_FESTIVAL',
    'ITCH_UPDATE',
    'OTHER'
  ];

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

  readonly hasMarketingRole = computed(() =>
    ['OWNER', 'PRODUCER'].includes(this.currentMembership()?.role ?? '')
  );

  readonly canManageMarketing = computed(() =>
    this.hasMarketingRole() && this.selectedGame()?.validationStatus === 'VALIDATED'
  );

  readonly completedCount = computed(() =>
    this.activities().filter(activity => activity.completedAt !== null).length
  );

  readonly overdueCount = computed(() =>
    this.activities().filter(activity => this.isOverdue(activity)).length
  );

  readonly upcomingCount = computed(() =>
    this.activities().filter(activity => !activity.completedAt && !this.isOverdue(activity)).length
  );

  readonly createForm = new FormGroup({
    activityType: new FormControl<MarketingActivityType>('SOCIAL_POST', { nonNullable: true }),
    channel: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(80)]
    }),
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(160)]
    }),
    scheduledFor: new FormControl('', { nonNullable: true })
  });

  readonly completionForm = new FormGroup({
    resultNotes: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(5000)]
    })
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
        error: error => this.handleError(error, 'Unable to load the marketing workspace.')
      });
  }

  changeStudio(event: Event): void {
    this.loadStudio(Number((event.target as HTMLSelectElement).value));
  }

  changeGame(event: Event): void {
    const gameId = Number((event.target as HTMLSelectElement).value);
    this.selectedGameId.set(gameId);
    this.showCreateForm.set(false);
    this.completionFormActivityId.set(null);
    this.loadActivities(gameId);
  }

  toggleCreateForm(): void {
    if (!this.canManageMarketing()) {
      return;
    }

    this.actionError.set('');
    this.showCreateForm.update(show => !show);
  }

  createActivity(): void {
    const gameId = this.selectedGameId();

    if (!gameId || !this.canManageMarketing() || this.createForm.invalid || this.savingActivity()) {
      this.createForm.markAllAsTouched();
      return;
    }

    const value = this.createForm.getRawValue();
    const request: CreateMarketingActivity = {
      activityType: value.activityType,
      channel: value.channel.trim(),
      title: value.title.trim(),
      scheduledFor: value.scheduledFor || null
    };

    this.actionError.set('');
    this.savingActivity.set(true);

    this.marketingService.create(gameId, request)
      .pipe(finalize(() => this.savingActivity.set(false)))
      .subscribe({
        next: activity => {
          this.activities.update(items => [...items, activity]);
          this.games.update(games => games.map(game =>
            game.id === gameId ? { ...game, currentStage: 'MARKETING' } : game
          ));
          this.createForm.reset({
            activityType: 'SOCIAL_POST',
            channel: '',
            title: '',
            scheduledFor: ''
          });
          this.showCreateForm.set(false);
        },
        error: error => this.handleActionError(error, 'Unable to create the marketing activity.')
      });
  }

  startCompletion(activityId: number): void {
    if (!this.canManageMarketing()) {
      return;
    }

    this.actionError.set('');
    this.completionForm.reset({ resultNotes: '' });
    this.completionFormActivityId.set(activityId);
  }

  cancelCompletion(): void {
    this.completionFormActivityId.set(null);
  }

  completeActivity(activityId: number): void {
    if (!this.canManageMarketing() || this.completionForm.invalid || this.completingActivityId()) {
      return;
    }

    const notes = this.completionForm.getRawValue().resultNotes.trim();
    this.actionError.set('');
    this.completingActivityId.set(activityId);

    this.marketingService.complete(activityId, { resultNotes: notes || null })
      .pipe(finalize(() => this.completingActivityId.set(null)))
      .subscribe({
        next: updated => {
          this.activities.update(items => items.map(item => item.id === updated.id ? updated : item));
          this.completionFormActivityId.set(null);
        },
        error: error => this.handleActionError(error, 'Unable to complete the marketing activity.')
      });
  }

  openGame(): void {
    const gameId = this.selectedGameId();
    if (gameId) {
      void this.router.navigate(['/games', gameId], { fragment: 'release' });
    }
  }

  isOverdue(activity: MarketingActivity): boolean {
    return Boolean(
      !activity.completedAt &&
      activity.scheduledFor &&
      new Date(activity.scheduledFor).getTime() < Date.now()
    );
  }

  statusLabel(activity: MarketingActivity): string {
    if (activity.completedAt) {
      return 'Completed';
    }

    return this.isOverdue(activity) ? 'Overdue' : 'Scheduled';
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
      return 'Choose a game to manage its marketing schedule.';
    }

    if (this.selectedGame()?.validationStatus !== 'VALIDATED') {
      return 'This game must be greenlit before marketing activities can be created.';
    }

    if (!this.hasMarketingRole()) {
      return 'Marketing changes require the Owner or Producer role.';
    }

    return '';
  }

  private loadStudio(studioId: number): void {
    this.selectedStudioId.set(studioId);
    this.selectedGameId.set(null);
    this.games.set([]);
    this.activities.set([]);
    this.errorMessage.set('');
    this.loadingActivities.set(true);

    forkJoin({
      games: this.gameService.findByStudio(studioId),
      members: this.membershipService.findByStudio(studioId)
    })
      .pipe(finalize(() => this.loadingActivities.set(false)))
      .subscribe({
        next: ({ games, members }) => {
          this.games.set(games);
          this.members.set(members);

          if (games.length > 0) {
            this.selectedGameId.set(games[0].id);
            this.loadActivities(games[0].id);
          }
        },
        error: error => this.handleError(error, 'Unable to load games for this studio.')
      });
  }

  private loadActivities(gameId: number): void {
    this.activities.set([]);
    this.errorMessage.set('');
    this.loadingActivities.set(true);

    this.marketingService.findByGame(gameId)
      .pipe(finalize(() => this.loadingActivities.set(false)))
      .subscribe({
        next: activities => this.activities.set(activities),
        error: error => this.handleError(error, 'Unable to load marketing activities.')
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
      this.actionError.set('Greenlight the game before creating marketing activities.');
      return;
    }

    this.actionError.set(message);
  }
}
