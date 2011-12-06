package com.rits.cloning;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author kostantinos.kougios
 *
 * 21 May 2009
 */
public class FastClonerArrayList implements IFastCloner
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object clone(final Object t, final Cloner cloner, final Map<Object, Object> clones) throws IllegalAccessException
	{
		final ArrayList al = (ArrayList) t;
		final ArrayList l = new ArrayList();
		for (final Object o : al)
		{
			final Object cloneInternal = cloner.cloneInternal(o, clones);
			l.add(cloneInternal);
		}
		return l;
	}

}
