INSERT INTO lc_plm_field (
    id, field_code, display_name, object_type, source_type, schema_name,
    data_type, required, editable, multiple, i18n_key, range_source,
    range_config_json, created_at, updated_at
) VALUES (
    '10000000-0000-0000-0000-000000000006',
    'AFFECTED_PLANT',
    '影响工厂',
    'JFDA',
    'ATTRIBUTE',
    'JFAffectsFactory',
    'enum',
    TRUE,
    TRUE,
    TRUE,
    'emxFramework.Attribute.JFAffectsFactory',
    'PLM_RANGE',
    '{"attributeName":"JFAffectsFactory"}',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (field_code) DO UPDATE SET
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
    '20000000-0000-0000-0000-000000000005',
    'QUERY_ATTRIBUTE_RANGE',
    '查询PLM属性Range',
    'QUERY',
    'JF_LowCode',
    'getAttributeRangeOptionsLowCode',
    'POST',
    '{"attributeName":"${attributeName}"}',
    '{"options":"data.options"}',
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
