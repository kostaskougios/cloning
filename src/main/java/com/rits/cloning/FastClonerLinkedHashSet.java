package com.rits.cloning;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Fast Cloner for LinkedHashSet
 * 
 * @author Tobias Weimer
 */
public class FastClonerLinkedHashSet implements IFastCloner
{
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
		final LinkedHashSet<?> al = (LinkedHashSet) t;
		final LinkedHashSet l = new LinkedHashSet();
		for (final Object o : al)
		{
			l.add(cloner.deepClone(o, clones));
		}
		return l;
	}
}
