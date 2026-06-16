export interface Prototype {
  id: number;
  gameId: number;
  gameJamId: number | null;
  name: string;
  buildVersion: string | null;
  itchUrl: string | null;
  repositoryUrl: string | null;
  playableUrl: string | null;
  createdAt: string;
}

export interface CreatePrototype {
  gameId: number;
  gameJamId: number | null;
  name: string;
  buildVersion: string | null;
  itchUrl: string | null;
  repositoryUrl: string | null;
  playableUrl: string | null;
}
