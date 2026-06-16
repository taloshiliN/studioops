export interface ReleaseChecklistItem {
  id: number;
  gameId: number;
  title: string;
  description: string | null;
  completed: boolean;
  blocksRelease: boolean;
  createdAt: string;
}

export interface ReleaseReadiness {
  gameId: number;
  totalItems: number;
  completedItems: number;
  readinessPercentage: number;
  blocked: boolean;
  blockingItems: string[];
}

export interface CreateReleaseChecklistItem {
  title: string;
  description: string | null;
  blocksRelease: boolean;
}

export interface UpdateReleaseChecklistCompletion {
  completed: boolean;
}
