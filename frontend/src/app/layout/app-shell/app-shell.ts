import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, startWith } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { AppSidebar } from '../app-sidebar/app-sidebar';

@Component({
  selector: 'app-shell',
  imports: [AppSidebar, RouterOutlet],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.scss'
})
export class AppShell implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly user = this.authService.user;
  readonly pageTitle = signal('Overview');
  readonly pageContext = signal('Studio operating view');

  ngOnInit(): void {
    this.authService.loadCurrentUser().subscribe({
      error: () => {
        this.authService.logout();
        void this.router.navigate(['/login']);
      }
    });

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        startWith(null)
      )
      .subscribe(() => this.updatePageHeading());
  }

  private updatePageHeading(): void {
    let activeRoute = this.route;

    while (activeRoute.firstChild) {
      activeRoute = activeRoute.firstChild;
    }

    this.pageTitle.set(activeRoute.snapshot.data['title'] ?? 'Overview');
    this.pageContext.set(activeRoute.snapshot.data['context'] ?? 'Studio operating view');
  }
}
