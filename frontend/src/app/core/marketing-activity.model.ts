export type MarketingActivityType =
  | 'EVENT'
  | 'SOCIAL_POST'
  | 'DEVLOG'
  | 'TRAILER'
  | 'PRESS_EMAIL'
  | 'DISCORD_ANNOUNCEMENT'
  | 'STEAM_FESTIVAL'
  | 'ITCH_UPDATE'
  | 'OTHER';

export interface MarketingActivity {
  id: number;
  gameId: number;
  activityType: MarketingActivityType;
  channel: string;
  title: string;
  scheduledFor: string | null;
  completedAt: string | null;
  resultNotes: string | null;
  createdAt: string;
}

export interface CreateMarketingActivity {
  activityType: MarketingActivityType;
  channel: string;
  title: string;
  scheduledFor: string | null;
}

export interface CompleteMarketingActivity {
  resultNotes: string | null;
}
