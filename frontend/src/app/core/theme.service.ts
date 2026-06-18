import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type ThemeId = 'studio-blue' | 'mint' | 'night';

export interface ThemeOption {
  id: ThemeId;
  name: string;
  description: string;
}

const STORAGE_KEY = 'studioops-theme';
const DEFAULT_THEME: ThemeId = 'studio-blue';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);

  readonly themes: readonly ThemeOption[] = [
    {
      id: 'studio-blue',
      name: 'Studio Blue',
      description: 'Matches the sketch: blue workspace, gray sidebar, bold buttons.'
    },
    {
      id: 'mint',
      name: 'Mint Desk',
      description: 'A softer green workspace for calmer planning sessions.'
    },
    {
      id: 'night',
      name: 'Night Build',
      description: 'A darker production mood with brighter navigation controls.'
    }
  ];

  readonly selectedTheme = signal<ThemeId>(this.readStoredTheme());

  constructor() {
    this.applyTheme(this.selectedTheme());
  }

  initialize(): void {
    this.applyTheme(this.selectedTheme());
  }

  setTheme(themeId: ThemeId): void {
    if (!this.isThemeId(themeId)) {
      return;
    }

    this.selectedTheme.set(themeId);
    this.applyTheme(themeId);
    this.writeStoredTheme(themeId);
  }

  private applyTheme(themeId: ThemeId): void {
    this.document.documentElement.dataset['theme'] = themeId;
  }

  private readStoredTheme(): ThemeId {
    const storedTheme = this.document.defaultView?.localStorage.getItem(STORAGE_KEY) ?? null;
    return this.isThemeId(storedTheme) ? storedTheme : DEFAULT_THEME;
  }

  private writeStoredTheme(themeId: ThemeId): void {
    this.document.defaultView?.localStorage.setItem(STORAGE_KEY, themeId);
  }

  private isThemeId(value: string | null): value is ThemeId {
    return value === 'studio-blue' || value === 'mint' || value === 'night';
  }
}
