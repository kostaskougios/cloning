package com.rits.cloning;

import java.util.GregorianCalendar;
import java.util.Map;

/**
 * @author kostantinos.kougios
 *
 * 21 May 2009
 */
public class FastClonerCalendar implements IFastCloner
{
	public Object clone(final Object t, final Cloner cloner, final Map<Object, Object> clones)
	{
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(((GregorianCalendar) t).getTimeInMillis());
		return gc;
	}
}
