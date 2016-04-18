package com.rcoe.dbconnector.common;

import java.io.Serializable;

public interface Persistable extends Serializable
{
    long getId();
    
    void setId( long uid );
}
