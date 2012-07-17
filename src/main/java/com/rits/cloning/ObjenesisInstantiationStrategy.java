package com.rits.cloning;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * @author kostantinos.kougios
 *
 * 17 Jul 2012
 */
public class ObjenesisInstantiationStrategy implements IInstantiationStrategy
{
	private final Objenesis	objenesis	= new ObjenesisStd();

	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> c)
	{
		return (T) objenesis.newInstance(c);
	}

	private static ObjenesisInstantiationStrategy	instance	= new ObjenesisInstantiationStrategy();

	public static ObjenesisInstantiationStrategy getInstance()
	{
		return instance;
	}
}
