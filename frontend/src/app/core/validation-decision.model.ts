export type ValidationDecisionType =
  | 'GREENLIGHT'
  | 'PIVOT'
  | 'SHELVE'
  | 'NEEDS_MORE_TESTING';

export interface ValidationDecision {
  id: number;
  gameId: number;
  decision: ValidationDecisionType;
  reason: string;
  currentStage: string;
  validationStatus: string;
  decidedAt: string;
}

export interface CreateValidationDecision {
  decision: ValidationDecisionType;
  reason: string;
}
