package com.comdev.da.dbconnector.dto;

import java.io.Serializable;

public interface Persistable extends Serializable
{
    long getUid();
    
    void setUid( long uid );
}
