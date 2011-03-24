package com.rits.cloning;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author kostantinos.kougios
 *
 * 24 Mar 2011
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Immutable
{
	boolean subClass() default false;
}
