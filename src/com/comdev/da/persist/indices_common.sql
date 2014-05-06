CREATE INDEX idx_path ON {schema_name}.resource_ref
    ( path ASC );

CREATE INDEX idx_filename ON {schema_name}.resource_ref
    ( filename ASC );

CREATE INDEX idx_filekey ON {schema_name}.resource_ref
    ( fileKey ASC );

CREATE INDEX idx_fullpath ON {schema_name}.resource_ref
    ( path, filename );

CREATE INDEX idx_acl_principal on {schema_name}.acl_entry
  ( principalName ASC );
    