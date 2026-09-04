ALTER TABLE lc_page
    ADD COLUMN plm_config_json JSONB NOT NULL DEFAULT '{}'::jsonb;

ALTER TABLE lc_page_version
    ADD COLUMN plm_config_json JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE TABLE lc_plm_field (
    id UUID PRIMARY KEY,
    field_code VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    object_type VARCHAR(200) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    schema_name VARCHAR(300) NOT NULL,
    data_type VARCHAR(30) NOT NULL,
    required BOOLEAN NOT NULL,
    editable BOOLEAN NOT NULL,
    multiple BOOLEAN NOT NULL,
    i18n_key VARCHAR(300),
    range_source VARCHAR(30) NOT NULL,
    range_config_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE lc_plm_action (
    id UUID PRIMARY KEY,
    action_code VARCHAR(100) NOT NULL UNIQUE,
    action_name VARCHAR(200) NOT NULL,
    action_kind VARCHAR(30) NOT NULL,
    jpo_name VARCHAR(200),
    method_name VARCHAR(200),
    http_method VARCHAR(10) NOT NULL,
    input_mapping_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    output_mapping_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO lc_plm_field (
    id, field_code, display_name, object_type, source_type, schema_name,
    data_type, required, editable, multiple, i18n_key, range_source,
    range_config_json, created_at, updated_at
) VALUES
    ('10000000-0000-0000-0000-000000000001', 'TITLE', '标题', 'JF_CompetitiveBOM', 'ATTRIBUTE', 'PLMEntity.V_Name', 'string', TRUE, TRUE, FALSE, 'emxFramework.Attribute.Title', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000002', 'DESCRIPTION', '描述', 'JF_CompetitiveBOM', 'BASIC', 'description', 'textarea', FALSE, TRUE, FALSE, 'emxFramework.Basic.Description', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000003', 'PART_TYPE', '零件子类型', 'JF_CompetitiveBOM', 'ATTRIBUTE', 'JF_VPMReference.JF_PartType', 'enum', TRUE, TRUE, FALSE, 'emxFramework.Attribute.JF_PartType', 'FIXED', '{"options":[{"label":"整椅","value":"C"},{"label":"面套","value":"T"},{"label":"发泡","value":"U"}]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000004', 'OBJECT_ID', '对象ID', '*', 'BASIC', 'id', 'string', FALSE, FALSE, FALSE, NULL, 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000005', 'CURRENT', '成熟度状态', '*', 'BASIC', 'current', 'string', FALSE, FALSE, FALSE, 'emxFramework.Basic.Current', 'NONE', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO lc_plm_action (
    id, action_code, action_name, action_kind, jpo_name, method_name,
    http_method, input_mapping_json, output_mapping_json, enabled,
    created_at, updated_at
) VALUES
    ('20000000-0000-0000-0000-000000000001', 'CREATE_COMPETITIVE_BOM', '创建竞品BOM', 'CREATE', 'JF_CompetitiveBOM', 'createCompetitiveBOM', 'POST', '{"title":"${title}","description":"${description}","partType":"${partType}"}', '{"objectId":"data.objectId","name":"data.name"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', 'QUERY_COMPETITIVE_BOM', '查询竞品BOM', 'QUERY', 'JF_CompetitiveBOM', 'getCompetitiveBOMList', 'POST', '{"objectId":"${plmContext.objectId}","page":"${page}","pageSize":"${perPage}"}', '{"items":"data.items","total":"data.total"}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
