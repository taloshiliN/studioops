package org.fromdesertdev.studioops.workitem;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.membership.MembershipRole;
import org.fromdesertdev.studioops.membership.StudioMembershipRepository;
import org.fromdesertdev.studioops.milestone.Milestone;
import org.fromdesertdev.studioops.milestone.MilestoneRepository;
import org.fromdesertdev.studioops.user.AppUser;
import org.fromdesertdev.studioops.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkItemService {
    private final WorkItemRepository workItemRepository;
    private final GameRepository gameRepository;
    private final MilestoneRepository milestoneRepository;
    private final AppUserRepository appUserRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final PermissionService permissionService;

    public WorkItemService(
            WorkItemRepository workItemRepository,
            GameRepository gameRepository,
            MilestoneRepository milestoneRepository,
            AppUserRepository appUserRepository,
            StudioMembershipRepository studioMembershipRepository,
            PermissionService permissionService
    ) {
        this.workItemRepository = workItemRepository;
        this.gameRepository = gameRepository;
        this.milestoneRepository = milestoneRepository;
        this.appUserRepository = appUserRepository;
        this.studioMembershipRepository = studioMembershipRepository;
        this.permissionService = permissionService;
    }

    @Transactional
    public WorkItemResponse create(
            Long gameId,
            CreateWorkItemRequest request
    ) {
        permissionService.requireGameRole(
                gameId,
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        Game game = findGame(gameId);

        Milestone milestone = findMilestoneForGame(
                gameId,
                request.milestoneId()
        );

        AppUser assignee = findAssignableUser(
                game.getStudio().getId(),
                request.assigneeUserId()
        );

        WorkItem workItem = new WorkItem(
                game,
                milestone,
                assignee,
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        );

        return WorkItemResponse.from(
                workItemRepository.save(workItem)
        );
    }

    @Transactional(readOnly = true)
    public List<WorkItemResponse> findByGame(Long gameId) {
        permissionService.requireGameMember(gameId);
        findGame(gameId);

        return workItemRepository
                .findByGame_IdOrderByDueDateAscCreatedAtAsc(gameId)
                .stream()
                .map(WorkItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkItemResponse findById(Long workItemId) {
        WorkItem workItem = findWorkItem(workItemId);

        permissionService.requireGameMember(
                workItem.getGame().getId()
        );

        return WorkItemResponse.from(workItem);
    }

    @Transactional
    public WorkItemResponse updateStatus(
            Long workItemId,
            UpdateWorkItemStatusRequest request
    ) {
        WorkItem workItem = findWorkItem(workItemId);

        permissionService.requireGameRole(
                workItem.getGame().getId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER,
                MembershipRole.DEVELOPER
        );

        workItem.changeStatus(request.status());

        return WorkItemResponse.from(workItem);
    }

    @Transactional
    public WorkItemResponse assign(
            Long workItemId,
            AssignWorkItemRequest request
    ) {
        WorkItem workItem = findWorkItem(workItemId);

        permissionService.requireGameRole(
                workItem.getGame().getId(),
                MembershipRole.OWNER,
                MembershipRole.PRODUCER
        );

        AppUser assignee = findAssignableUser(
                workItem.getGame().getStudio().getId(),
                request.assigneeUserId()
        );

        workItem.assignTo(assignee);

        return WorkItemResponse.from(workItem);
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Game not found"
                ));
    }

    private WorkItem findWorkItem(Long workItemId) {
        return workItemRepository.findById(workItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Work item not found"
                ));
    }

    private Milestone findMilestoneForGame(
            Long gameId,
            Long milestoneId
    ) {
        if (milestoneId == null) {
            return null;
        }

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Milestone not found"
                ));

        if (!milestone.getGame().getId().equals(gameId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Milestone does not belong to the selected game"
            );
        }

        return milestone;
    }

    private AppUser findAssignableUser(
            Long studioId,
            Long assigneeUserId
    ) {
        if (assigneeUserId == null) {
            return null;
        }

        AppUser assignee = appUserRepository.findById(assigneeUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assignee not found"
                ));

        boolean isStudioMember =
                studioMembershipRepository.existsByStudio_IdAndUser_Id(
                        studioId,
                        assigneeUserId
                );

        if (!isStudioMember) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assignee is not a member of this studio"
            );
        }

        return assignee;
    }
}