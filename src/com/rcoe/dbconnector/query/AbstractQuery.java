package com.rcoe.dbconnector.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractQuery
{
    protected static final String SCHEMA_PARAMETER = "{schema}";
    protected String query;

    private final Pattern SCHEMA_REPLACEMENT_PATTERN = Pattern.compile( "\\{schema\\}" );

    public AbstractQuery( String sql, String schema )
    {
        Matcher matcher = SCHEMA_REPLACEMENT_PATTERN.matcher( sql );
        if( matcher.find() ) {
            query = matcher.replaceAll( schema );
        } else {
            query = sql;
        }
    }
}
