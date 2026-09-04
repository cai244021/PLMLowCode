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
@Table(name = "lc_page")
public class LowCodePage {

    @Id
    private UUID id;

    @Column(name = "page_code", nullable = false, unique = true, length = 100)
    private String pageCode;

    @Column(name = "page_name", nullable = false, length = 200)
    private String pageName;

    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode schemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plm_config_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode plmConfigJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected LowCodePage() {
    }

    public LowCodePage(String pageCode, String pageName, JsonNode schemaJson, JsonNode plmConfigJson) {
        this.id = UUID.randomUUID();
        this.pageCode = pageCode;
        this.pageName = pageName;
        this.schemaJson = schemaJson;
        this.plmConfigJson = plmConfigJson;
        this.currentVersion = 0;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getPageCode() {
        return pageCode;
    }

    public String getPageName() {
        return pageName;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public JsonNode getSchemaJson() {
        return schemaJson;
    }

    public JsonNode getPlmConfigJson() {
        return plmConfigJson;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 更新页面内容并递增版本号
     **
     * @param pageName 页面名称
     * @param schemaJson AMIS页面JSON
     * @param plmConfigJson PLM绑定配置JSON
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    public void update(String pageName, JsonNode schemaJson, JsonNode plmConfigJson) {
        this.pageName = pageName;
        this.schemaJson = schemaJson;
        this.plmConfigJson = plmConfigJson;
        this.currentVersion++;
        this.updatedAt = Instant.now();
    }

    /**
     * 判断页面是否已软删除
     **
     * @return 已删除时返回true
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 标记页面删除，保留内容、编码和历史版本
     **
     * @author caipan by codex
     * @date 2026/9/4 16:00
     */
    public void markDeleted() {
        deletedAt = Instant.now();
    }
}
