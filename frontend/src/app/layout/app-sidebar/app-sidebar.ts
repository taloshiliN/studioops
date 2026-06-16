import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './app-sidebar.html',
  styleUrl: './app-sidebar.scss'
})
export class AppSidebar {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly menuOpen = signal(false);

  toggleMenu(): void {
    this.menuOpen.update(open => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  signOut(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
