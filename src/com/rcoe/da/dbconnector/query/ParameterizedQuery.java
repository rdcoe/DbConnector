package com.rcoe.da.dbconnector.query;

public interface ParameterizedQuery<T, P>
{
    P[][] generateParms( T[] keys );

    boolean execute( T key );
    
    boolean execute( T[] keys );
}
