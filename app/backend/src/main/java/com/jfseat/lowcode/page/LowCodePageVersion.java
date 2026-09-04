package com.jfseat.lowcode.page;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lc_page_version")
public class LowCodePageVersion {

    @Id
    private UUID id;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode schemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plm_config_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode plmConfigJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LowCodePageVersion() {
    }

    public LowCodePageVersion(UUID pageId, int versionNo, JsonNode schemaJson, JsonNode plmConfigJson) {
        this.id = UUID.randomUUID();
        this.pageId = pageId;
        this.versionNo = versionNo;
        this.schemaJson = schemaJson.deepCopy();
        this.plmConfigJson = plmConfigJson.deepCopy();
        this.createdAt = Instant.now();
    }
}
