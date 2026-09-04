package com.jfseat.lowcode.plm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlmActionRepository extends JpaRepository<PlmActionDefinition, UUID> {

    Optional<PlmActionDefinition> findByActionCode(String actionCode);

    List<PlmActionDefinition> findAllByOrderByActionCodeAsc();
}
