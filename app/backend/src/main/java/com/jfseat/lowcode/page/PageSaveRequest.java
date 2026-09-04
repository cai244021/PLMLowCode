package com.jfseat.lowcode.page;

import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PageSaveRequest(
        @NotBlank(message = "页面名称不能为空") @Size(max = 200, message = "页面名称不能超过200字") String pageName,
        @NotNull(message = "页面JSON不能为空") JsonNode schema,
        JsonNode plmConfig
) {
}
