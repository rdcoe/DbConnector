package com.rcoe.dbconnector.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IdentityImpl implements Identity, Serializable
{
    private static final long serialVersionUID = -502309945043190147L;

    private final String id;
    private String name;
    private String sidString;
    private List<String> groups = new ArrayList<String>();

    public IdentityImpl()
    {
        this( UUID.randomUUID().toString() );
    }
    
    public IdentityImpl( String id )
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }
    
    @Override
    public String getName()
    {
        return ( name == null ? id : name );
    }

    @Override
    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String getSidString()
    {
        return sidString;
    }

    @Override
    public void setSidString( String sidString )
    {
        this.sidString = sidString;
    }

    @Override
    public List<String> getGroups()
    {
        return groups;
    }

    @Override
    public void addGroup( String group )
    {
        this.groups.add( group );
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null ) {
            return false;
        }

        if( obj == this ) {
            return true;
        }

        if( obj instanceof IdentityImpl ) {
            IdentityImpl ident = (IdentityImpl)obj;
            return ident.id.equals( this.id );
        } else {
            return false;
        }
    }
}
