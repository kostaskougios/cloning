package com.rits.tests.cloning;

import com.rits.cloning.Cloner;
import com.rits.cloning.FastClonerHashMap;
import com.rits.cloning.Immutable;
import com.rits.tests.cloning.TestCloner.SynthOuter.Inner;
import com.rits.tests.cloning.domain.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author kostantinos.kougios
 * <p>
 * 18 Sep 2008
 */
public class TestCloner {
    private final Cloner cloner = new Cloner();

    {
        cloner.setDumpClonedClasses(false);
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface MyImmutable {

    }

    @MyImmutable
    static private class MyAX {
    }

    @Test
    public void testCloneListOf12() {
        List<Integer> list1 = List.of(1);
        assertEquals(list1, cloner.deepClone(list1));
        assertEquals(1, cloner.deepClone(list1).size());
        List<Integer> list2 = List.of(1, 2);
        assertEquals(list2, cloner.deepClone(list2));
        assertEquals(2, cloner.deepClone(list2).size());
    }

    @Test
    public void testCloneSetOf12() {
        Set<Integer> set1 = Set.of(1);
        assertEquals(set1, cloner.deepClone(set1));
        assertEquals(1, cloner.deepClone(set1).size());
        Set<Integer> set2 = Set.of(1, 2);
        assertEquals(set2, cloner.deepClone(set2));
        assertEquals(2, cloner.deepClone(set2).size());
    }

    @Test
    public void testCalendarTimezone() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        Calendar c = Calendar.getInstance(timeZone);
        Calendar cloned = cloner.deepClone(c);
        assertEquals(timeZone, cloned.getTimeZone());
    }

    @Test
    public void testCloneEnumInMapIssue20() {
        Map<Integer, TestEnum> m = new HashMap<>();
        m.put(1, TestEnum.A);
        m.put(2, TestEnum.B);
        m.put(3, TestEnum.C);
        Map<Integer, TestEnum> clone = cloner.deepClone(m);

        assertSame(clone.get(1), TestEnum.A);
        assertSame(clone.get(2), TestEnum.B);
        assertSame(clone.get(3), TestEnum.C);
    }

    @Test
    public void testCustomAnnotation() {
        final Cloner cloner = new Cloner() {
            @Override
            protected Class<?> getImmutableAnnotation() {
                return MyImmutable.class;
            }
        };
        final MyAX o = new MyAX();
        final MyAX c = cloner.deepClone(o);
        assertSame(o, c);
    }

    @Test
    public void testConsiderImmutable() {
        final Cloner cloner = new Cloner() {
            @Override
            protected boolean considerImmutable(final Class<?> clz) {
                return clz == Object.class;
            }
        };
        final Object o = new Object();
        final Object c = cloner.deepClone(o);
        assertSame(o, c);
    }

    class X {
        private X(int x) {
            x = 5;
        }
    }

    @Immutable(subClass = true)
    static public class ATestImmutable {
    }

    static public class ATestImmutableSubclass extends ATestImmutable {

    }

    @Immutable
    static public class BTestImmutable {
    }

    static public class BTestImmutableSubclass extends BTestImmutable {
    }

    @Test
    public void testIssue7() {
        final HashMap<Object, Object> source = new HashMap<>();
        source.put("string", "string");
        source.put("array", new Integer[]{1, 2, 3});
        final HashMap<Object, Object> sc = cloner.shallowClone(source);
        assertEquals("string", sc.get("string"));
    }

    @Test
    public void testIgnoreInstanceOf() {
        final Cloner cloner = new Cloner();
        cloner.dontCloneInstanceOf(A.class);

        final A a = new A() {
        };
        assertNotSame(a.getClass(), A.class);
        assertSame(a, cloner.deepClone(a));
    }

    @Test
    public void testImmutableSubclassNotEnabled() {
        final BTestImmutableSubclass a = new BTestImmutableSubclass();
        final BTestImmutableSubclass ca = cloner.deepClone(a);
        assertNotSame(a, ca);
    }

    @Test
    public void testImmutableSubclass() {
        final ATestImmutableSubclass a = new ATestImmutableSubclass();
        assertSame(a, cloner.deepClone(a));
        assertSame(a, cloner.deepClone(a));
    }

    @Test
    public void testImmutable() {
        final ATestImmutable a = new ATestImmutable();
        assertSame(a, cloner.deepClone(a));
        assertSame(a, cloner.deepClone(a));
    }

    /**
     * tests if it happens that in the deep-graph of the cloned objects,
     * if a reference to the same object exists twice, the cloned object
     * will have only 1 clone and references to this clone.
     */
    @Test
    public void testCloningOfSameObject() {
        final Object o1 = new Object();
        final Object o2 = new Object();
        class OO {
            Object o1, o2, o3, o4;
        }
        final OO oo = new OO();
        oo.o1 = o1;
        oo.o2 = o2;
        oo.o3 = o1;
        oo.o4 = o2;
        OO clone = cloner.deepClone(oo);
        assertSame(clone.o1, clone.o3);
        assertSame(clone.o2, clone.o4);

        final HashSet<Object> h1 = new HashSet<>();
        final HashSet<Object> h2 = new HashSet<>();
        oo.o1 = h1;
        oo.o2 = h2;
        oo.o3 = h1;
        oo.o4 = h2;
        clone = cloner.deepClone(oo);
        assertSame(clone.o1, clone.o3);
        assertSame(clone.o2, clone.o4);
        assertNotSame(clone.o1, clone.o2);
        assertNotSame(clone.o2, clone.o3);
    }

    /**
     * tests if immutable clone is the same instance
     */
    @Test
    public void testCloneImmutables() {
        final String s = "test1";
        final String clone1 = cloner.deepClone(s);
        assertSame(s, clone1);
    }

    /**
     * tests if immutable clone is the same instance
     */
    @Test
    public void testCloneFloat() {
        final Float float1 = 8F;
        final Float cloned = cloner.deepClone(float1);
        assertSame(float1, cloned);
        assertEquals(float1, cloned);
    }

    /**
     * tests if arrays are cloned correctly
     */
    @Test
    public void testCloneArrays() {
        final int[] ia = {1, 2, 3};
        final int[] cloned = cloner.deepClone(ia);
        assertEquals(ia.length, cloned.length);
        for (int i = 0; i < ia.length; i++) {
            assertEquals(ia[i], cloned[i]);
        }
        final double[] da = {1, 2, 3};
        final double[] dcloned = cloner.deepClone(da);
        assertEquals(da.length, dcloned.length);
        for (int i = 0; i < ia.length; i++) {
            assertEquals(da[i], dcloned[i]);
        }
    }

    private class Simple {
        private int x = 1;
        private String s = "simple";
        private Complex complex;

        public Complex getComplex() {
            return complex;
        }

        public void setComplex(final Complex complex) {
            this.complex = complex;
        }

        public int getX() {
            return x;
        }

        public void setX(final int x) {
            this.x = x;
        }

        public String getS() {
            return s;
        }

        public void setS(final String s) {
            this.s = s;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Simple) {
                final Simple s = (Simple) obj;
                return s.getS().equals(getS()) && s.getX() == getX();
            }
            return super.equals(obj);
        }
    }

    /**
     * tests cloning of a simple class
     */
    @Test
    public void testCloneSimple() {
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
        assertNotEquals(simple.getS(), clone.getS());
        assertFalse(simple.getX() == clone.getX());
    }

    protected class Complex {
        private int x = 1;
        private String s = "complex";
        private final List<Simple> l = new ArrayList<>();

        public Complex() {
            l.add(new Simple());
            final Simple simple = new Simple();
            simple.setS("s2");
            simple.setX(30);
            l.add(simple);
            simple.setComplex(this);
        }

        public int getX() {
            return x;
        }

        public void setX(final int x) {
            this.x = x;
        }

        public String getS() {
            return s;
        }

        public List<Simple> getL() {
            return l;
        }

        public void setS(final String s) {
            this.s = s;
        }

    }

    /**
     * test cloning of a complex object graph
     */
    @Test
    public void testCloneComplex() {
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

    @Test
    public void testShallowClone() {
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

    @Test
    public void testCloneStack() {
        final List<Integer> lst = new LinkedList<>();
        for (int i = 0; i < 100000; i++) {
            lst.add(i);
        }
        final List<Integer> clone = cloner.deepClone(lst);
        assertEquals(lst.size(), clone.size());
    }

    @Test
    public void testCloneTreeSet() {
        final TreeSet<DC> set = new TreeSet<>();
        final DC dc1 = new DC(5);
        set.add(dc1);
        final DC dc2 = new DC(10);
        set.add(dc2);

        assertTrue(set.contains(dc1));
        assertTrue(set.contains(dc2));

        assertTrue(set.remove(dc1));
        set.add(dc1);

        final TreeSet<DC> set2 = cloner.deepClone(set);

        assertTrue(set2.contains(dc1));
        assertTrue(set2.contains(dc2));
        assertTrue(set2.remove(dc1));
        assertEquals(1, set2.size());
    }

    @Test
    public void testCloneHashSet() {
        Set<DC> set = new HashSet<>();
        final DC dc1 = new DC(5);
        set.add(dc1);
        final DC dc2 = new DC(10);
        set.add(dc2);

        assertTrue(set.contains(dc1));
        assertTrue(set.contains(dc2));

        assertTrue(set.remove(dc1));
        set.add(dc1);

        set = cloner.deepClone(set);

        assertTrue(set.contains(dc1));
        assertTrue(set.contains(dc2));
        assertTrue(set.remove(dc1));
        assertEquals(1, set.size());
    }

    @Test
    public void testCloneStability() {
        for (int i = 0; i < 10; i++) {
            final Complex complex = new Complex();
            complex.setS("x1");
            complex.setX(20);
            final ArrayList<Object> l = new ArrayList<>();
            l.add(complex);
            final HashSet<Object> h1 = new HashSet<>();
            final HashSet<Object> h2 = new HashSet<>();
            for (int j = 0; j < 100; j++) {
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

    @Test
    public void testArrayListCloning() {
        final ArrayList<Object> l = new ArrayList<>();
        l.add(Calendar.getInstance());
        l.add(2);
        l.add(3);
        l.add("kostas");

        final ArrayList<Object> cloned = cloner.deepClone(l);
        assertEquals(l.size(), cloned.size());
        for (int i = 0; i < l.size(); i++) {
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

    @Test
    public void testLinkedListCloning() {
        final LinkedList<Object> l = new LinkedList<>();
        l.add(Calendar.getInstance());
        l.add(2);
        l.add(3);
        l.add("kostas");

        final LinkedList<Object> cloned = cloner.deepClone(l);
        assertEquals(l.size(), cloned.size());
        for (int i = 0; i < l.size(); i++) {
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

    @Test
    public void testHashSetCloning() {
        final HashSet<Object> l = new HashSet<>();
        l.add(Calendar.getInstance());
        l.add(2);
        l.add(3);
        l.add("kostas");

        final HashSet<Object> cloned = cloner.deepClone(l);
        assertNotSame(l, cloned);
        assertEquals(l.size(), cloned.size());
        for (final Object o : l) {
            assertTrue(cloned.contains(o));
        }
    }

    @Test
    public void testHashMapCloning() {
        final HashMap<String, Object> m = new HashMap<>();
        m.put("kostas", Calendar.getInstance());
        m.put("tina", 500);
        m.put("george", "Ah!");
        final HashMap<String, Object> cloned = cloner.deepClone(m);
        assertEquals(m.size(), cloned.size());
        for (final Map.Entry<String, Object> e : m.entrySet()) {
            assertEquals(e.getValue(), cloned.get(e.getKey()));
        }
        assertNotSame(m, cloned);
        assertNotSame(m.get("kostas"), cloned.get("kostas"));
        assertSame(m.get("tina"), cloned.get("tina"));
        cloned.put("x", 100);
        assertEquals(3, m.size());
        assertEquals(4, cloned.size());
    }

    @Test
    public void testTreeMapCloning() {
        final TreeMap<String, Object> m = new TreeMap<>();
        m.put("kostas", Calendar.getInstance());
        m.put("tina", 500);
        m.put("george", "Ah!");
        final TreeMap<String, Object> cloned = cloner.deepClone(m);
        assertEquals(m.size(), cloned.size());
        for (final Map.Entry<String, Object> e : m.entrySet()) {
            assertEquals(e.getValue(), cloned.get(e.getKey()));
        }
        assertNotSame(m, cloned);
        assertNotSame(m.get("kostas"), cloned.get("kostas"));
        assertSame(m.get("tina"), cloned.get("tina"));
        cloned.put("x", 100);
        assertEquals(3, m.size());
        assertEquals(4, cloned.size());
    }

    @Test
    public void testTransientNullPositive() {
        final Cloner c = new Cloner();
        c.setNullTransient(true);
        final TransientTest tt = new TransientTest();
        final TransientTest deepClone = c.deepClone(tt);
        assertNull(deepClone.tr1);
        assertNull(deepClone.a);
        assertEquals(0, deepClone.i);
        assertNotNull(deepClone.nontr);
    }

    @Test
    public void testTransientNullNegative() {
        final Cloner c = new Cloner();
        c.setNullTransient(false);
        final TransientTest tt = new TransientTest();
        final TransientTest deepClone = c.deepClone(tt);
        assertNotNull(deepClone.tr1);
        assertNotNull(deepClone.a);
        assertNotNull(deepClone.nontr);
    }

    @Test
    public void testNullInsteadOfClone() {
        final Cloner c = new Cloner();
        c.nullInsteadOfClone(A.class);

        G g = new G(new A(), new B());
        G deepClone = c.deepClone(g);

        assertNotNull(deepClone.getB());
        assertNull(deepClone.getA());
    }

    @Test
    public void testNullInsteadOfCloneAnnotatedFields() {
        final Cloner c = new Cloner();
        c.nullInsteadOfCloneFieldAnnotation(TestAnnotation.class);

        E e = new E();
        E deepClone = c.deepClone(e);

        assertNotNull(deepClone.getA());
        assertNotSame(e.getA(), deepClone.getA());
        assertNull(deepClone.getId());
    }

    @Test
    public void testCopyPropertiesArrayPrimitive() {
        final int[] src = new int[]{5, 6, 7};
        final int[] dest = new int[3];
        cloner.copyPropertiesOfInheritedClass(src, dest);
        assertEquals(src[0], dest[0]);
        assertEquals(src[1], dest[1]);
        assertEquals(src[2], dest[2]);
    }

    @Test
    public void testCopyPropertiesArray() {
        final Object[] src = new Object[]{5, 8.5f, 3.5d};
        final Object[] dest = new Object[3];
        cloner.copyPropertiesOfInheritedClass(src, dest);
        assertEquals(src[0], dest[0]);
        assertEquals(src[1], dest[1]);
        assertEquals(src[2], dest[2]);
    }

    @Test
    public void testCopyPropertiesInheritedClasses() {
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

    @Test
    public void testFreezable() {
        final F f = new F();
        assertNotSame(f, cloner.deepClone(f));
        f.setFrozen(true);
        assertSame(f, cloner.deepClone(f));
    }

    @Test
    public void testDeepCloneDontCloneInstances() {
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

    static class SynthOuter {
        public Inner getInner() {
            return new Inner();
        }

        class Inner {
            Object x = new Object();

            public SynthOuter getOuter() {
                return SynthOuter.this;
            }
        }
    }

    @Test
    public void testDontCloneSynthetic() {
        final Cloner cloner = new Cloner();
        cloner.setCloneSynthetics(false);
        final SynthOuter outer = new SynthOuter();
        final Inner inner = outer.getInner();
        final Inner clonedInner = cloner.deepClone(inner);
        assertNotSame(inner, clonedInner);
        assertNotSame(inner.x, clonedInner.x);
        assertSame(outer, clonedInner.getOuter());
    }

    @Test
    public void testTreeMapWithComparator() {
        final TreeMap<Object, String> m = new TreeMap<>(Comparator.comparingInt(Object::hashCode));
        m.put(new Object() {
            @Override
            public int hashCode() {
                return 1;
            }
        }, "1");
        m.put(new Object() {
            @Override
            public int hashCode() {
                return 2;
            }
        }, "2");

        final TreeMap<Object, String> clone = cloner.deepClone(m);
        assertEquals(m, clone);
    }

    @Test
    public void testTreeSetWithComparator() {
        final TreeSet<Object> set = new TreeSet<>(Comparator.comparingInt(Object::hashCode));

        set.add(new Object() {
            @Override
            public int hashCode() {
                return 1;
            }
        });
        set.add(new Object() {
            @Override
            public int hashCode() {
                return 2;
            }
        });

        final TreeSet<Object> clone = cloner.deepClone(set);
        assertEquals(set, clone);
    }

    @Test
    public void testEnumIssue9() {
        final TestEnum original = TestEnum.A;
        final TestEnum clone = cloner.deepClone(original);
        assertSame(clone, original);
    }

    @Test
    public void testDate() {
        Date original = new Date();
        Cloner cloner = new Cloner();
        cloner.setNullTransient(true);
        Date clone = cloner.deepClone(original);

        // I expect this to be true, but is is false.
        assertEquals(0, clone.getTime());
    }

    @Test
    public void testUnregisterFastCloner() {
        Cloner cloner = new Cloner();
        cloner.unregisterFastCloner(HashMap.class);
        cloner.registerFastCloner(HashMap.class, new FastClonerHashMap());
    }

    @Test
    public void testEmptyLinkedHashMap() {
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>();
        LinkedHashMap<Integer, Integer> cloned = cloner.deepClone(m);
        assertEquals(m, cloned);
    }

    @Test
    public void testLinkedHashMap() {
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 1; i < 10000; i++) {
            m.put(i, i * 2);
        }
        LinkedHashMap<Integer, Integer> cloned = cloner.deepClone(m);
        assertEquals(m, cloned);
    }

    @Test
    public void testLinkedHashMapIterationOrder() {
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 1000; i >= 1; i--) {
            m.put(i, i * 2);
        }
        LinkedHashMap<Integer, Integer> cloned = cloner.deepClone(m);
        Iterator<Integer> it = cloned.keySet().iterator();
        for (int i = 1000; i >= 1; i--) {
            assertEquals((Integer) i, it.next());
        }
    }

    @Test
    public void testCloneArrayListSubList() {
        List<String> a = new ArrayList<>();
        a.add("1");
        a.add("2");
        a.add("3");

        List<String> b = a.subList(0, 1);

        b.add("3");
        b.remove(0);

        b.size(); // fine

        assertEquals(1, cloner.deepClone(b).size()); // throws ConcurrentModificationException
    }

    @Test
    public void testCloneLinkedListSubList() {
        List<String> a = new LinkedList<>();
        a.add("1");
        a.add("2");
        a.add("3");

        List<String> b = a.subList(0, 1);

        b.add("3");
        b.remove(0);

        b.size(); // fine

        List<String> clone = cloner.deepClone(b);
        assertEquals(1, clone.size());
    }

//	public void testHashMapIterator() {
//		HashMap<Integer, String> m = new HashMap<Integer, String>();
//		m.put(1, "one");
//		m.put(2, "two");
//
//		Iterator<Map.Entry<Integer, String>> it = m.entrySet().iterator();
//		m.put(3, "three");
//
//		Iterator<Map.Entry<Integer, String>> cIt = cloner.deepClone(it);
//		cIt.next(); // throws ConcurrentModificationException
//	}

    @Test
    public void testConcurrentLinkedQueue() {
        ConcurrentLinkedQueue<A> list = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 3000; ++i) {
            A a = new A();
            a.setX(i);
            list.add(a);
        }
        ConcurrentLinkedQueue<A> cloned = cloner.deepClone(list);// StackOverflowError
        assertArrayEquals(list.toArray(), cloned.toArray());
        assertNotSame(list, cloned);
        assertNotSame(list.peek(), cloned.peek());
    }

    /**
     * Test case with EnumMap where one Enum is mapped onto null
     */
    @Test
    public void testEnumMapWithNullValue() {
        EnumMap<TestEnum, String> originalMap = new EnumMap<>(TestEnum.class);
        originalMap.put(TestEnum.A, null);

        EnumMap<TestEnum, String> clonedMap = cloner.deepClone(originalMap);
        assertNotSame(originalMap, clonedMap, "Cloned EnumMap same as original EnumMap");
        assertEquals(1, clonedMap.size(), "Cloned Map not of expected size");
        assertNull(clonedMap.get(TestEnum.A), "Expected value is null (contains key A)");
        assertNull(clonedMap.get(TestEnum.B), "Expected value is null (doesn't contain key B)");
        assertTrue(clonedMap.containsKey(TestEnum.A), "Cloned Map doesn't contain key A");
        assertEquals(originalMap, clonedMap, "Cloned EnumMap not equal to original EnumMap");
    }

    /**
     * Test case for an empty EnumMap
     */
    @Test
    public void testEmptyEnumMap() {
        EnumMap<TestEnum, String> originalMap = new EnumMap<>(TestEnum.class);
        EnumMap<TestEnum, String> clonedMap = cloner.deepClone(originalMap);
        assertNotSame(originalMap, clonedMap, "Cloned EnumMap same as original EnumMap");
        assertEquals(0, clonedMap.size(), "Cloned Map is not empty");
        assertTrue(clonedMap.isEmpty(), "Cloned Map is not empty");
        assertEquals(originalMap, clonedMap, "Cloned EnumMap not equal to original EnumMap");
    }


    /**
     * Test case for non empty EnumMap
     */
    @Test
    public void testEnumMapWithNonNullValue() {
        EnumMap<TestEnum, String> originalMap = new EnumMap<>(TestEnum.class);
        originalMap.put(TestEnum.A, "Hello");
        EnumMap<TestEnum, String> clonedMap = cloner.deepClone(originalMap);
        assertNotSame(originalMap, clonedMap, "Cloned EnumMap not equal to original EnumMap");
        assertEquals(originalMap, clonedMap, "Cloned EnumMap not equal to original EnumMap");
        assertEquals(originalMap.get(TestEnum.A), clonedMap.get(TestEnum.A), "Cloned EnumMap value not equal to original EnumMap");
    }

    /**
     * Test case for EnumMap with non-null mutable value
     * to see if it is deep cloned
     */
    @Test
    public void testEnumMapWithNonNullMutableValue() {
        EnumMap<TestEnum, DC> originalMap = new EnumMap<>(TestEnum.class);
        DC dc = new DC(5);
        originalMap.put(TestEnum.A, dc);
        EnumMap<TestEnum, DC> clonedMap = cloner.deepClone(originalMap);
        assertNotSame(originalMap, clonedMap, "Cloned EnumMap value not equal to original EnumMap");
        assertEquals(originalMap, clonedMap, "Cloned EnumMap not equal to original EnumMap");

        DC dc2 = clonedMap.get(TestEnum.A);
        // Assert references are different
        assertNotSame(dc, dc2, "value not cloned");
        // Assert both objects are equal
        assertEquals(dc, dc2, "Cloned value not equal to original object");

    }

    /**
     * Test case for cloning LinkedHashSet
     */
    @Test
    public void testLinkedHashSetToArray() {
        Set<Object> set = new LinkedHashSet<>();
        set.add(new Object());
        set.add(new Object());

        Set<Object> clonedSet = cloner.deepClone(set);
        assertTrue(clonedSet instanceof LinkedHashSet, "Cloned LinkedHashSet not instanceof LinkedHashSet");


        Object first = clonedSet.toArray()[0];
        assertTrue(clonedSet.contains(first), "First element not contained in cloned LinkedHashSet");
        assertTrue(clonedSet.remove(first), "First element not removed from LinkedHashSet");
    }

    /**
     * Another test case for LinkedHashSet
     */
    @Test
    public void testLinkedHashSetEquals() {
        LinkedHashSet<String> originalSet = new LinkedHashSet<>();
        originalSet.add("Test 1");
        originalSet.add("Test 2");

        LinkedHashSet<String> clonedSet = cloner.deepClone(originalSet);
        assertNotSame(originalSet, clonedSet, "Cloned LinkedHashSet same as original one");
        assertEquals(originalSet, clonedSet, "Cloned LinkedHashSet not equal to original one");
    }


    /**
     * Test if insertion order of LinkedhashSet is the same
     */
    @Test
    public void testLinkedHashSetIterationOrder() {
        LinkedHashSet<Integer> originalSet = new LinkedHashSet<>();
        for (int i = 1000; i >= 1; i--) {
            originalSet.add(i);
        }

        LinkedHashSet<Integer> clonedSet = cloner.deepClone(originalSet);
        assertEquals(originalSet, clonedSet, "Cloned LinkedHashSet not equal to original one");
        int i = 1000;
        for (Integer act : clonedSet) {
            assertEquals(i--, act.intValue(), "LinkedHashSet iteration order not preserved");
        }
    }

    /**
     * Tests if LinkedHashSet with mutable value is deep cloned
     */
    @Test
    public void testLinkedHashSetWithMutableValue() {
        LinkedHashSet<DC> originalSet = new LinkedHashSet<>();
        DC dc = new DC(1000);
        originalSet.add(dc);

        LinkedHashSet<DC> clonedSet = cloner.deepClone(originalSet);
        assertEquals(1, clonedSet.size(), "Size of cloned LinkedHashSet is wrong");
        assertNotSame(originalSet, clonedSet, "Cloned LinkedHashSet same as original one");
        assertEquals(originalSet, clonedSet, "Cloned LinkedHashSet not equal to original one");
        DC dc2 = clonedSet.iterator().next();
        // Assert references are different
        assertNotSame(dc, dc2, "value not cloned");
        // Assert both objects are equal
        assertEquals(dc, dc2, "Cloned value not equal to original object");

    }

    @Test
    public void testStaticTransient() {
        class StaticTransient extends ArrayList<String> {
        }

        cloner.deepClone(new StaticTransient());
    }

    @Test
    @Disabled("this fails with Caused by: java.lang.ClassNotFoundException: com.rits.tests.cloning.TestCloner$$Lambda$54.0x0000000800c24210 It passes only on adopt-openjdk9 v16")
    public void testLambda() {
        Function<ZonedDateTime, Integer> f = ZonedDateTime::getNano;
        Function<ZonedDateTime, Integer> cloned = cloner.deepClone(f);
        assertNotSame(f, cloned);
    }

    private static class ClassWithEnum {
        TimeUnit timeUnit;
    }

    @Test
    public void testClassWithEnum() {
        ClassWithEnum a = new ClassWithEnum();
        a.timeUnit = TimeUnit.SECONDS;
        ClassWithEnum b = cloner.deepClone(a);
        assertSame(TimeUnit.SECONDS, b.timeUnit);
    }
}

