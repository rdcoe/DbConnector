package com.comdev.da.dbconnector;

public class DbConnectionFactoryException extends Exception
{
    private static final long serialVersionUID = -5315753934758234633L;

    public DbConnectionFactoryException()
    {
        super();
    }

    public DbConnectionFactoryException( String message,
                                         Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }

    public DbConnectionFactoryException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public DbConnectionFactoryException( String message )
    {
        super( message );
    }

    public DbConnectionFactoryException( Throwable cause )
    {
        super( cause );
    }
}
