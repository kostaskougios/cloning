package com.rits.perspectives;

import java.util.Collection;

import com.rits.cloning.Cloner;

/**
 * Perspectives: an object instance of a class behaving differently according to the "view angle".
 *  
 * @author kostantinos.kougios
 *
 * 30 Nov 2009
 */
public class Perspectives
{
	private final Cloner	cloner;

	public Perspectives(final Cloner cloner)
	{
		this.cloner = cloner;
	}

	/**
	 * Sample: if o is an instance of Product and c is OrderedProduct.class then this returns
	 * and instance of OrderedProduct.class which has equal field values to those of the instance of Product.
	 * In other words, the returned instance of OrderedProduct.class is the Product instance from the perspective
	 * of an OrderedProduct
	 * 
	 * View an object o from the perspective of class c. (view o as an instance of c). c must be instanceof o.getClass() 
	 * 
	 * @param <T>		the object
	 * @param <E>		this will be the returned type and it must be instanceof T. All properties of o will be copied to this instance.
	 * @param c			the class of E. This is used to generate new instances of c
	 * @param o			the object that must be viewed from a different perspective
	 * @return			the E perspective of o
	 */
	public <T, E extends T> E viewAs(final Class<E> c, final T o)
	{
		if (o == null) return null;
		if (o instanceof Collection<?>) throw new IllegalArgumentException("for collections please use viewCollectionAs() method. Invalid object " + o);
		final E newInstance = cloner.fastCloneOrNewInstance(c);
		cloner.copyPropertiesOfInheritedClass(o, newInstance);
		return newInstance;
	}

	/**
	 * Sample: if o is a [ Products extends LinkedList<Product> ] then the returned instance 
	 * is a [ OrderedProducts extends LinkedList<OrderedProduct> ].
	 * 
	 * View a collection o from the perspective of collection E.
	 * 
	 * NOTE: order of the items might not be preserved, depending on the collection type
	 * 
	 * @param <T>								the type of the collection o
	 * @param <I>								the type of the elements of the collection o
	 * @param <E>								the type of the perspective collection
	 * @param <NI>								the type of the perspective's elements
	 * @param newCollection			the collection to which the adapted instances should be added
	 * @param currentCollection			the collection with the instances to be adapted
	 * @param perspectiveCollectionItemClass	the class of the NI
	 * @return									E, the collection from a different perspective or null if currentCollection is null
	 */
	public <I, NI extends I, T extends Collection<I>, E extends Collection<NI>> E viewCollectionAs(final E newCollection, final Class<NI> perspectiveCollectionItemClass, final T currentCollection)
	{
		if (currentCollection == null) return null;
		for (final I item : currentCollection)
		{
			final NI newItem = viewAs(perspectiveCollectionItemClass, item);
			newCollection.add(newItem);
		}
		return newCollection;
	}
}
