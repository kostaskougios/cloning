package com.rits.cloning;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fast Cloner for EnumMaps
 * 
 * @author Tobias Weimer
 */
public class FastClonerEnumMap implements IFastCloner
{
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object clone(final Object toBeCloned, final IDeepCloner cloner, final Map<Object, Object> clones) {
		final EnumMap<? extends Enum<?>, ?> m = (EnumMap) toBeCloned;
		
		// make a shallow copy of the original EnumMap
		// since there is no no-arg-constructor
		final EnumMap result = new EnumMap(m);
		
		// Now clone the values
		for (final Map.Entry<? extends Enum<?>, ?> e : m.entrySet()) {
			// No need to clone the key, since it is an Enum
			// However, the value MUST be cloned
			result.put(e.getKey(), cloner.deepClone(e.getValue(), clones));
		}
		return result;
	}
}
