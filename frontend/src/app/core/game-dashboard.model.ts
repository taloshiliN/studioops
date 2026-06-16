export interface GameDashboard {
  game: {
    id: number;
    title: string;
    currentStage: string;
    validationStatus: string;
  };

  validation: {
    latestDecision: string;
    reason: string;
    decidedAt: string;
  } | null;

  traction: {
    latestSource: string;
    views: number;
    downloads: number;
    plays: number;
    ratingsCount: number;
    averageRating: number;
    commentsCount: number;
    followersGained: number;
    wishlists: number;
    revenueCents: number;
    capturedAt: string;
  } | null;

  milestones: {
    total: number;
    completed: number;
    inProgress: number;
    blocked: number;
  };

  workItems: {
    total: number;
    todo: number;
    inProgress: number;
    blocked: number;
    done: number;
    overdue: number;
  };

  playtests: {
    total: number;
    latestSessionDate: string | null;
    latestBuildVersion: string | null;
    latestMainFindings: string | null;
  };

  marketing: {
    total: number;
    completed: number;
    upcoming: number;
    nextActivityType: string | null;
    nextChannel: string | null;
    nextTitle: string | null;
    nextScheduledFor: string | null;
  };

  launchPlan: {
    itchPageUrl: string | null;
    steamPageUrl: string | null;
    demoUrl: string | null;
    trailerUrl: string | null;
    targetDemoDate: string | null;
    targetNextFestDate: string | null;
    targetLaunchDate: string | null;
    contentCreatorOutreachTarget: number;
    festivalSubmissionTarget: number;
    readinessPercentage: number;
    missingItems: string[];
  };

  releaseReadiness: {
    totalItems: number;
    completedItems: number;
    readinessPercentage: number;
    blocked: boolean;
    blockingItems: string[];
  };
}
