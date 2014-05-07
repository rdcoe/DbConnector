package com.comdev.da.persist.query;

import java.sql.ResultSet;
import java.sql.SQLException;

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
@PrepareForTest( {SelectOneResultSetHandler.class} )
public class SelectOneResultSetHandlerTest
{
    private AbstractSelectQuery<Persistable, String> stubQuery;

    @Before
    public void setUp()
    {
        Identity identity = new IdentityImpl();
        identity.setName( "stubQuery" );

        DbConnectionFactory mockFactory = PowerMock.createMock( DbConnectionFactory.class );
        stubQuery = new SelectQueryStub( mockFactory, identity );
    }

    @Test
    public void testDTOReturnedWhenResultSetNotEmpty()
        throws SQLException
    {
        // setup data
        SelectOneResultSetHandler<Persistable, String> handler = new SelectOneResultSetHandler<Persistable, String>( stubQuery );

        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        // setup expectations
        EasyMock.expect( mockRs.next() ).andReturn( true );
        mockRs.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        // exercise
        Persistable actual = handler.handle( mockRs );

        // verify results
        PowerMock.verifyAll();

        Assert.assertNotNull( actual );
    }

    @Test
    public void testNullDTOWhenResultSetEmpty()
        throws SQLException
    {
        // setup data
        SelectOneResultSetHandler<Persistable, String> handler = new SelectOneResultSetHandler<Persistable, String>( stubQuery );

        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        // setup expectations
        EasyMock.expect( mockRs.next() ).andReturn( false );
        mockRs.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        // exercise
        Persistable actual = handler.handle( mockRs );

        // verify results
        PowerMock.verifyAll();

        Assert.assertNull( actual );
    }
}
