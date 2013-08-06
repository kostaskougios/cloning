package com.rits.tests.cloning;

public enum TestEnum
{
	A("a"), B("b"), C("c");

	private String name;

	private TestEnum(String name)
	{
		this.name = name;
	}

	public static Object o = new Object();
}