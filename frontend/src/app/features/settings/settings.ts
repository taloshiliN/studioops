import { Component, inject } from '@angular/core';

import { ThemeId, ThemeService } from '../../core/theme.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.html',
  styleUrls: ['../../layout/workspace-page.scss', './settings.scss']
})
export class SettingsPage {
  private readonly themeService = inject(ThemeService);

  readonly themes = this.themeService.themes;
  readonly selectedTheme = this.themeService.selectedTheme;

  selectTheme(themeId: ThemeId): void {
    this.themeService.setTheme(themeId);
  }
}
