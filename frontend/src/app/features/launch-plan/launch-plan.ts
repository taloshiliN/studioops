import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import { LaunchPlan, UpsertLaunchPlan } from '../../core/launch-plan.model';
import { LaunchPlanService } from '../../core/launch-plan.service';
import { StudioMember } from '../../core/membership.model';
import { MembershipService } from '../../core/membership.service';
import { Studio } from '../../core/studio.model';
import { StudioService } from '../../core/studio.service';

@Component({
  selector: 'app-launch-plan',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './launch-plan.html',
  styleUrl: './launch-plan.scss'
})
export class LaunchPlanPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly studioService = inject(StudioService);
  private readonly gameService = inject(GameService);
  private readonly membershipService = inject(MembershipService);
  private readonly launchPlanService = inject(LaunchPlanService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly user = this.authService.user;
  readonly studios = signal<Studio[]>([]);
  readonly games = signal<Game[]>([]);
  readonly members = signal<StudioMember[]>([]);
  readonly launchPlan = signal<LaunchPlan | null>(null);
  readonly selectedStudioId = signal<number | null>(null);
  readonly selectedGameId = signal<number | null>(null);
  readonly loadingWorkspace = signal(true);
  readonly loadingPlan = signal(false);
  readonly savingPlan = signal(false);
  readonly errorMessage = signal('');
  readonly actionError = signal('');
  readonly successMessage = signal('');

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

  readonly canManageLaunchPlan = computed(() =>
    ['OWNER', 'PRODUCER'].includes(this.currentMembership()?.role ?? '')
  );

  readonly readiness = computed(() => this.launchPlan()?.readinessPercentage ?? 0);

  readonly missingItems = computed(() => this.launchPlan()?.missingItems ?? []);

  readonly launchPlanForm = new FormGroup({
    itchPageUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(500)]
    }),
    steamPageUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(500)]
    }),
    demoUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(500)]
    }),
    trailerUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(500)]
    }),
    targetDemoDate: new FormControl('', { nonNullable: true }),
    targetNextFestDate: new FormControl('', { nonNullable: true }),
    targetLaunchDate: new FormControl('', { nonNullable: true }),
    contentCreatorOutreachTarget: new FormControl(300, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)]
    }),
    festivalSubmissionTarget: new FormControl(5, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)]
    }),
    notes: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(5000)]
    })
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
            const requestedStudioId = this.numberQueryParam('studioId');
            const initialStudio = studios.find(studio => studio.id === requestedStudioId) ?? studios[0];
            this.loadStudio(initialStudio.id);
          }
        },
        error: error => this.handleError(error, 'Unable to load launch planning.')
      });
  }

  changeStudio(event: Event): void {
    this.loadStudio(Number((event.target as HTMLSelectElement).value));
  }

  changeGame(event: Event): void {
    const gameId = Number((event.target as HTMLSelectElement).value);
    this.selectedGameId.set(gameId);
    this.loadLaunchPlan(gameId);
  }

  saveLaunchPlan(): void {
    const gameId = this.selectedGameId();

    if (!gameId || !this.canManageLaunchPlan() || this.launchPlanForm.invalid || this.savingPlan()) {
      this.launchPlanForm.markAllAsTouched();
      return;
    }

    const value = this.launchPlanForm.getRawValue();
    const request: UpsertLaunchPlan = {
      itchPageUrl: this.optionalText(value.itchPageUrl),
      steamPageUrl: this.optionalText(value.steamPageUrl),
      demoUrl: this.optionalText(value.demoUrl),
      trailerUrl: this.optionalText(value.trailerUrl),
      targetDemoDate: value.targetDemoDate || null,
      targetNextFestDate: value.targetNextFestDate || null,
      targetLaunchDate: value.targetLaunchDate || null,
      contentCreatorOutreachTarget: value.contentCreatorOutreachTarget,
      festivalSubmissionTarget: value.festivalSubmissionTarget,
      notes: this.optionalText(value.notes)
    };

    this.actionError.set('');
    this.successMessage.set('');
    this.savingPlan.set(true);

    this.launchPlanService.update(gameId, request)
      .pipe(finalize(() => this.savingPlan.set(false)))
      .subscribe({
        next: plan => {
          this.launchPlan.set(plan);
          this.patchForm(plan);
          this.successMessage.set('Launch plan saved.');
        },
        error: error => this.handleActionError(error, 'Unable to save the launch plan.')
      });
  }

  openGame(): void {
    const gameId = this.selectedGameId();
    if (gameId) {
      void this.router.navigate(['/games', gameId], { fragment: 'summary' });
    }
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
      return 'Choose a game to manage its launch plan.';
    }

    if (!this.canManageLaunchPlan()) {
      return 'Launch plan changes require the Owner or Producer role.';
    }

    return '';
  }

  private loadStudio(studioId: number): void {
    this.selectedStudioId.set(studioId);
    this.selectedGameId.set(null);
    this.games.set([]);
    this.members.set([]);
    this.launchPlan.set(null);
    this.errorMessage.set('');
    this.actionError.set('');
    this.successMessage.set('');
    this.loadingPlan.set(true);

    forkJoin({
      games: this.gameService.findByStudio(studioId),
      members: this.membershipService.findByStudio(studioId)
    })
      .pipe(finalize(() => this.loadingPlan.set(false)))
      .subscribe({
        next: ({ games, members }) => {
          this.games.set(games);
          this.members.set(members);

          if (games.length > 0) {
            const requestedGameId = this.numberQueryParam('gameId');
            const initialGame = games.find(game => game.id === requestedGameId) ?? games[0];
            this.selectedGameId.set(initialGame.id);
            this.loadLaunchPlan(initialGame.id);
          }
        },
        error: error => this.handleError(error, 'Unable to load games for this studio.')
      });
  }

  private loadLaunchPlan(gameId: number): void {
    this.launchPlan.set(null);
    this.errorMessage.set('');
    this.actionError.set('');
    this.successMessage.set('');
    this.loadingPlan.set(true);

    this.launchPlanService.findByGame(gameId)
      .pipe(finalize(() => this.loadingPlan.set(false)))
      .subscribe({
        next: plan => {
          this.launchPlan.set(plan);
          this.patchForm(plan);
        },
        error: error => this.handleError(error, 'Unable to load the launch plan.')
      });
  }

  private patchForm(plan: LaunchPlan): void {
    this.launchPlanForm.reset({
      itchPageUrl: plan.itchPageUrl ?? '',
      steamPageUrl: plan.steamPageUrl ?? '',
      demoUrl: plan.demoUrl ?? '',
      trailerUrl: plan.trailerUrl ?? '',
      targetDemoDate: plan.targetDemoDate ?? '',
      targetNextFestDate: plan.targetNextFestDate ?? '',
      targetLaunchDate: plan.targetLaunchDate ?? '',
      contentCreatorOutreachTarget: plan.contentCreatorOutreachTarget,
      festivalSubmissionTarget: plan.festivalSubmissionTarget,
      notes: plan.notes ?? ''
    });
  }

  private optionalText(value: string): string | null {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }

  private numberQueryParam(name: string): number | null {
    const value = Number(this.route.snapshot.queryParamMap.get(name));
    return Number.isInteger(value) && value > 0 ? value : null;
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

    this.actionError.set(message);
  }
}
