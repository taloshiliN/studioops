package org.fromdesertdev.studioops.prototype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrototypeRepository extends JpaRepository<Prototype, Long> {
    List<Prototype> findByGame_Id(Long gameId);
}
