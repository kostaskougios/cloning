package com.rits.tests.cloning;

/**
 * @author kostantinos.kougios
 *
 * 7 Apr 2009
 */
public class DC implements Comparable<DC>
{
	public int	id	= -1;

	public DC(final int id)
	{
		this.id = id;
	}

	public int compareTo(final DC o)
	{
		return id - o.id;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(final Object obj)
	{
		return ((DC) obj).id == id;
	}

	@Override
	public String toString()
	{
		return "DC: id:" + id;
	}
}
