package com.rcoe.da.dbconnector.query;

public interface ParameterizedUpdate<T, P>
{
    boolean update( T record );
}
