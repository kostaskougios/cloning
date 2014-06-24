package com.rits.cloning;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kostantinos.kougios
 *
 * 21 May 2009
 */
public class FastClonerHashMap implements IFastCloner
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
		final HashMap<Object, Object> m = (HashMap) t;
		final HashMap result = new HashMap();
		for (final Map.Entry e : m.entrySet())
		{
            final Object key = cloner.deepClone(e.getKey(), clones);
            final Object value = cloner.deepClone(e.getValue(), clones);

			result.put(key, value);
		}
		return result;
	}
}
