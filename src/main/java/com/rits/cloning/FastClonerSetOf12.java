package com.rits.cloning;

import java.util.*;

public class FastClonerSetOf12 implements IFastCloner{
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
        Set set=(Set) t;
        Object[] a=set.toArray();
        if (set.size()==1){
            return Set.of(a[0]);
        }else if (set.size()==2){
            return Set.of(a[0],a[1]);
        }else {
            return new HashSet<>();
        }
    }
}
