package com.rits.cloning;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author kostantinos.kougios
 *
 * 21 May 2009
 */
public class FastClonerTreeMap implements IFastCloner
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object clone(final Object t, final Cloner cloner, final Map<Object, Object> clones) throws IllegalAccessException
	{
		final TreeMap<Object, Object> m = (TreeMap) t;
		final TreeMap result = new TreeMap(m.comparator());
		for (final Map.Entry e : m.entrySet())
		{
			final Object key = cloner.cloneInternal(e.getKey(), clones);
			final Object value = cloner.cloneInternal(e.getValue(), clones);
			result.put(key, value);
		}
		return result;
	}
}
