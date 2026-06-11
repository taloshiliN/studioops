package org.fromdesertdev.studioops.workitem;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.milestone.Milestone;
import org.fromdesertdev.studioops.user.AppUser;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "work_items")
public class WorkItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id")
    private Milestone milestone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private AppUser assignee;

    @Column(nullable = false, length = 180)
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WorkItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WorkItemPriority priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    protected WorkItem(){
    }

    public WorkItem(Game game, Milestone milestone, AppUser assignee, String title, String description, WorkItemPriority priority, LocalDate dueDate){
        this.game = game;
        this.milestone = milestone;
        this.assignee = assignee;
        this.title = title;
        this.description = description;
        this.status = WorkItemStatus.TODO;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
        this.updateAt = this.createdAt;
    }

    public void changeStatus(WorkItemStatus status){
        this.status = status;
        this.updateAt = LocalDateTime.now();
    }

    public void assignTo(AppUser assignee){
        this.assignee = assignee;
        this.updateAt = LocalDateTime.now();
    }

    public Long getId(){ return id; }
    public Game getGame(){ return game; }
    public Milestone getMilestone(){ return milestone; }
    public AppUser getAssignee(){ return assignee; }
    public String getTitle(){ return title; }
    public String getDescription() { return description; }
    public WorkItemStatus getStatus() { return status; }
    public WorkItemPriority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updateAt; }
}
