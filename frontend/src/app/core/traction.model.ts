export type TractionSource =
  | 'ITCH_IO'
  | 'STEAM'
  | 'X_TWITTER'
  | 'YOUTUBE'
  | 'TIKTOK'
  | 'EVENT'
  | 'DISCORD'
  | 'NEWSLETTER'
  | 'OTHER';

export interface TractionSnapshot {
  id: number;
  gameId: number;
  prototypeId: number | null;
  source: TractionSource;
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
}

export interface CreateTractionSnapshot {
  gameId: number;
  prototypeId: number | null;
  source: TractionSource;
  views: number;
  downloads: number;
  plays: number;
  ratingsCount: number;
  averageRating: number;
  commentsCount: number;
  followersGained: number;
  wishlists: number;
  revenueCents: number;
  capturedAt: string | null;
}
