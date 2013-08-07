package com.rits.tests.cloning;

public enum TestEnum
{
	A("a")
			{
				public String someMethod()
				{
					return "Some String from A";
				}
			},


	B("b")
			{
				public String someMethod()
				{
					return "Some String from B";
				}
			},

	C("c")
			{
				public String someMethod()
				{
					return "Some String from C";
				}
			};

	private final String name;

	private TestEnum(String name)
	{
		this.name = name;
	}

	public abstract String someMethod();

	public static Object o = new Object();
}