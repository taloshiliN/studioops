package org.fromdesertdev.studioops.gamejam;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_jams")
public class GameJam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 160)
    private String host;

    @Column(length = 160)
    private String theme;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 500)
    private String url;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected GameJam(){

    }

    public GameJam(String name, String host, String theme, LocalDate startDate, LocalDate endDate, String url){
        this.name = name;
        this.host = host;
        this.theme = theme;
        this.startDate = startDate;
        this.endDate = endDate;
        this.url = url;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public String getName(){return name;}
    public String getHost(){return host;}
    public String getTheme() { return theme; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getUrl() { return url; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
