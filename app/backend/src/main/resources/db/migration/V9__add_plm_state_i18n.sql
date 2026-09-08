UPDATE lc_plm_field
SET range_source = 'PLM_STATE',
    range_config_json = '{"policyName":"JFDA"}',
    updated_at = CURRENT_TIMESTAMP
WHERE field_code = 'DA_CURRENT';
