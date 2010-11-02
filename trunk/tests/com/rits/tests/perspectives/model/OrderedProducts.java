package com.rits.tests.perspectives.model;

/**
 * @author kostantinos.kougios
 *
 * 1 Dec 2009
 */
public class OrderedProducts extends ProductsCollection<OrderedProduct>
{
	private static final long	serialVersionUID	= 1483417907388671141L;

	public OrderedProducts(final OrderedProduct... ops)
	{
		super();
		for (final OrderedProduct p : ops)
		{
			add(p);
		}
	}

	// extra behaviour/properties for the ordered products collection can go here
	public OrderedProducts getCheapOnes()
	{
		final OrderedProducts ops = new OrderedProducts();
		for (final OrderedProduct p : this)
		{
			if (p.getPrice().doubleValue() < 10d)
			{
				ops.add(p);
			}
		}
		return ops;
	}
}
