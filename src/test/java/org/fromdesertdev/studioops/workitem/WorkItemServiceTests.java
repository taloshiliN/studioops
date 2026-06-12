package org.fromdesertdev.studioops.workitem;

import org.fromdesertdev.studioops.authorization.PermissionService;
import org.fromdesertdev.studioops.game.Game;
import org.fromdesertdev.studioops.game.GameRepository;
import org.fromdesertdev.studioops.membership.StudioMembershipRepository;
import org.fromdesertdev.studioops.milestone.Milestone;
import org.fromdesertdev.studioops.milestone.MilestoneRepository;
import org.fromdesertdev.studioops.studio.Studio;
import org.fromdesertdev.studioops.user.AppUser;
import org.fromdesertdev.studioops.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkItemServiceTests {
    private WorkItemRepository workItemRepository;
    private GameRepository gameRepository;
    private MilestoneRepository milestoneRepository;
    private AppUserRepository appUserRepository;
    private StudioMembershipRepository membershipRepository;
    private PermissionService permissionService;
    private WorkItemService service;

    @BeforeEach
    void setUp() {
        workItemRepository = mock(WorkItemRepository.class);
        gameRepository = mock(GameRepository.class);
        milestoneRepository = mock(MilestoneRepository.class);
        appUserRepository = mock(AppUserRepository.class);
        membershipRepository = mock(StudioMembershipRepository.class);
        permissionService = mock(PermissionService.class);
        service = new WorkItemService(workItemRepository, gameRepository, milestoneRepository,
                appUserRepository, membershipRepository, permissionService);
    }

    @Test
    void creatingWorkItemDefaultsToTodo() {
        Game game = game(3L, 2L);
        when(gameRepository.findById(3L)).thenReturn(Optional.of(game));
        when(workItemRepository.save(any(WorkItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkItemResponse response = service.create(3L, request(null, null));

        assertEquals(WorkItemStatus.TODO, response.status());
        assertEquals(WorkItemPriority.HIGH, response.priority());
        assertNull(response.assigneeUserId());
        assertNull(response.milestoneId());
        verify(workItemRepository).save(any(WorkItem.class));
    }

    @Test
    void rejectsMilestoneFromAnotherGame() {
        Game selectedGame = game(3L, 2L);
        Game otherGame = game(4L, 3L);
        Milestone milestone = mock(Milestone.class);
        when(gameRepository.findById(3L)).thenReturn(Optional.of(selectedGame));
        when(milestoneRepository.findById(10L)).thenReturn(Optional.of(milestone));
        when(milestone.getGame()).thenReturn(otherGame);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create(3L, request(10L, null)));

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Milestone does not belong to the selected game", exception.getReason());
    }

    @Test
    void rejectsAssigneeOutsideGameStudio() {
        Game game = game(3L, 2L);
        when(gameRepository.findById(3L)).thenReturn(Optional.of(game));
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(mock(AppUser.class)));
        when(membershipRepository.existsByStudio_IdAndUser_Id(2L, 7L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.create(3L, request(null, 7L)));

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Assignee is not a member of this studio", exception.getReason());
    }

    @Test
    void updatesWorkItemStatus() {
        WorkItem workItem = workItem(game(3L, 2L));
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));

        WorkItemResponse response = service.updateStatus(
                1L, new UpdateWorkItemStatusRequest(WorkItemStatus.IN_PROGRESS));

        assertEquals(WorkItemStatus.IN_PROGRESS, response.status());
    }

    @Test
    void assignsValidStudioMember() {
        WorkItem workItem = workItem(game(3L, 2L));
        AppUser assignee = mock(AppUser.class);
        when(workItemRepository.findById(1L)).thenReturn(Optional.of(workItem));
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(assignee));
        when(membershipRepository.existsByStudio_IdAndUser_Id(2L, 3L)).thenReturn(true);
        when(assignee.getId()).thenReturn(3L);
        when(assignee.getName()).thenReturn("Placeholder Joe");

        WorkItemResponse response = service.assign(1L, new AssignWorkItemRequest(3L));

        assertEquals(3L, response.assigneeUserId());
        assertEquals("Placeholder Joe", response.assigneeName());
    }

    private CreateWorkItemRequest request(Long milestoneId, Long assigneeUserId) {
        return new CreateWorkItemRequest(milestoneId, assigneeUserId,
                "Implement player movement", "Add movement controls and collision handling.",
                WorkItemPriority.HIGH, LocalDate.of(2026, 6, 20));
    }

    private WorkItem workItem(Game game) {
        return new WorkItem(game, null, null, "Implement player movement",
                "Add movement controls", WorkItemPriority.HIGH, LocalDate.of(2026, 6, 20));
    }

    private Game game(Long gameId, Long studioId) {
        Studio studio = mock(Studio.class);
        Game game = mock(Game.class);
        when(studio.getId()).thenReturn(studioId);
        when(game.getId()).thenReturn(gameId);
        when(game.getStudio()).thenReturn(studio);
        return game;
    }
}
