import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login').then(m => m.Login)
  },
  {
    path: 'games/:gameId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/game-dashboard/game-dashboard')
        .then(m => m.GameDashboardPage)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/app-shell/app-shell').then(m => m.AppShell),
    children: [
      {
        path: 'dashboard',
        data: { title: 'Overview', context: 'Studio operating view' },
        loadComponent: () =>
          import('./features/overview/overview').then(m => m.OverviewPage)
      },
      {
        path: 'games',
        data: { title: 'Games', context: 'Studio portfolio' },
        loadComponent: () =>
          import('./features/games/games').then(m => m.GamesPage)
      },
      {
        path: 'launch-plan',
        data: { title: 'Launch plan', context: 'Validation to market' },
        loadComponent: () =>
          import('./features/launch-plan/launch-plan').then(m => m.LaunchPlanPage)
      },
      {
        path: 'work-items',
        data: { title: 'Work items', context: 'Production operations' },
        loadComponent: () =>
          import('./features/work-items/work-items').then(m => m.WorkItemsPage)
      },
      {
        path: 'marketing',
        data: { title: 'Marketing', context: 'Audience development' },
        loadComponent: () =>
          import('./features/marketing/marketing').then(m => m.MarketingPage)
      },
      {
        path: 'release-readiness',
        data: { title: 'Release readiness', context: 'Shipping confidence' },
        loadComponent: () =>
          import('./features/release-readiness/release-readiness')
            .then(m => m.ReleaseReadinessPage)
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
