package com.jfseat.lowcode.plm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlmFieldRepository extends JpaRepository<PlmFieldDefinition, UUID> {

    Optional<PlmFieldDefinition> findByFieldCode(String fieldCode);

    List<PlmFieldDefinition> findAllByOrderByFieldCodeAsc();
}
