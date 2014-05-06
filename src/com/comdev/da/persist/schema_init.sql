CREATE TABLE {schema_name}.resource_ref(
    uid SERIAL PRIMARY KEY,
    path VARCHAR(512) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    createdTime TIMESTAMP NOT NULL,
    lastAccessed TIMESTAMP NOT NULL,
    lastModified TIMESTAMP NOT NULL,
    size BIGINT NOT NULL,
    fileKey INTEGER NOT NULL
);

CREATE TABLE {schema_name}.lookup(
    uid SERIAL PRIMARY KEY,
    lookupKey VARCHAR(255) NOT NULL,
    resourceId BIGINT NOT NULL REFERENCES {schema_name}.resource_ref(uid) ON DELETE CASCADE,
    createdTime TIMESTAMP NOT NULL,
    updatedTime TIMESTAMP
);

CREATE TABLE {schema_name}.acl_entry(
    uid SERIAL PRIMARY KEY,
    resourceId BIGINT NOT NULL REFERENCES {schema_name}.resource_ref(uid) ON DELETE CASCADE,
    principalName VARCHAR(255) NOT NULL,
    lastModified TIMESTAMP NOT NULL
);
