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
@Table(name = "lc_plm_action")
public class PlmActionDefinition {

    @Id
    private UUID id;

    @Column(name = "action_code", nullable = false, unique = true, length = 100)
    private String actionCode;

    @Column(name = "action_name", nullable = false, length = 200)
    private String actionName;

    @Column(name = "action_kind", nullable = false, length = 30)
    private String actionKind;

    @Column(name = "jpo_name", length = 200)
    private String jpoName;

    @Column(name = "method_name", length = 200)
    private String methodName;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_mapping_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode inputMappingJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_mapping_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode outputMappingJson;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PlmActionDefinition() {
    }

    public PlmActionDefinition(String actionCode, PlmActionRequest request,
                               JsonNode inputMapping, JsonNode outputMapping) {
        this.id = UUID.randomUUID();
        this.actionCode = actionCode;
        this.createdAt = Instant.now();
        update(request, inputMapping, outputMapping);
    }

    /**
     * 更新PLM动作定义
     **
     * @param request 动作配置请求
     * @param inputMapping 输入参数映射
     * @param outputMapping 输出参数映射
     * @author caipan by codex
     * @date 2026/9/3 11:10
     */
    public void update(PlmActionRequest request, JsonNode inputMapping, JsonNode outputMapping) {
        this.actionName = request.actionName().trim();
        this.actionKind = request.actionKind();
        this.jpoName = normalize(request.jpoName());
        this.methodName = normalize(request.methodName());
        this.httpMethod = request.httpMethod();
        this.inputMappingJson = inputMapping;
        this.outputMappingJson = outputMapping;
        this.enabled = request.enabled();
        this.updatedAt = Instant.now();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getActionCode() {
        return actionCode;
    }

    public String getActionName() {
        return actionName;
    }

    public String getActionKind() {
        return actionKind;
    }

    public String getJpoName() {
        return jpoName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public JsonNode getInputMappingJson() {
        return inputMappingJson;
    }

    public JsonNode getOutputMappingJson() {
        return outputMappingJson;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
