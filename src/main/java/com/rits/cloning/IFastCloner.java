package com.rits.cloning;

import java.util.Map;

/**
 * @author kostantinos.kougios
 *         <p/>
 *         21 May 2009
 */
public interface IFastCloner {
    public Object clone(Object t, IDeepCloner cloner, Map<Object, Object> clones);
}
