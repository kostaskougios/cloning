package com.rits.cloning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FastClonerListOf12 implements IFastCloner {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
        List al = (List) t;
        if (al.size() == 1) {
            return List.of(cloner.deepClone(al.get(0), clones));
        } else if (al.size() == 2) {
            Object o1 = cloner.deepClone(al.get(0), clones);
            Object o2 = cloner.deepClone(al.get(1), clones);
            return List.of(o1, o2);
        } else {
            return new ArrayList<>();
        }
    }

}
