package com.rcoe.dbconnector.query;

public interface ParameterizedUpdate<T, P>
{
    boolean update( T record );
}
