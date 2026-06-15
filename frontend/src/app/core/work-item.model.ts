export type WorkItemStatus =
  | 'TODO'
  | 'IN_PROGRESS'
  | 'BLOCKED'
  | 'DONE'
  | 'CANCELLED';

export type WorkItemPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface WorkItem {
  id: number;
  gameId: number;
  milestoneId: number | null;
  assigneeUserId: number | null;
  assigneeName: string | null;
  title: string;
  description: string | null;
  status: WorkItemStatus;
  priority: WorkItemPriority;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWorkItem {
  milestoneId: number | null;
  assigneeUserId: number | null;
  title: string;
  description: string | null;
  priority: WorkItemPriority;
  dueDate: string | null;
}
