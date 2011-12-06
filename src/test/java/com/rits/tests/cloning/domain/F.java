package com.rits.tests.cloning.domain;

import com.rits.cloning.IFreezable;

/**
 * @author kostantinos.kougios
 *
 * 15 Nov 2010
 */
public class F implements IFreezable
{
	private boolean	frozen	= false;

	public void setFrozen(final boolean frozen)
	{
		this.frozen = frozen;
	}

	public boolean isFrozen()
	{
		return frozen;
	}
}
