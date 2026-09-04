package com.jfseat.lowcode.plm;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record PlmFieldResponse(
        String fieldCode,
        String displayName,
        String objectType,
        String sourceType,
        String schemaName,
        String dataType,
        boolean required,
        boolean editable,
        boolean multiple,
        String i18nKey,
        String rangeSource,
        JsonNode rangeConfig,
        Instant updatedAt
) {
    /**
     * 将PLM字段实体转换为接口响应
     **
     * @param field 字段实体
     * @return 字段响应
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    public static PlmFieldResponse from(PlmFieldDefinition field) {
        return new PlmFieldResponse(
                field.getFieldCode(), field.getDisplayName(), field.getObjectType(),
                field.getSourceType(), field.getSchemaName(), field.getDataType(),
                field.isRequired(), field.isEditable(), field.isMultiple(),
                field.getI18nKey(), field.getRangeSource(), field.getRangeConfigJson(),
                field.getUpdatedAt()
        );
    }
}
