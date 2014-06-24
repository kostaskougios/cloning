package com.rits.cloning;

import java.util.Map;

/**
 * @author kostas.kougios Date 24/06/14
 */
public interface IDeepCloner {
    <T> T deepClone(final T o, final Map<Object, Object> clones);
}
