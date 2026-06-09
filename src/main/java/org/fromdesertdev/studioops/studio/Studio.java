package org.fromdesertdev.studioops.studio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "studios")
public class Studio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Studio(){
    }

    public Studio(String name){
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId(){
        return id;
    }

    public String GetName(){
        return name;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
}
