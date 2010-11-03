package com.rits.cloning;

import java.util.Collection;
import java.util.Map;

/**
 * @author kostantinos.kougios
 *
 * 21 May 2009
 */
@SuppressWarnings("unchecked")
public abstract class FastClonerCustomCollection<T extends Collection<Object>> implements IFastCloner
{
	public abstract T getInstance();

	@Override
	public Object clone(final Object t, final Cloner cloner, final Map<Object, Object> clones) throws IllegalAccessException
	{
		final T c = getInstance();
		final T l = (T) t;
		for (final Object o : l)
		{
			final Object cloneInternal = cloner.cloneInternal(o, clones);
			c.add(cloneInternal);
		}
		return c;
	}

}
