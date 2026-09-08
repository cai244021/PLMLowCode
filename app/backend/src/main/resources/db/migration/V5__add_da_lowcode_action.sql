INSERT INTO lc_plm_action (
    id, action_code, action_name, action_kind, jpo_name, method_name,
    http_method, input_mapping_json, output_mapping_json, enabled,
    created_at, updated_at
) VALUES (
    '20000000-0000-0000-0000-000000000003',
    'QUERY_CURRENT_USER_DA_LIST',
    '查询当前用户DA列表',
    'QUERY',
    'JF_LowCode',
    'getCurrentUserDAListLowCode',
    'POST',
    '{"page":"${page}","perPage":"${perPage}"}',
    '{"items":"data.items","total":"data.total"}',
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
