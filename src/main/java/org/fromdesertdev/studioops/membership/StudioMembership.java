package org.fromdesertdev.studioops.membership;

import jakarta.persistence.*;
import org.fromdesertdev.studioops.studio.Studio;
import org.fromdesertdev.studioops.user.AppUser;

import java.time.LocalDateTime;

@Entity
@Table(name = "studio_memberships")
public class StudioMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MembershipRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected StudioMembership() {
    }

    public StudioMembership(Studio studio, AppUser user, MembershipRole role) {
        this.studio = studio;
        this.user = user;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public void changeRole(MembershipRole role) {
        this.role = role;
    }

    public Long getId() { return id; }
    public Studio getStudio() { return studio; }
    public AppUser getUser() { return user; }
    public MembershipRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
