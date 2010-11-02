package com.rits.tests.perspectives.model;

import java.util.LinkedList;

/**
 * @author kostantinos.kougios
 *
 * 1 Dec 2009
 */
public class ProductsCollection<T extends Product> extends LinkedList<T>
{
	private static final long	serialVersionUID	= -5975494299750933209L;

	// sample extra method for this collection
	public Product findBySKU(final String sku)
	{
		for (final Product p : this)
		{
			if (p.getSku().equals(sku)) return p;
		}
		return null;
	}
}
