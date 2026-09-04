package com.jfseat.lowcode.plm;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record PlmActionResponse(
        String actionCode,
        String actionName,
        String actionKind,
        String jpoName,
        String methodName,
        String httpMethod,
        JsonNode inputMapping,
        JsonNode outputMapping,
        boolean enabled,
        Instant updatedAt
) {
    /**
     * 将PLM动作实体转换为接口响应
     **
     * @param action 动作实体
     * @return 动作响应
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    public static PlmActionResponse from(PlmActionDefinition action) {
        return new PlmActionResponse(
                action.getActionCode(), action.getActionName(), action.getActionKind(),
                action.getJpoName(), action.getMethodName(), action.getHttpMethod(),
                action.getInputMappingJson(), action.getOutputMappingJson(),
                action.isEnabled(), action.getUpdatedAt()
        );
    }
}
