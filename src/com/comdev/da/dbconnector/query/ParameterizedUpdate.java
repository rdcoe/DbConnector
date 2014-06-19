package com.comdev.da.dbconnector.query;

public interface ParameterizedUpdate<T, P>
{
    boolean update( T record );
}
