package com.rcoe.da.dbconnector.query;

import javax.sql.DataSource;

public abstract class AbstractParameterizedQuery<T, P> extends AbstractQuery implements ParameterizedQuery<T, P>
{
    protected final DataSource ds;

    public AbstractParameterizedQuery( String sql, DataSource ds, String schema )
    {
        super( sql, schema );
        this.ds = ds;
    }
}
