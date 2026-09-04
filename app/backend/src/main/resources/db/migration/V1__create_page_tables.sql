CREATE TABLE lc_page (
    id UUID PRIMARY KEY,
    page_code VARCHAR(100) NOT NULL UNIQUE,
    page_name VARCHAR(200) NOT NULL,
    current_version INTEGER NOT NULL,
    schema_json JSONB NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE lc_page_version (
    id UUID PRIMARY KEY,
    page_id UUID NOT NULL REFERENCES lc_page(id),
    version_no INTEGER NOT NULL,
    schema_json JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_lc_page_version UNIQUE (page_id, version_no)
);

CREATE INDEX idx_lc_page_version_page_id
    ON lc_page_version(page_id);

