export interface Game {
  id: number;
  studioId: number;
  title: string;
  shortPitch: string | null;
  genre: string | null;
  currentStage: string;
  validationStatus: string;
  targetPlatforms: string | null;
  fontFamily: string | null;
  createdAt: string;
}

export interface CreateGame {
  studioId: number;
  title: string;
  shortPitch: string | null;
  genre: string | null;
  targetPlatforms: string | null;
  fontFamily: string;
}
