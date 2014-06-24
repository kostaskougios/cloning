package com.rits.cloning;

import java.util.Collection;
import java.util.Map;

/**
 * @author kostantinos.kougios
 *
 * 21 May 2009
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class FastClonerCustomCollection<T extends Collection> implements IFastCloner
{
	public abstract T getInstance(T o);

    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
		final T c = getInstance((T) t);
		final T l = (T) t;
		for (final Object o : l)
		{
            final Object clone = cloner.deepClone(o, clones);
            c.add(clone);
		}
		return c;
	}
}
