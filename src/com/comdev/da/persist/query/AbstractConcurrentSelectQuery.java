package com.comdev.da.persist.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.comdev.da.auth.Identity;
import com.comdev.da.persist.DbConnectionFactory;

public abstract class AbstractConcurrentSelectQuery<T, P> extends AbstractSelectQuery<T, P>
{

    public AbstractConcurrentSelectQuery( DbConnectionFactory dbFactory,
                                          String sql,
                                          String schema,
                                          Identity ident )
    {
        super( dbFactory, sql, schema, ident );
    }

    @Override
    PreparedStatement registerStatement( P[] id )
        throws SQLException
    {
        return registerStatement( id, false );
    }

}
