package com.rcoe.da.dbconnector.tests.stubs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.rcoe.da.auth.Identity;
import com.rcoe.da.common.Persistable;
import com.rcoe.da.dbconnector.DbConnectionFactory;
import com.rcoe.da.dbconnector.query.AbstractSelectQuery;

public class SelectQueryStub extends AbstractSelectQuery<Persistable, String>
{
    private final static String SQL;
    private int i = 0;
    
    static {
        SQL = "SELECT * FROM X";
    }

    public SelectQueryStub( DbConnectionFactory dbFactory, Identity identity )
    {
        super( dbFactory, SQL, null, identity );
    }

    @Override
    public void fillDTO( ResultSet rs )
        throws SQLException
    {
        dto = new Persistable() {
            private static final long serialVersionUID = 1L;

            @Override
            public long getId()
            {
                return ++i;
            }

            @Override
            public void setId( long uid )
            {

            }
        };
    }

    @Override
    public Persistable selectOne()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Persistable> selectAll()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
