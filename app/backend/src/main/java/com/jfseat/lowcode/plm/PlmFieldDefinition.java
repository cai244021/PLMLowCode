package com.jfseat.lowcode.plm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lc_plm_field")
public class PlmFieldDefinition {

    @Id
    private UUID id;

    @Column(name = "field_code", nullable = false, unique = true, length = 100)
    private String fieldCode;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "object_type", nullable = false, length = 200)
    private String objectType;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "schema_name", nullable = false, length = 300)
    private String schemaName;

    @Column(name = "data_type", nullable = false, length = 30)
    private String dataType;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private boolean editable;

    @Column(nullable = false)
    private boolean multiple;

    @Column(name = "i18n_key", length = 300)
    private String i18nKey;

    @Column(name = "range_source", nullable = false, length = 30)
    private String rangeSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "range_config_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode rangeConfigJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlmFieldDefinition() {
    }

    public PlmFieldDefinition(String fieldCode, PlmFieldRequest request, JsonNode rangeConfigJson) {
        this.id = UUID.randomUUID();
        this.fieldCode = fieldCode;
        this.createdAt = Instant.now();
        update(request, rangeConfigJson);
    }

    /**
     * 更新PLM字段定义
     **
     * @param request 字段配置请求
     * @param rangeConfigJson Range配置JSON
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    public void update(PlmFieldRequest request, JsonNode rangeConfigJson) {
        this.displayName = request.displayName().trim();
        this.objectType = request.objectType().trim();
        this.sourceType = request.sourceType();
        this.schemaName = request.schemaName().trim();
        this.dataType = request.dataType();
        this.required = request.required();
        this.editable = request.editable();
        this.multiple = request.multiple();
        this.i18nKey = request.i18nKey() == null ? null : request.i18nKey().trim();
        this.rangeSource = request.rangeSource();
        this.rangeConfigJson = rangeConfigJson;
        this.updatedAt = Instant.now();
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public String getRangeSource() {
        return rangeSource;
    }

    public JsonNode getRangeConfigJson() {
        return rangeConfigJson;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
