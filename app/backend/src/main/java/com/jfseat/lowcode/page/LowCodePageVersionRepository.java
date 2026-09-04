package com.jfseat.lowcode.page;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LowCodePageVersionRepository extends JpaRepository<LowCodePageVersion, UUID> {
}

