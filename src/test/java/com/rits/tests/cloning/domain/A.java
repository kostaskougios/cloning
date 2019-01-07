package com.rits.tests.cloning.domain;

import java.util.Objects;

/**
 * @author kostantinos.kougios
 *
 * 30 Nov 2009
 */
public class A
{
	private int		x		= 5;
	private String	name	= "kostas";

	public int getX()
	{
		return x;
	}

	public void setX(final int x)
	{
		this.x = x;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		A a = (A) o;
		return x == a.x &&
				Objects.equals(name, a.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, name);
	}
}
