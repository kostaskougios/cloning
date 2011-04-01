package com.rits.tests.cloning.domain;

/**
 * @author kostantinos.kougios
 *
 * 1 Apr 2011
 */
public class G
{
	private final A	a;
	private final B	b;

	public G(final A a, final B b)
	{
		this.a = a;
		this.b = b;
	}

	public A getA()
	{
		return a;
	}

	public B getB()
	{
		return b;
	}

}
