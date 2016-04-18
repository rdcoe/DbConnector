package com.rcoe.dbconnector.auth;

import java.io.Serializable;
import java.util.List;

/**
 * The Identity type is a wrapper that identifies a user.
 * 
 * @author rcoe
 * 
 */
public interface Identity extends Serializable
{
    public String getId();
    
    public String getName();

    public void setName( String name );

    public String getSidString();

    public void setSidString( String sidString );

    public List<String> getGroups();

    public void addGroup( String group );
}
