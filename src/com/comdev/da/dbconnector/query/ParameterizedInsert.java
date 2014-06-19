package com.comdev.da.dbconnector.query;

import java.sql.SQLException;

/**
 * The ParameterizedInsert type is used for classes that connect to a database and requires
 * parameters to be inserted into a prepared statement.
 * 
 * @author rcoe
 * 
 * @param <T>
 * @param <P>
 */
public interface ParameterizedInsert<T, P extends Object>
{
    /**
     * The insert method inserts the parms according to the query defined by the base class. It is
     * the responsibility of the caller to both close or rollback the transaction, depending on
     * success or failure.
     * 
     * @param parms
     * @return
     * @throws SQLException
     */
    T insert( P parms )
        throws SQLException;
}
