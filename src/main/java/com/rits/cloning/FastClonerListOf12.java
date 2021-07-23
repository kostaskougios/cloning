package com.rits.cloning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FastClonerListOf12 implements IFastCloner {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
        List al = (List) t;
        if (al.size()==1){
            return List.of(al.get(0));
        }else if (al.size()==2){
            return List.of(al.get(0),al.get(1));
        }else {
            return new ArrayList<>();
        }
    }

}
