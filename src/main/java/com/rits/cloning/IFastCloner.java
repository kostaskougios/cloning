package com.rits.cloning;

import java.util.Map;

/**
 * allows a custom cloner to be created for a specific class.
 * (it has to be registered with Cloner)
 *
 * @author kostantinos.kougios
 * 21 May 2009
 */
public interface IFastCloner {
    public Object clone(Object t, IDeepCloner cloner, Map<Object, Object> clones);
}
