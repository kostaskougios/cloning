package com.rits.cloning;

import java.util.*;

public class FastClonerSetOf12 implements IFastCloner {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
        Set set = (Set) t;
        Object[] a = set.toArray();
        if (set.size() == 1) {
            return Set.of(cloner.deepClone(a[0], clones));
        } else if (set.size() == 2) {
            Object o1 = cloner.deepClone(a[0], clones);
            Object o2 = cloner.deepClone(a[1], clones);
            return Set.of(o1, o2);
        } else {
            return new HashSet<>();
        }
    }
}
