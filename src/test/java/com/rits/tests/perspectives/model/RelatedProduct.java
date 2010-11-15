package com.rits.tests.perspectives.model;

/**
 * @author kostantinos.kougios
 *
 * 15 Nov 2010
 */
public class RelatedProduct extends Product
{
	private Product	related;

	public RelatedProduct(final int id, final String sku, final String title)
	{
		super(id, sku, title);
	}

	public Product getRelated()
	{
		return related;
	}

	public void setRelated(final Product related)
	{
		this.related = related;
	}
}
