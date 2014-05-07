package com.comdev.da.persist.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.comdev.da.auth.Identity;
import com.comdev.da.auth.IdentityImpl;
import com.comdev.da.persist.DbConnectionFactory;
import com.comdev.da.persist.dto.Persistable;
import com.comdev.da.tests.stubs.SelectQueryStub;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {SelectManyResultSetHandler.class} )
public class SelectManyResultSetHandlerTest
{
    private AbstractSelectQuery<Persistable, String> stubQuery;

    @Before
    public void setUp()
    {
        DbConnectionFactory mockFactory = PowerMock.createMock( DbConnectionFactory.class );
        Identity identity = new IdentityImpl();
        identity.setName( "stubQuery" );

        stubQuery = new SelectQueryStub( mockFactory, identity );
    }

    @Test
    public void testListReturnedWhenResultSetNotEmpty()
        throws SQLException
    {
        // setup data
        SelectManyResultSetHandler<Persistable, String> handler = new SelectManyResultSetHandler<Persistable, String>( stubQuery );

        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        // setup expectations
        EasyMock.expect( mockRs.next() ).andReturn( true ).times( 2 );
        EasyMock.expect( mockRs.next() ).andReturn( false );
        mockRs.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        // exercise
        List<Persistable> actual = handler.handle( mockRs );

        // verify results
        PowerMock.verifyAll();

        Assert.assertTrue( actual.size() == 2 );
        Assert.assertTrue( actual.get( 0 ).getUid() == 1 );
        Assert.assertTrue( actual.get( 1 ).getUid() == 2 );
    }

    @Test
    public void testListIsEmptyWhenResultSetEmpty()
        throws SQLException
    {
        // setup data
        SelectManyResultSetHandler<Persistable, String> handler = new SelectManyResultSetHandler<Persistable, String>( stubQuery );

        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        // setup expectations
        EasyMock.expect( mockRs.next() ).andReturn( false );
        mockRs.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        // exercise
        List<Persistable> actual = handler.handle( mockRs );

        // verify results
        PowerMock.verifyAll();

        Assert.assertTrue( actual.size() == 0 );
    }
}
