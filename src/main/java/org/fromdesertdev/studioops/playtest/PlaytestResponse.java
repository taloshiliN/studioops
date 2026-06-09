package org.fromdesertdev.studioops.playtest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlaytestResponse(
        Long id,
        Long gameId,
        LocalDate sessionDate,
        String testerGroup,
        String buildVersion,
        String notes,
        String mainFindings,
        LocalDateTime createdAt
) {
    public static PlaytestResponse from(Playtest playtest) {
        return new PlaytestResponse(
                playtest.getId(), playtest.getGame().getId(), playtest.getSessionDate(),
                playtest.getTesterGroup(), playtest.getBuildVersion(), playtest.getNotes(),
                playtest.getMainFindings(), playtest.getCreatedAt()
        );
    }
}
