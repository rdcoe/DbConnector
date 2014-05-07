package com.comdev.da.persist.query;

import java.sql.SQLException;

public interface InsertAndReturnKeys<P>
{
    Object[] buildParams( P records );

    Long[] insertAndReturnKeys( P record )
        throws SQLException;
}
