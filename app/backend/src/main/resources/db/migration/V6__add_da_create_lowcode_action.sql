INSERT INTO lc_plm_action (
    id, action_code, action_name, action_kind, jpo_name, method_name,
    http_method, input_mapping_json, output_mapping_json, enabled,
    created_at, updated_at
) VALUES (
    '20000000-0000-0000-0000-000000000004',
    'CREATE_DA',
    '创建DA申请单',
    'CREATE',
    'JF_LowCode',
    'createDALowCode',
    'POST',
    '{"projectId":"${projectId}","title":"${title}","changeType":"${changeType}","projectPhase":"${projectPhase}","affectedPlant":"${affectedPlant}","deviationReason":"${deviationReason}","beforeChange":"${beforeChange}","afterChange":"${afterChange}"}',
    '{"name":"data.name","objectId":"data.objectId"}',
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
