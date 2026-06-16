import { DOCUMENT, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, map, switchMap } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { GameDashboard } from '../../core/game-dashboard.model';
import { Game } from '../../core/game.model';
import { GameService } from '../../core/game.service';
import { StudioMember } from '../../core/membership.model';
import { MembershipService } from '../../core/membership.service';
import { CreatePrototype, Prototype } from '../../core/prototype.model';
import { PrototypeService } from '../../core/prototype.service';
import {
  CreateTractionSnapshot,
  TractionSnapshot,
  TractionSource
} from '../../core/traction.model';
import { TractionService } from '../../core/traction.service';
import {
  CreateValidationDecision,
  ValidationDecision,
  ValidationDecisionType
} from '../../core/validation-decision.model';
import { ValidationDecisionService } from '../../core/validation-decision.service';
import {
  CreateWorkItem,
  WorkItem,
  WorkItemPriority,
  WorkItemStatus
} from '../../core/work-item.model';
import { WorkItemService } from '../../core/work-item.service';

@Component({
  selector: 'app-game-dashboard',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './game-dashboard.html',
  styleUrl: './game-dashboard.scss'
})
export class GameDashboardPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly document = inject(DOCUMENT);
  private readonly router = inject(Router);
  private readonly gameService = inject(GameService);
  private readonly workItemService = inject(WorkItemService);
  private readonly membershipService = inject(MembershipService);
  private readonly prototypeService = inject(PrototypeService);
  private readonly tractionService = inject(TractionService);
  private readonly validationDecisionService = inject(ValidationDecisionService);
  private readonly authService = inject(AuthService);

  readonly dashboard = signal<GameDashboard | null>(null);
  readonly game = signal<Game | null>(null);
  readonly workItems = signal<WorkItem[]>([]);
  readonly members = signal<StudioMember[]>([]);
  readonly prototypes = signal<Prototype[]>([]);
  readonly tractionSnapshots = signal<TractionSnapshot[]>([]);
  readonly validationDecisions = signal<ValidationDecision[]>([]);
  readonly currentMembership = computed(() => {
    const userId = this.authService.user()?.id;
    return this.members().find(member => member.userId === userId) ?? null;
  });
  readonly canManageEvidence = computed(() =>
    ['OWNER', 'PRODUCER', 'DEVELOPER'].includes(this.currentMembership()?.role ?? '')
  );
  readonly canRecordDecision = computed(() =>
    ['OWNER', 'PRODUCER'].includes(this.currentMembership()?.role ?? '')
  );
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly taskError = signal('');
  readonly showCreateForm = signal(false);
  readonly savingTask = signal(false);
  readonly openEvidenceForm = signal<'prototype' | 'traction' | 'decision' | null>(null);
  readonly savingPrototype = signal(false);
  readonly savingTraction = signal(false);
  readonly savingDecision = signal(false);
  readonly evidenceError = signal('');
  readonly updatingItemIds = signal<Set<number>>(new Set());

  readonly statuses: WorkItemStatus[] = [
    'TODO',
    'IN_PROGRESS',
    'BLOCKED',
    'DONE',
    'CANCELLED'
  ];

  readonly priorities: WorkItemPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly tractionSources: TractionSource[] = [
    'ITCH_IO',
    'STEAM',
    'X_TWITTER',
    'YOUTUBE',
    'TIKTOK',
    'EVENT',
    'DISCORD',
    'NEWSLETTER',
    'OTHER'
  ];
  readonly decisionTypes: ValidationDecisionType[] = [
    'GREENLIGHT',
    'PIVOT',
    'NEEDS_MORE_TESTING',
    'SHELVE'
  ];

  readonly createForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)]
    }),
    description: new FormControl('', { nonNullable: true }),
    priority: new FormControl<WorkItemPriority>('MEDIUM', { nonNullable: true }),
    dueDate: new FormControl('', { nonNullable: true }),
    assigneeUserId: new FormControl('', { nonNullable: true })
  });

  readonly prototypeForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(160)]
    }),
    buildVersion: new FormControl('', { nonNullable: true }),
    itchUrl: new FormControl('', { nonNullable: true }),
    playableUrl: new FormControl('', { nonNullable: true }),
    repositoryUrl: new FormControl('', { nonNullable: true })
  });

  readonly tractionForm = new FormGroup({
    prototypeId: new FormControl('', { nonNullable: true }),
    source: new FormControl<TractionSource>('ITCH_IO', { nonNullable: true }),
    views: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    downloads: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    plays: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    ratingsCount: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    averageRating: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.min(0), Validators.max(5)]
    }),
    commentsCount: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    followersGained: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    wishlists: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    revenueCents: new FormControl(0, { nonNullable: true, validators: [Validators.min(0)] }),
    capturedAt: new FormControl('', { nonNullable: true })
  });

  readonly decisionForm = new FormGroup({
    decision: new FormControl<ValidationDecisionType>('NEEDS_MORE_TESTING', {
      nonNullable: true
    }),
    reason: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(2000)]
    })
  });

  private gameId = 0;

  ngOnInit(): void {
    this.gameId = Number(this.route.snapshot.paramMap.get('gameId'));

    if (!Number.isInteger(this.gameId) || this.gameId <= 0) {
      void this.router.navigate(['/dashboard']);
      return;
    }

    this.loadPage();
  }

  goBack(): void {
    void this.router.navigate(['/dashboard']);
  }

  openLaunchPlan(): void {
    const studioId = this.game()?.studioId;

    void this.router.navigate(['/launch-plan'], {
      queryParams: {
        gameId: this.gameId,
        ...(studioId ? { studioId } : {})
      }
    });
  }

  signOut(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }

  toggleCreateForm(): void {
    this.taskError.set('');
    this.showCreateForm.update(value => !value);
  }

  toggleEvidenceForm(form: 'prototype' | 'traction' | 'decision'): void {
    if (!this.canManageEvidence() || (form === 'decision' && !this.canRecordDecision())) {
      return;
    }

    this.evidenceError.set('');
    this.openEvidenceForm.update(current => current === form ? null : form);
  }

  createPrototype(): void {
    if (!this.canManageEvidence() || this.prototypeForm.invalid || this.savingPrototype()) {
      this.prototypeForm.markAllAsTouched();
      return;
    }

    const value = this.prototypeForm.getRawValue();
    const request: CreatePrototype = {
      gameId: this.gameId,
      gameJamId: null,
      name: value.name.trim(),
      buildVersion: value.buildVersion.trim() || null,
      itchUrl: value.itchUrl.trim() || null,
      repositoryUrl: value.repositoryUrl.trim() || null,
      playableUrl: value.playableUrl.trim() || null
    };

    this.evidenceError.set('');
    this.savingPrototype.set(true);

    this.prototypeService.create(request)
      .pipe(finalize(() => this.savingPrototype.set(false)))
      .subscribe({
        next: prototype => {
          this.prototypes.update(items => [prototype, ...items]);
          this.prototypeForm.reset({
            name: '',
            buildVersion: '',
            itchUrl: '',
            playableUrl: '',
            repositoryUrl: ''
          });
          this.openEvidenceForm.set(null);
        },
        error: error => this.handleEvidenceError(error, 'Unable to create the prototype.')
      });
  }

  createTractionSnapshot(): void {
    if (!this.canManageEvidence() || this.tractionForm.invalid || this.savingTraction()) {
      this.tractionForm.markAllAsTouched();
      return;
    }

    const value = this.tractionForm.getRawValue();
    const request: CreateTractionSnapshot = {
      gameId: this.gameId,
      prototypeId: value.prototypeId ? Number(value.prototypeId) : null,
      source: value.source,
      views: value.views,
      downloads: value.downloads,
      plays: value.plays,
      ratingsCount: value.ratingsCount,
      averageRating: value.averageRating,
      commentsCount: value.commentsCount,
      followersGained: value.followersGained,
      wishlists: value.wishlists,
      revenueCents: value.revenueCents,
      capturedAt: value.capturedAt || null
    };

    this.evidenceError.set('');
    this.savingTraction.set(true);

    this.tractionService.create(request)
      .pipe(finalize(() => this.savingTraction.set(false)))
      .subscribe({
        next: snapshot => {
          this.tractionSnapshots.update(items => [...items, snapshot]);
          this.tractionForm.reset({
            prototypeId: '',
            source: 'ITCH_IO',
            views: 0,
            downloads: 0,
            plays: 0,
            ratingsCount: 0,
            averageRating: 0,
            commentsCount: 0,
            followersGained: 0,
            wishlists: 0,
            revenueCents: 0,
            capturedAt: ''
          });
          this.openEvidenceForm.set(null);
          this.refreshDashboard();
        },
        error: error => this.handleEvidenceError(error, 'Unable to record the traction snapshot.')
      });
  }

  createValidationDecision(): void {
    if (!this.canRecordDecision() || this.decisionForm.invalid || this.savingDecision()) {
      this.decisionForm.markAllAsTouched();
      return;
    }

    const value = this.decisionForm.getRawValue();
    const request: CreateValidationDecision = {
      decision: value.decision,
      reason: value.reason.trim()
    };

    this.evidenceError.set('');
    this.savingDecision.set(true);

    this.validationDecisionService.create(this.gameId, request)
      .pipe(finalize(() => this.savingDecision.set(false)))
      .subscribe({
        next: decision => {
          this.validationDecisions.update(items => [decision, ...items]);
          this.decisionForm.reset({
            decision: 'NEEDS_MORE_TESTING',
            reason: ''
          });
          this.openEvidenceForm.set(null);
          this.refreshGameState();
        },
        error: error => this.handleEvidenceError(error, 'Unable to record the validation decision.')
      });
  }

  createWorkItem(): void {
    if (this.createForm.invalid || this.savingTask()) {
      this.createForm.markAllAsTouched();
      return;
    }

    const formValue = this.createForm.getRawValue();
    const assigneeUserId = formValue.assigneeUserId
      ? Number(formValue.assigneeUserId)
      : null;

    const request: CreateWorkItem = {
      milestoneId: null,
      assigneeUserId,
      title: formValue.title.trim(),
      description: formValue.description.trim() || null,
      priority: formValue.priority,
      dueDate: formValue.dueDate || null
    };

    this.taskError.set('');
    this.savingTask.set(true);

    this.workItemService.create(this.gameId, request)
      .pipe(finalize(() => this.savingTask.set(false)))
      .subscribe({
        next: workItem => {
          this.workItems.update(items => [workItem, ...items]);
          this.createForm.reset({
            title: '',
            description: '',
            priority: 'MEDIUM',
            dueDate: '',
            assigneeUserId: ''
          });
          this.showCreateForm.set(false);
          this.refreshDashboard();
        },
        error: error => this.handleTaskError(error, 'Unable to create the work item.')
      });
  }

  changeStatus(workItem: WorkItem, event: Event): void {
    const status = (event.target as HTMLSelectElement).value as WorkItemStatus;

    if (status === workItem.status || this.isUpdating(workItem.id)) {
      return;
    }

    this.setUpdating(workItem.id, true);
    this.taskError.set('');

    this.workItemService.updateStatus(workItem.id, status)
      .pipe(finalize(() => this.setUpdating(workItem.id, false)))
      .subscribe({
        next: updated => {
          this.replaceWorkItem(updated);
          this.refreshDashboard();
        },
        error: error => this.handleTaskError(error, 'Unable to update the work-item status.')
      });
  }

  changeAssignee(workItem: WorkItem, event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    if (!value || this.isUpdating(workItem.id)) {
      return;
    }

    const assigneeUserId = Number(value);
    if (assigneeUserId === workItem.assigneeUserId) {
      return;
    }

    this.setUpdating(workItem.id, true);
    this.taskError.set('');

    this.workItemService.assign(workItem.id, assigneeUserId)
      .pipe(finalize(() => this.setUpdating(workItem.id, false)))
      .subscribe({
        next: updated => this.replaceWorkItem(updated),
        error: error => this.handleTaskError(error, 'Unable to assign the work item.')
      });
  }

  isUpdating(workItemId: number): boolean {
    return this.updatingItemIds().has(workItemId);
  }

  isOverdue(workItem: WorkItem): boolean {
    if (!workItem.dueDate || workItem.status === 'DONE' || workItem.status === 'CANCELLED') {
      return false;
    }

    return workItem.dueDate < new Date().toISOString().slice(0, 10);
  }

  formatLabel(value: string | null): string {
    if (!value) {
      return 'Not recorded';
    }

    return value
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'No date';
    }

    return new Intl.DateTimeFormat('en', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    }).format(new Date(`${value}T00:00:00`));
  }

  priorityClass(priority: WorkItemPriority): string {
    return priority.toLowerCase();
  }

  private loadPage(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    forkJoin({
      user: this.authService.loadCurrentUser(),
      dashboard: this.gameService.getDashboard(this.gameId),
      game: this.gameService.findById(this.gameId),
      workItems: this.workItemService.findByGame(this.gameId),
      prototypes: this.prototypeService.findByGame(this.gameId),
      tractionSnapshots: this.tractionService.findByGame(this.gameId),
      validationDecisions: this.validationDecisionService.findByGame(this.gameId)
    })
      .pipe(
        switchMap(result =>
          this.membershipService.findByStudio(result.game.studioId).pipe(
            map(members => ({ ...result, members }))
          )
        ),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: ({
          dashboard,
          game,
          workItems,
          prototypes,
          tractionSnapshots,
          validationDecisions,
          members
        }) => {
          this.dashboard.set(dashboard);
          this.game.set(game);
          this.workItems.set(workItems);
          this.prototypes.set(prototypes);
          this.tractionSnapshots.set(tractionSnapshots);
          this.validationDecisions.set(validationDecisions);
          this.members.set(members);
          this.scrollToRequestedSection();
        },
        error: error => this.handlePageError(error)
      });
  }

  private refreshDashboard(): void {
    this.gameService.getDashboard(this.gameId).subscribe({
      next: dashboard => this.dashboard.set(dashboard),
      error: error => this.handleTaskError(error, 'The task changed, but the summary could not refresh.')
    });
  }

  private scrollToRequestedSection(): void {
    const fragment = this.route.snapshot.fragment
      ?? this.document.defaultView?.location.hash.replace(/^#/, '');

    if (!fragment) {
      return;
    }

    setTimeout(() => {
      this.document.getElementById(fragment)?.scrollIntoView({ block: 'start' });
    }, 150);
  }

  private refreshGameState(): void {
    forkJoin({
      dashboard: this.gameService.getDashboard(this.gameId),
      game: this.gameService.findById(this.gameId)
    }).subscribe({
      next: ({ dashboard, game }) => {
        this.dashboard.set(dashboard);
        this.game.set(game);
      },
      error: error => this.handleEvidenceError(
        error,
        'The decision was saved, but the game summary could not refresh.'
      )
    });
  }

  private replaceWorkItem(updated: WorkItem): void {
    this.workItems.update(items =>
      items.map(item => item.id === updated.id ? updated : item)
    );
  }

  private setUpdating(workItemId: number, updating: boolean): void {
    const ids = new Set(this.updatingItemIds());
    updating ? ids.add(workItemId) : ids.delete(workItemId);
    this.updatingItemIds.set(ids);
  }

  private handlePageError(error: HttpErrorResponse): void {
    if (error.status === 401) {
      this.signOut();
      return;
    }

    this.errorMessage.set(
      error.status === 403
        ? 'You do not have permission to view this game.'
        : 'Unable to load the game dashboard.'
    );
  }

  private handleTaskError(error: HttpErrorResponse, message: string): void {
    if (error.status === 401) {
      this.signOut();
      return;
    }

    this.taskError.set(message);
  }

  private handleEvidenceError(error: HttpErrorResponse, message: string): void {
    if (error.status === 401) {
      this.signOut();
      return;
    }

    this.evidenceError.set(
      error.status === 403
        ? 'Your studio role does not allow this action.'
        : message
    );
  }
}
