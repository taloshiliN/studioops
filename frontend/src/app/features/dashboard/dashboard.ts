import {Component, inject, OnInit} from '@angular/core';
import {AuthService} from '../../core/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.authService.user;

  ngOnInit() {
    this.authService.loadCurrentUser().subscribe({
      error: () => this.signOut()
    })
  }

  signOut(): void{
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
