package com.rits.tests.cloning;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

import com.rits.cloning.Cloner;
import com.rits.cloning.Immutable;
import com.rits.tests.cloning.TestCloner.SynthOuter.Inner;
import com.rits.tests.cloning.domain.A;
import com.rits.tests.cloning.domain.B;
import com.rits.tests.cloning.domain.F;
import com.rits.tests.cloning.domain.G;

/**
 * @author kostantinos.kougios
 *
 * 18 Sep 2008
 */
public class TestCloner extends TestCase
{
	private final Cloner	cloner	= new Cloner();
	{
		cloner.setDumpClonedClasses(false);
	}

	class X
	{
		private X(int x)
		{
			x = 5;
		}
	}

	@Immutable(subClass = true)
	static public class ATestImmutable
	{
	}

	static public class ATestImmutableSubclass extends ATestImmutable
	{

	}

	@Immutable
	static public class BTestImmutable
	{
	}

	static public class BTestImmutableSubclass extends BTestImmutable
	{
	}

	public void testImmutableSubclassNotEnabled()
	{
		final BTestImmutableSubclass a = new BTestImmutableSubclass();
		final BTestImmutableSubclass ca = cloner.deepClone(a);
		assertNotSame(a, ca);
	}

	public void testImmutableSubclass()
	{
		final ATestImmutableSubclass a = new ATestImmutableSubclass();
		final ATestImmutable ca = cloner.deepClone(a);
		assertSame(a, ca);
	}

	public void testImmutable()
	{
		final ATestImmutable a = new ATestImmutable();
		final ATestImmutable ca = cloner.deepClone(a);
		assertSame(a, ca);
	}

	/**
	 * tests if it happens that in the deep-graph of the cloned objects,
	 * if a reference to the same object exists twice, the cloned object 
	 * will have only 1 clone and references to this clone.
	 */
	public void testCloningOfSameObject()
	{
		final Object o1 = new Object();
		final Object o2 = new Object();
		class OO
		{
			Object	o1, o2, o3, o4;
		}
		final OO oo = new OO();
		oo.o1 = o1;
		oo.o2 = o2;
		oo.o3 = o1;
		oo.o4 = o2;
		OO clone = cloner.deepClone(oo);
		assertTrue(clone.o1 == clone.o3);
		assertTrue(clone.o2 == clone.o4);

		final HashSet<Object> h1 = new HashSet<Object>();
		final HashSet<Object> h2 = new HashSet<Object>();
		oo.o1 = h1;
		oo.o2 = h2;
		oo.o3 = h1;
		oo.o4 = h2;
		clone = cloner.deepClone(oo);
		assertTrue(clone.o1 == clone.o3);
		assertTrue(clone.o2 == clone.o4);
		assertTrue(clone.o1 != clone.o2);
		assertTrue(clone.o2 != clone.o3);
	}

	/**
	 * tests if immutable clone is the same instance
	 */
	public void testCloneImmutables()
	{
		final String s = "test1";
		final String clone1 = cloner.deepClone(s);
		assertSame(s, clone1);
	}

	/**
	 * tests if immutable clone is the same instance
	 */
	public void testCloneFloat()
	{
		final Float float1 = new Float(8);
		final Float cloned = cloner.deepClone(float1);
		assertSame(float1, cloned);
		assertEquals(float1, cloned);
	}

	/**
	 * tests if arrays are cloned correctly
	 */
	public void testCloneArrays()
	{
		final int[] ia = { 1, 2, 3 };
		final int[] cloned = cloner.deepClone(ia);
		assertEquals(ia.length, cloned.length);
		for (int i = 0; i < ia.length; i++)
		{
			assertEquals(ia[i], cloned[i]);
		}
		final double[] da = { 1, 2, 3 };
		final double[] dcloned = cloner.deepClone(da);
		assertEquals(da.length, dcloned.length);
		for (int i = 0; i < ia.length; i++)
		{
			assertEquals(da[i], dcloned[i]);
		}
	}

	private class Simple
	{
		private int		x	= 1;
		private String	s	= "simple";
		private Complex	complex;

		public Complex getComplex()
		{
			return complex;
		}

		public void setComplex(final Complex complex)
		{
			this.complex = complex;
		}

		public int getX()
		{
			return x;
		}

		public void setX(final int x)
		{
			this.x = x;
		}

		public String getS()
		{
			return s;
		}

		public void setS(final String s)
		{
			this.s = s;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (obj instanceof Simple)
			{
				final Simple s = (Simple) obj;
				return s.getS().equals(getS()) && s.getX() == getX();
			}
			return super.equals(obj);
		}
	}

	/**
	 * tests cloning of a simple class
	 */
	public void testCloneSimple()
	{
		final Simple simple = new Simple();
		simple.setS("x1");
		simple.setX(20);
		final Simple clone = cloner.deepClone(simple);
		assertEquals(simple.getS(), clone.getS());
		assertSame(simple.getS(), clone.getS());
		assertEquals(simple.getX(), clone.getX());
		simple.setS("x2");
		simple.setX(30);
		assertNotSame(simple.getS(), clone.getS());
		assertFalse(simple.getS().equals(clone.getS()));
		assertFalse(simple.getX() == clone.getX());
	}

	protected class Complex
	{
		private int					x	= 1;
		private String				s	= "complex";
		private final List<Simple>	l	= new ArrayList<Simple>();

		public Complex()
		{
			l.add(new Simple());
			final Simple simple = new Simple();
			simple.setS("s2");
			simple.setX(30);
			l.add(simple);
			simple.setComplex(this);
		}

		public int getX()
		{
			return x;
		}

		public void setX(final int x)
		{
			this.x = x;
		}

		public String getS()
		{
			return s;
		}

		public List<Simple> getL()
		{
			return l;
		}

		public void setS(final String s)
		{
			this.s = s;
		}

	}

	/**
	 * test cloning of a complex object graph
	 */
	public void testCloneComplex()
	{
		final Complex complex = new Complex();
		complex.setS("x1");
		complex.setX(20);
		final Complex clone = cloner.deepClone(complex);
		assertEquals(complex.getS(), clone.getS());
		assertEquals(complex.getX(), clone.getX());
		assertEquals(complex.getL().size(), clone.getL().size());
		final Simple simple1 = complex.getL().get(0);
		final Simple simple2 = complex.getL().get(1);
		final Simple simple1Clone = clone.getL().get(0);
		final Simple simple2Clone = clone.getL().get(1);
		assertNotSame(simple1, simple1Clone);
		assertNotSame(simple2, simple2Clone);
		assertEquals(simple1, simple1Clone);
		assertEquals(simple2, simple2Clone);
	}

	public void testShallowClone()
	{
		final Simple simple1 = new Simple();
		final Complex complex = new Complex();
		simple1.setComplex(complex);
		simple1.setX(5);
		simple1.setS("test");

		final Simple shallowClone = cloner.shallowClone(simple1);
		assertNotSame(simple1, shallowClone);
		assertSame(simple1.getComplex(), shallowClone.getComplex());
		assertEquals(simple1.getX(), shallowClone.getX());
		assertEquals(simple1.getS(), shallowClone.getS());
		shallowClone.setX(10);
		assertTrue(shallowClone.getX() != simple1.getX());
		shallowClone.setS("x");
		assertTrue(shallowClone.getS() != simple1.getS());
	}

	public void testCloneStack()
	{
		final List<Integer> lst = new LinkedList<Integer>();
		for (int i = 0; i < 100000; i++)
		{
			lst.add(i);
		}
		final List<Integer> clone = cloner.deepClone(lst);
		assertEquals(lst.size(), clone.size());
	}

	public void testCloneTreeSet()
	{
		final TreeSet<DC> set = new TreeSet<DC>();
		final DC dc1 = new DC(5);
		set.add(dc1);
		final DC dc2 = new DC(10);
		set.add(dc2);

		assertTrue(set.contains(dc1));
		assertTrue(set.contains(dc2));

		assertTrue(set.remove(dc1));
		set.add(dc1);

		//		cloner.setDumpClonedClasses(true);

		final TreeSet<DC> set2 = cloner.deepClone(set);

		assertTrue(set2.contains(dc1));
		assertTrue(set2.contains(dc2));
		assertTrue(set2.remove(dc1));
		assertEquals(1, set2.size());
	}

	public void testCloneHashSet()
	{
		Set<DC> set = new HashSet<DC>();
		final DC dc1 = new DC(5);
		set.add(dc1);
		final DC dc2 = new DC(10);
		set.add(dc2);

		assertTrue(set.contains(dc1));
		assertTrue(set.contains(dc2));

		assertTrue(set.remove(dc1));
		set.add(dc1);

		//		cloner.setDumpClonedClasses(true);

		set = cloner.deepClone(set);

		assertTrue(set.contains(dc1));
		assertTrue(set.contains(dc2));
		assertTrue(set.remove(dc1));
		assertEquals(1, set.size());
	}

	public void testCloneStability()
	{
		for (int i = 0; i < 10; i++)
		{
			final Complex complex = new Complex();
			complex.setS("x1");
			complex.setX(20);
			final ArrayList<Object> l = new ArrayList<Object>();
			l.add(complex);
			final HashSet<Object> h1 = new HashSet<Object>();
			final HashSet<Object> h2 = new HashSet<Object>();
			for (int j = 0; j < 100; j++)
			{
				h1.add(j);
				h2.add("string" + j);
				h1.add(Calendar.getInstance());
				h2.add(new Date());
				l.add(new Random());
			}

			l.add(h1);
			l.add(h2);
			final Complex clone = cloner.deepClone(complex);
			l.add(clone);
			cloner.deepClone(l);
		}
	}

	public void testArrayListCloning()
	{
		final ArrayList<Object> l = new ArrayList<Object>();
		l.add(Calendar.getInstance());
		l.add(2);
		l.add(3);
		l.add("kostas");

		final ArrayList<Object> cloned = cloner.deepClone(l);
		assertEquals(l.size(), cloned.size());
		for (int i = 0; i < l.size(); i++)
		{
			assertEquals(l.get(i), cloned.get(i));
		}
		assertNotSame(l, cloned);
		assertNotSame(l.get(0), cloned.get(0));
		assertSame(l.get(1), cloned.get(1));

		l.add(5);
		assertEquals(4, cloned.size());
		cloned.add(8);
		assertEquals(5, l.size());
	}

	public void testLinkedListCloning()
	{
		final LinkedList<Object> l = new LinkedList<Object>();
		l.add(Calendar.getInstance());
		l.add(2);
		l.add(3);
		l.add("kostas");

		final LinkedList<Object> cloned = cloner.deepClone(l);
		assertEquals(l.size(), cloned.size());
		for (int i = 0; i < l.size(); i++)
		{
			assertEquals(l.get(i), cloned.get(i));
		}
		assertNotSame(l, cloned);
		assertNotSame(l.get(0), cloned.get(0));
		assertSame(l.get(1), cloned.get(1));

		l.add(5);
		assertEquals(4, cloned.size());
		cloned.add(8);
		assertEquals(5, l.size());
	}

	public void testHashSetCloning()
	{
		final HashSet<Object> l = new HashSet<Object>();
		l.add(Calendar.getInstance());
		l.add(2);
		l.add(3);
		l.add("kostas");

		final HashSet<Object> cloned = cloner.deepClone(l);
		assertNotSame(l, cloned);
		assertEquals(l.size(), cloned.size());
		for (final Object o : l)
		{
			assertTrue(cloned.contains(o));
		}
	}

	public void testHashMapCloning()
	{
		final HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("kostas", Calendar.getInstance());
		m.put("tina", 500);
		m.put("george", "Ah!");
		final HashMap<String, Object> cloned = cloner.deepClone(m);
		assertEquals(m.size(), cloned.size());
		for (final Map.Entry<String, Object> e : m.entrySet())
		{
			assertEquals(e.getValue(), cloned.get(e.getKey()));
		}
		assertNotSame(m, cloned);
		assertNotSame(m.get("kostas"), cloned.get("kostas"));
		assertSame(m.get("tina"), cloned.get("tina"));
		cloned.put("x", 100);
		assertEquals(3, m.size());
		assertEquals(4, cloned.size());
	}

	public void testTreeMapCloning()
	{
		final TreeMap<String, Object> m = new TreeMap<String, Object>();
		m.put("kostas", Calendar.getInstance());
		m.put("tina", 500);
		m.put("george", "Ah!");
		final TreeMap<String, Object> cloned = cloner.deepClone(m);
		assertEquals(m.size(), cloned.size());
		for (final Map.Entry<String, Object> e : m.entrySet())
		{
			assertEquals(e.getValue(), cloned.get(e.getKey()));
		}
		assertNotSame(m, cloned);
		assertNotSame(m.get("kostas"), cloned.get("kostas"));
		assertSame(m.get("tina"), cloned.get("tina"));
		cloned.put("x", 100);
		assertEquals(3, m.size());
		assertEquals(4, cloned.size());
	}

	public void testTransientNullPositive()
	{
		final Cloner c = new Cloner();
		c.setNullTransient(true);
		final TransientTest tt = new TransientTest();
		final TransientTest deepClone = c.deepClone(tt);
		assertNull(deepClone.tr1);
		assertNull(deepClone.a);
		assertEquals(0, deepClone.i);
		assertNotNull(deepClone.nontr);
	}

	public void testTransientNullNegative()
	{
		final Cloner c = new Cloner();
		c.setNullTransient(false);
		final TransientTest tt = new TransientTest();
		final TransientTest deepClone = c.deepClone(tt);
		assertNotNull(deepClone.tr1);
		assertNotNull(deepClone.a);
		assertNotNull(deepClone.nontr);
	}

	public void testCopyPropertiesArrayPrimitive()
	{
		final int[] src = new int[] { 5, 6, 7 };
		final int[] dest = new int[3];
		cloner.copyPropertiesOfInheritedClass(src, dest);
		assertEquals(src[0], dest[0]);
		assertEquals(src[1], dest[1]);
		assertEquals(src[2], dest[2]);
	}

	public void testCopyPropertiesArray()
	{
		final Object[] src = new Object[] { new Integer(5), new Float(8.5f), new Double(3.5d) };
		final Object[] dest = new Object[3];
		cloner.copyPropertiesOfInheritedClass(src, dest);
		assertEquals(src[0], dest[0]);
		assertEquals(src[1], dest[1]);
		assertEquals(src[2], dest[2]);
	}

	public void testCopyPropertiesInheritedClasses()
	{
		final A a = new A();
		final B b = new B();
		b.setName("x");
		b.setX(-1);
		b.setY(10);
		cloner.copyPropertiesOfInheritedClass(a, b);

		assertEquals("kostas", b.getName());
		assertEquals(5, b.getX());
		assertEquals(10, b.getY());
	}

	public void testFreezable()
	{
		final F f = new F();
		assertNotSame(f, cloner.deepClone(f));
		f.setFrozen(true);
		assertSame(f, cloner.deepClone(f));
	}

	public void testDeepCloneDontCloneInstances()
	{
		final A a = new A();
		final B b = new B();
		final G g = new G(a, b);
		final G cga = cloner.deepCloneDontCloneInstances(g, a);
		assertNotSame(g, cga);
		assertNotSame(cga.getB(), b);
		assertSame(cga.getA(), a);

		final G cgab = cloner.deepCloneDontCloneInstances(g, a, b);
		assertNotSame(g, cgab);
		assertSame(cgab.getB(), b);
		assertSame(cgab.getA(), a);
	}

	static class SynthOuter
	{
		public Inner getInner()
		{
			return new Inner();
		}

		class Inner
		{
			Object	x	= new Object();

			public SynthOuter getOuter()
			{
				return SynthOuter.this;
			}
		}
	}

	public void testDontCloneSynthetic()
	{
		final Cloner cloner = new Cloner();
		cloner.setCloneSynthetics(false);
		final SynthOuter outer = new SynthOuter();
		final Inner inner = outer.getInner();
		final Inner clonedInner = cloner.deepClone(inner);
		assertNotSame(inner, clonedInner);
		assertNotSame(inner.x, clonedInner.x);
		assertSame(outer, clonedInner.getOuter());
	}

	public void testTreeMapWithComparator()
	{
		final TreeMap<Object, String> m = new TreeMap<Object, String>(new Comparator<Object>()
		{
			public int compare(final Object o1, final Object o2)
			{
				return o1.hashCode() - o2.hashCode();
			}
		});
		m.put(new Object()
		{
			@Override
			public int hashCode()
			{
				return 1;
			}
		}, "1");
		m.put(new Object()
		{
			@Override
			public int hashCode()
			{
				return 2;
			}
		}, "2");

		final TreeMap<Object, String> clone = cloner.deepClone(m);
		assertEquals(m, clone);
	}

	public void testTreeSetWithComparator()
	{
		final TreeSet<Object> set = new TreeSet<Object>(new Comparator<Object>()
		{
			public int compare(final Object o1, final Object o2)
			{
				return o1.hashCode() - o2.hashCode();
			}
		});

		set.add(new Object()
		{
			@Override
			public int hashCode()
			{
				return 1;
			}
		});
		set.add(new Object()
		{
			@Override
			public int hashCode()
			{
				return 2;
			}
		});

		final TreeSet<Object> clone = cloner.deepClone(set);
		assertEquals(set, clone);
	}
}
