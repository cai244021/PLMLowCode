INSERT INTO lc_plm_field (
    id, field_code, display_name, object_type, source_type, schema_name,
    data_type, required, editable, multiple, i18n_key, range_source,
    range_config_json, created_at, updated_at
) VALUES
    ('10000000-0000-0000-0000-000000000008', 'DA_NAME', '名称', 'JFDA', 'BASIC', 'name', 'string', FALSE, FALSE, FALSE, 'emxFramework.Basic.Name', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000009', 'DA_PROJECT_NAME', '项目名称', 'JFDA', 'ATTRIBUTE', 'JFProjectName', 'string', FALSE, FALSE, FALSE, 'emxFramework.Attribute.JFProjectName', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000010', 'DA_TITLE', '标题', 'JFDA', 'ATTRIBUTE', 'Title', 'string', TRUE, TRUE, FALSE, 'emxFramework.Attribute.Title', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000011', 'DA_CURRENT', '状态', 'JFDA', 'BASIC', 'current', 'string', FALSE, FALSE, FALSE, 'emxFramework.Basic.Current', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000012', 'DA_CHANGE_TYPE', '变更类型', 'JFDA', 'ATTRIBUTE', 'JFChangeType', 'enum', TRUE, TRUE, FALSE, 'emxFramework.Attribute.JFChangeType', 'PLM_RANGE', '{"attributeName":"JFChangeType"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000013', 'DA_PROJECT_PHASE', '项目阶段', 'JFDA', 'ATTRIBUTE', 'JFProjectPhase', 'enum', TRUE, TRUE, FALSE, 'emxFramework.Attribute.JFProjectPhase', 'PLM_RANGE', '{"attributeName":"JFProjectPhase"}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000014', 'DA_REASON', '偏差原因/描述', 'JFDA', 'ATTRIBUTE', 'JFReasonDeviation', 'textarea', TRUE, TRUE, FALSE, 'emxFramework.Attribute.JFReasonDeviation', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000015', 'DA_BEFORE_CHANGE', '变更前', 'JFDA', 'ATTRIBUTE', 'JFBeforeChange', 'textarea', TRUE, TRUE, FALSE, 'emxFramework.Attribute.JFBeforeChange', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000016', 'DA_AFTER_CHANGE', '变更后', 'JFDA', 'ATTRIBUTE', 'JFAfterChange', 'textarea', TRUE, TRUE, FALSE, 'emxFramework.Attribute.JFAfterChange', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000017', 'DA_START_TIME', 'DA启动时间', 'JFDA', 'ATTRIBUTE', 'JFDAStartTime', 'datetime', FALSE, FALSE, FALSE, 'emxFramework.Attribute.JFDAStartTime', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000018', 'DA_CLOSE_TIME', 'DA关闭时间', 'JFDA', 'ATTRIBUTE', 'JFDACloseTime', 'datetime', FALSE, FALSE, FALSE, 'emxFramework.Attribute.JFDACloseTime', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000019', 'DA_EXTENSION_TIME', '延期关闭时间', 'JFDA', 'ATTRIBUTE', 'JFDAExtensionTime', 'datetime', FALSE, FALSE, FALSE, 'emxFramework.Label.JFDAExtensionTime', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000020', 'DA_OWNER', '创建者', 'JFDA', 'BASIC', 'owner', 'string', FALSE, FALSE, FALSE, 'emxFramework.Basic.Owner', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000021', 'DA_ORIGINATED', '创建时间', 'JFDA', 'BASIC', 'originated', 'datetime', FALSE, FALSE, FALSE, 'emxFramework.Basic.Originated', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (field_code) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    object_type = EXCLUDED.object_type,
    source_type = EXCLUDED.source_type,
    schema_name = EXCLUDED.schema_name,
    data_type = EXCLUDED.data_type,
    required = EXCLUDED.required,
    editable = EXCLUDED.editable,
    multiple = EXCLUDED.multiple,
    i18n_key = EXCLUDED.i18n_key,
    range_source = EXCLUDED.range_source,
    range_config_json = EXCLUDED.range_config_json,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO lc_plm_action (
    id, action_code, action_name, action_kind, jpo_name, method_name,
    http_method, input_mapping_json, output_mapping_json, enabled,
    created_at, updated_at
) VALUES (
    '20000000-0000-0000-0000-000000000006',
    'QUERY_PAGE_FIELD_METADATA',
    '解析页面PLM国际化元数据',
    'QUERY',
    'JF_LowCode',
    'getPageFieldMetadataLowCode',
    'POST',
    '{"fields":"${fields}"}',
    '{"fields":"data.fields"}',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (action_code) DO UPDATE SET
    action_name = EXCLUDED.action_name,
    action_kind = EXCLUDED.action_kind,
    jpo_name = EXCLUDED.jpo_name,
    method_name = EXCLUDED.method_name,
    http_method = EXCLUDED.http_method,
    input_mapping_json = EXCLUDED.input_mapping_json,
    output_mapping_json = EXCLUDED.output_mapping_json,
    enabled = EXCLUDED.enabled,
    updated_at = CURRENT_TIMESTAMP;
