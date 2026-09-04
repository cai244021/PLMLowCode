UPDATE lc_page
SET page_code = 'JF_COMPETITIVE_BOM_CREATE'
WHERE page_code = 'WELCOME_PAGE'
  AND NOT EXISTS (
      SELECT 1
      FROM lc_page existing_page
      WHERE existing_page.page_code = 'JF_COMPETITIVE_BOM_CREATE'
  );
