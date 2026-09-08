package com.jfseat.lowcode.plm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import tools.jackson.databind.JsonNode;

public record PlmActionRequest(
        @NotBlank String actionName,
        @Pattern(regexp = "CREATE|UPDATE|QUERY|ACTION|NAVIGATION") String actionKind,
        String jpoName,
        String methodName,
        @Pattern(regexp = "POST", message = "PLM动作只允许POST请求") String httpMethod,
        JsonNode inputMapping,
        JsonNode outputMapping,
        boolean enabled
) {
}
