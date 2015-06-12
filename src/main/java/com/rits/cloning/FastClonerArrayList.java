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
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
		final ArrayList al = (ArrayList) t;
		final ArrayList l = new ArrayList(al.size());
		for (final Object o : al)
		{
        		final Object cloneInternal = cloner.deepClone(o, clones);
        		l.add(cloneInternal);
		}
		return l;
	}

}
