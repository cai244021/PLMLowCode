package com.jfseat.lowcode.plm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import tools.jackson.databind.JsonNode;

public record PlmFieldRequest(
        @NotBlank String displayName,
        @NotBlank String objectType,
        @Pattern(regexp = "BASIC|ATTRIBUTE|RELATIONSHIP|PROGRAM") String sourceType,
        @NotBlank String schemaName,
        @Pattern(regexp = "string|textarea|number|boolean|date|datetime|enum|object") String dataType,
        boolean required,
        boolean editable,
        boolean multiple,
        String i18nKey,
        @Pattern(regexp = "NONE|FIXED|PLM_RANGE|PLM_STATE|JPO") String rangeSource,
        JsonNode rangeConfig
) {
}
