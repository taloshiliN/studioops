export interface LaunchPlan {
  id: number | null;
  gameId: number;
  itchPageUrl: string | null;
  steamPageUrl: string | null;
  demoUrl: string | null;
  trailerUrl: string | null;
  targetDemoDate: string | null;
  targetNextFestDate: string | null;
  targetLaunchDate: string | null;
  contentCreatorOutreachTarget: number;
  festivalSubmissionTarget: number;
  notes: string | null;
  readinessPercentage: number;
  missingItems: string[];
  createdAt: string | null;
  updatedAt: string | null;
}

export interface UpsertLaunchPlan {
  itchPageUrl: string | null;
  steamPageUrl: string | null;
  demoUrl: string | null;
  trailerUrl: string | null;
  targetDemoDate: string | null;
  targetNextFestDate: string | null;
  targetLaunchDate: string | null;
  contentCreatorOutreachTarget: number;
  festivalSubmissionTarget: number;
  notes: string | null;
}
