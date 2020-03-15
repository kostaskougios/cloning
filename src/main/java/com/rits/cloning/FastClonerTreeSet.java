package com.rits.cloning;

import com.rits.cloning.IFastCloner;

import java.util.Map;
import java.util.TreeSet;

public class FastClonerTreeSet implements IFastCloner {
	@Override
	@SuppressWarnings("unchecked")
	public Object clone(Object t, IDeepCloner cloner, Map<Object, Object> clones) {
		TreeSet<?> treeSet = (TreeSet<?>) t;
		TreeSet result = new TreeSet(treeSet.comparator());
		for (Object o : treeSet) {
			result.add(cloner.deepClone(o, clones));
		}
		return result;
	}
}
