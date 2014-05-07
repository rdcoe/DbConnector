package com.comdev.da.persist.query;

public interface ParameterizedUpdate<T, P>
{
    boolean update( T record );
}
