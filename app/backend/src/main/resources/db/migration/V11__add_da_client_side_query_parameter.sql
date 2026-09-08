UPDATE lc_plm_action
SET input_mapping_json = '{"page":"${page}","perPage":"${perPage}","clientSide":"${clientSide}"}',
    updated_at = CURRENT_TIMESTAMP
WHERE action_code = 'QUERY_CURRENT_USER_DA_LIST';
