package com.rits.tests.perspectives.model;

import java.math.BigDecimal;

/**
 * @author kostantinos.kougios
 *
 * 30 Nov 2009
 */
public class OrderedProduct extends Product
{
	private BigDecimal	price;
	private int			qty;

	public OrderedProduct(final int id, final String sku, final String title, final BigDecimal price, final int qty)
	{
		super(id, sku, title);
		this.price = price;
		this.qty = qty;
	}

	public int getQty()
	{
		return qty;
	}

	public void setQty(final int qty)
	{
		this.qty = qty;
	}

	public BigDecimal getPrice()
	{
		return price;
	}

	public void setPrice(final BigDecimal price)
	{
		this.price = price;
	}

	@Override
	public String toString()
	{
		return "ordered product:" + getSku() + ":qty=" + qty;
	}
}
