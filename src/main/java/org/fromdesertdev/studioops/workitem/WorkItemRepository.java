package org.fromdesertdev.studioops.workitem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    List<WorkItem> findByGame_IdOrderByDueDateAscCreatedAtAsc(Long gameId);
    List<WorkItem> findByMilestone_IdOrderByDueDateAscCreatedAtAsc(Long milestoneId);
    List<WorkItem> findByAssignee_IdOrderByDueDateAscCreatedAtAsc(Long assigneeId);

    void deleteByGame_Id(Long gameId);
}
