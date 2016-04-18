CREATE INDEX idx_lookup_resourceid
  ON {schema_name}.lookup
  USING btree
  (resourceId);
  
CREATE INDEX idx_lookup_btree_ops
   ON {schema_name}.lookup 
   USING btree (lookupkey text_pattern_ops);
  
CREATE INDEX idx_acl_resourceid
  ON {schema_name}.acl_entry
  USING btree
  (resourceId);

DROP EXTENSION IF EXISTS pg_trgm CASCADE;
CREATE EXTENSION pg_trgm;

CREATE INDEX idx_lookup_trgm_gist
   ON {schema_name}.lookup 
   USING gist (lookupkey COLLATE pg_catalog."default" gist_trgm_ops);
