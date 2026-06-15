import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
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
import {
  CreateWorkItem,
  WorkItem,
  WorkItemPriority,
  WorkItemStatus
} from '../../core/work-item.model';
import { WorkItemService } from '../../core/work-item.service';

@Component({
  selector: 'app-game-dashboard',
  imports: [ReactiveFormsModule],
  templateUrl: './game-dashboard.html',
  styleUrl: './game-dashboard.scss'
})
export class GameDashboardPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly gameService = inject(GameService);
  private readonly workItemService = inject(WorkItemService);
  private readonly membershipService = inject(MembershipService);
  private readonly authService = inject(AuthService);

  readonly dashboard = signal<GameDashboard | null>(null);
  readonly game = signal<Game | null>(null);
  readonly workItems = signal<WorkItem[]>([]);
  readonly members = signal<StudioMember[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly taskError = signal('');
  readonly showCreateForm = signal(false);
  readonly savingTask = signal(false);
  readonly updatingItemIds = signal<Set<number>>(new Set());

  readonly statuses: WorkItemStatus[] = [
    'TODO',
    'IN_PROGRESS',
    'BLOCKED',
    'DONE',
    'CANCELLED'
  ];

  readonly priorities: WorkItemPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

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

  signOut(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }

  toggleCreateForm(): void {
    this.taskError.set('');
    this.showCreateForm.update(value => !value);
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
      dashboard: this.gameService.getDashboard(this.gameId),
      game: this.gameService.findById(this.gameId),
      workItems: this.workItemService.findByGame(this.gameId)
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
        next: ({ dashboard, game, workItems, members }) => {
          this.dashboard.set(dashboard);
          this.game.set(game);
          this.workItems.set(workItems);
          this.members.set(members);
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
}
