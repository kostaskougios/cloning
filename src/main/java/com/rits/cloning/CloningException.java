package com.rits.cloning;

/**
 * thrown if cloning fails
 * 
 * @author kostantinos.kougios
 *
 * 18 Jan 2009
 */
public class CloningException extends RuntimeException
{
	private static final long	serialVersionUID	= 3815175312001146867L;

	public CloningException(final String message, final Throwable cause)
	{
		super(message, cause);

	}

}
