INSERT INTO lc_plm_action (
    id, action_code, action_name, action_kind, jpo_name, method_name,
    http_method, input_mapping_json, output_mapping_json, enabled,
    created_at, updated_at
) VALUES
    (
        '20000000-0000-0000-0000-000000000008',
        'QUERY_PLM_REPORT_SUMMARY',
        '查询PLM通用报表汇总',
        'QUERY',
        'JF_LowCode',
        'getReportSummaryLowCode',
        'POST',
        '{}',
        '{}',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '20000000-0000-0000-0000-000000000009',
        'QUERY_PLM_REPORT_DETAILS',
        '查询PLM通用报表明细',
        'QUERY',
        'JF_LowCode',
        'getReportDetailsLowCode',
        'POST',
        '{}',
        '{"items":"data.items","total":"data.total"}',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (action_code) DO UPDATE SET
    action_name = EXCLUDED.action_name,
    action_kind = EXCLUDED.action_kind,
    jpo_name = EXCLUDED.jpo_name,
    method_name = EXCLUDED.method_name,
    http_method = EXCLUDED.http_method,
    input_mapping_json = EXCLUDED.input_mapping_json,
    output_mapping_json = EXCLUDED.output_mapping_json,
    enabled = EXCLUDED.enabled,
    updated_at = CURRENT_TIMESTAMP;
