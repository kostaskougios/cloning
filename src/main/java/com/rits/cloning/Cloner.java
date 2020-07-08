package com.rits.cloning;

import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * Cloner: deep clone objects.
 *
 * This class is thread safe. One instance can be used by multiple threads on the same time.
 *
 * @author kostantinos.kougios
 *         18 Sep 2008
 */
public class Cloner {
	private final IInstantiationStrategy instantiationStrategy;
	private final Set<Class<?>> ignored = new HashSet<Class<?>>();
	private final Set<Class<?>> ignoredInstanceOf = new HashSet<Class<?>>();
	private final Set<Class<?>> nullInstead = new HashSet<Class<?>>();
	private final Set<Class<? extends Annotation>> nullInsteadFieldAnnotations = new HashSet<Class<? extends Annotation>>();
	private final Map<Class<?>, IFastCloner> fastCloners = new HashMap<Class<?>, IFastCloner>();
	private final ConcurrentHashMap<Class<?>, List<Field>> fieldsCache = new ConcurrentHashMap<Class<?>, List<Field>>();
	private List<ICloningStrategy> cloningStrategies;

	private Map<Object, Object> ignoredInstances;

	public IDumpCloned getDumpCloned() {
		return dumpCloned;
	}

	/**
	 * provide a cloned classes dumper (so i.e. they can be logged or stored in a file
	 * instead of the default behaviour which is to println(cloned) )
	 *
	 * @param dumpCloned an implementation of the interface which can dump the
	 *                   cloned classes.
	 */
	public void setDumpCloned(IDumpCloned dumpCloned) {
		this.dumpCloned = dumpCloned;
	}

	private IDumpCloned dumpCloned = null;
	private boolean cloningEnabled = true;
	private boolean nullTransient = false;
	private boolean cloneSynthetics = true;

	public Cloner() {
		this.instantiationStrategy = ObjenesisInstantiationStrategy.getInstance();
		init();
	}

	public Cloner(final IInstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
		init();
	}

	public boolean isNullTransient() {
		return nullTransient;
	}

	/**
	 * this makes the cloner to set a transient field to null upon cloning.
	 *
	 * NOTE: primitive types can't be nulled. Their value will be set to default, i.e. 0 for int
	 *
	 * @param nullTransient true for transient fields to be nulled
	 */
	public void setNullTransient(final boolean nullTransient) {
		this.nullTransient = nullTransient;
	}

	public void setCloneSynthetics(final boolean cloneSynthetics) {
		this.cloneSynthetics = cloneSynthetics;
	}

	private void init() {
		registerKnownJdkImmutableClasses();
		registerKnownConstants();
		registerFastCloners();
	}

	/**
	 * registers a std set of fast cloners.
	 */
	protected void registerFastCloners() {
		registerFastCloner(GregorianCalendar.class, new FastClonerCalendar());
		registerFastCloner(ArrayList.class, new FastClonerArrayList());
		registerFastCloner(LinkedList.class, new FastClonerLinkedList());
		registerFastCloner(HashSet.class, new FastClonerHashSet());
		registerFastCloner(HashMap.class, new FastClonerHashMap());
		registerFastCloner(TreeMap.class, new FastClonerTreeMap());
		registerFastCloner(TreeSet.class, new FastClonerTreeSet());
		registerFastCloner(LinkedHashMap.class, new FastClonerLinkedHashMap());
		registerFastCloner(ConcurrentHashMap.class, new FastClonerConcurrentHashMap());
		registerFastCloner(ConcurrentLinkedQueue.class, new FastClonerConcurrentLinkedQueue());
		registerFastCloner(EnumMap.class, new FastClonerEnumMap());
		registerFastCloner(LinkedHashSet.class, new FastClonerLinkedHashSet());

		// register private classes
		FastClonerArrayListSubList subListCloner = new FastClonerArrayListSubList();
		registerInaccessibleClassToBeFastCloned("java.util.AbstractList$SubList", subListCloner);
		registerInaccessibleClassToBeFastCloned("java.util.ArrayList$SubList", subListCloner);
		registerInaccessibleClassToBeFastCloned("java.util.SubList", subListCloner);
		registerInaccessibleClassToBeFastCloned("java.util.RandomAccessSubList", subListCloner);
	}

	protected void registerInaccessibleClassToBeFastCloned(String className, IFastCloner fastCloner) {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			Class<?> subListClz = classLoader.loadClass(className);
			fastCloners.put(subListClz, fastCloner);
		} catch (ClassNotFoundException e) {
			// ignore, maybe a jdk without SubList
		}
	}

	private IDeepCloner deepCloner = this::cloneInternal;

	protected Object fastClone(final Object o, final Map<Object, Object> clones) {
		final Class<? extends Object> c = o.getClass();
		final IFastCloner fastCloner = fastCloners.get(c);
		if (fastCloner != null) return fastCloner.clone(o, deepCloner, clones);
		return null;
	}

	public void registerConstant(Object o) {
		if (ignoredInstances == null) {
			ignoredInstances = new IdentityHashMap<Object, Object>();
		}
		ignoredInstances.put(o, o);
	}

	public void registerConstant(Class<?> c, String privateFieldName) {
		try {
			List<Field> fields = allFields(c);
			for (Field field : fields) {
				if (field.getName().equals(privateFieldName)) {
					field.setAccessible(true);
					final Object v = field.get(null);
					registerConstant(v);
					return;
				}
			}
			throw new CloningException("No such field : " + privateFieldName);
		} catch (final SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new CloningException(e);
		}
	}

	/**
	 * registers some known JDK immutable classes. Override this to register your
	 * own list of jdk's immutable classes
	 */
	protected void registerKnownJdkImmutableClasses() {
		registerImmutable(String.class);
		registerImmutable(Integer.class);
		registerImmutable(Long.class);
		registerImmutable(Boolean.class);
		registerImmutable(Class.class);
		registerImmutable(Float.class);
		registerImmutable(Double.class);
		registerImmutable(Character.class);
		registerImmutable(Byte.class);
		registerImmutable(Short.class);
		registerImmutable(Void.class);

		registerImmutable(BigDecimal.class);
		registerImmutable(BigInteger.class);
		registerImmutable(URI.class);
		registerImmutable(URL.class);
		registerImmutable(UUID.class);
		registerImmutable(Pattern.class);
	}

	protected void registerKnownConstants() {
	}

	public void registerCloningStrategy(ICloningStrategy strategy) {
		if (strategy == null) throw new NullPointerException("strategy can't be null");
		if (cloningStrategies == null) {
			cloningStrategies = new ArrayList<ICloningStrategy>();
		}
		cloningStrategies.add(strategy);
	}

	/**
	 * registers all static fields of these classes. Those static fields won't be cloned when an instance
	 * of the class is cloned.
	 *
	 * This is useful i.e. when a static field object is added into maps or sets. At that point, there is no
	 * way for the cloner to know that it was static except if it is registered.
	 *
	 * @param classes array of classes
	 */
	public void registerStaticFields(final Class<?>... classes) {
		for (final Class<?> c : classes) {
			final List<Field> fields = allFields(c);
			for (final Field field : fields) {
				final int mods = field.getModifiers();
				if (Modifier.isStatic(mods) && !field.getType().isPrimitive()) {
					registerConstant(c, field.getName());
				}
			}
		}
	}

	/**
	 * spring framework friendly version of registerStaticFields
	 *
	 * @param set a set of classes which will be scanned for static fields
	 */
	public void setExtraStaticFields(final Set<Class<?>> set) {
		registerStaticFields(set.toArray(new Class<?>[0]));
	}

	/**
	 * instances of classes that shouldn't be cloned can be registered using this method.
	 *
	 * @param c The class that shouldn't be cloned. That is, whenever a deep clone for
	 *          an object is created and c is encountered, the object instance of c will
	 *          be added to the clone.
	 */
	public void dontClone(final Class<?>... c) {
		for (final Class<?> cl : c) {
			ignored.add(cl);
		}
	}

	public void dontCloneInstanceOf(final Class<?>... c) {
		for (final Class<?> cl : c) {
			ignoredInstanceOf.add(cl);
		}
	}

	public void setDontCloneInstanceOf(final Class<?>... c) {
		dontCloneInstanceOf(c);
	}

	/**
	 * instead of cloning these classes will set the field to null
	 *
	 * @param c the classes to nullify during cloning
	 */
	public void nullInsteadOfClone(final Class<?>... c) {
		for (final Class<?> cl : c) {
			nullInstead.add(cl);
		}
	}

	// spring framework friendly version of nullInsteadOfClone
	public void setExtraNullInsteadOfClone(final Set<Class<?>> set) {
		nullInstead.addAll(set);
	}

	/**
	 * instead of cloning, fields annotated with this annotations will be set to null
	 *
	 * @param a the annotations to nullify during cloning
	 */
	@SafeVarargs
	final public void nullInsteadOfCloneFieldAnnotation(final Class<? extends Annotation>... a) {
		for (final Class<? extends Annotation> an : a) {
			nullInsteadFieldAnnotations.add(an);
		}
	}

	// spring framework friendly version of nullInsteadOfCloneAnnotation
	public void setExtraNullInsteadOfCloneFieldAnnotation(final Set<Class<? extends Annotation>> set) {
		nullInsteadFieldAnnotations.addAll(set);
	}

	/**
	 * registers an immutable class. Immutable classes are not cloned.
	 *
	 * @param c the immutable class
	 */
	public void registerImmutable(final Class<?>... c) {
		for (final Class<?> cl : c) {
			ignored.add(cl);
		}
	}

	// spring framework friendly version of registerImmutable
	public void setExtraImmutables(final Set<Class<?>> set) {
		ignored.addAll(set);
	}

	public void registerFastCloner(final Class<?> c, final IFastCloner fastCloner) {
		if (fastCloners.containsKey(c)) throw new IllegalArgumentException(c + " already fast-cloned!");
		fastCloners.put(c, fastCloner);
	}

	public void unregisterFastCloner(final Class<?> c) {
		fastCloners.remove(c);
	}

	/**
	 * creates a new instance of c. Override to provide your own implementation
	 *
	 * @param <T> the type of c
	 * @param c   the class
	 * @return a new instance of c
	 */
	protected <T> T newInstance(Class<T> c) {
		return instantiationStrategy.newInstance(c);
	}

	@SuppressWarnings("unchecked")
	public <T> T fastCloneOrNewInstance(final Class<T> c) {
		final T fastClone = (T) fastClone(c, null);
		if (fastClone != null) return fastClone;
		return newInstance(c);
	}

	/**
	 * deep clones "o".
	 *
	 * @param <T> the type of "o"
	 * @param o   the object to be deep-cloned
	 * @return a deep-clone of "o".
	 */
	public <T> T deepClone(final T o) {
		if (o == null) return null;
		if (!cloningEnabled) return o;
		if (dumpCloned != null) {
			dumpCloned.startCloning(o.getClass());
		}
		Map<Object, Object> clones = new ClonesMap();
		return cloneInternal(o, clones);
	}

	public <T> T deepCloneDontCloneInstances(final T o, final Object... dontCloneThese) {
		if (o == null) return null;
		if (!cloningEnabled) return o;
		if (dumpCloned != null) {
			dumpCloned.startCloning(o.getClass());
		}
		final Map<Object, Object> clones = new ClonesMap();
		for (final Object dc : dontCloneThese) {
			clones.put(dc, dc);
		}
		return cloneInternal(o, clones);
	}

	/**
	 * shallow clones "o". This means that if c=shallowClone(o) then
	 * c!=o. Any change to c won't affect o.
	 *
	 * @param <T> the type of o
	 * @param o   the object to be shallow-cloned
	 * @return a shallow clone of "o"
	 */
	public <T> T shallowClone(final T o) {
		if (o == null) return null;
		if (!cloningEnabled) return o;
		return cloneInternal(o, null);
	}

	// caches immutables for quick reference
	private final ConcurrentHashMap<Class<?>, Boolean> immutables = new ConcurrentHashMap<Class<?>, Boolean>();
	private boolean cloneAnonymousParent = true;

	/**
	 * override this to decide if a class is immutable. Immutable classes are not cloned.
	 *
	 * @param clz the class under check
	 * @return true to mark clz as immutable and skip cloning it
	 */
	protected boolean considerImmutable(final Class<?> clz) {
		return false;
	}

	protected Class<?> getImmutableAnnotation() {
		return Immutable.class;
	}

	/**
	 * decides if a class is to be considered immutable or not
	 *
	 * @param clz the class under check
	 * @return true if the clz is considered immutable
	 */
	private boolean isImmutable(final Class<?> clz) {
		final Boolean isIm = immutables.get(clz);
		if (isIm != null) return isIm;
		if (considerImmutable(clz)) return true;

		final Class<?> immutableAnnotation = getImmutableAnnotation();
		for (final Annotation annotation : clz.getDeclaredAnnotations()) {
			if (annotation.annotationType() == immutableAnnotation) {
				immutables.put(clz, Boolean.TRUE);
				return true;
			}
		}
		Class<?> c = clz.getSuperclass();
		while (c != null && c != Object.class) {
			for (final Annotation annotation : c.getDeclaredAnnotations()) {
				if (annotation.annotationType() == Immutable.class) {
					final Immutable im = (Immutable) annotation;
					if (im.subClass()) {
						immutables.put(clz, Boolean.TRUE);
						return true;
					}
				}
			}
			c = c.getSuperclass();
		}
		immutables.put(clz, Boolean.FALSE);
		return false;
	}

	private Map<Class, IDeepCloner> cloners = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	protected <T> T cloneInternal(T o, Map<Object, Object> clones) {
		if (o == null) return null;
		if (o == this) return null;

		// Prevent cycles, expensive but necessary
		if (clones != null) {
			T clone = (T) clones.get(o);
			if (clone != null) {
				return clone;
			}
		}

		Class<?> aClass = o.getClass();
		IDeepCloner cloner = cloners.get(aClass);
		if (cloner == null) {
			cloner = findDeepCloner(aClass);
			cloners.put(aClass, cloner);
		}
		if (cloner == IGNORE_CLONER) {
			return o;
		} else if (cloner == NULL_CLONER) {
			return null;
		}
		return cloner.deepClone(o, clones);
	}

	private IDeepCloner findDeepCloner(Class<?> clz) {
		if (Enum.class.isAssignableFrom(clz)) {
			return IGNORE_CLONER;
		} else if (IFreezable.class.isAssignableFrom(clz)) {
			return new IFreezableCloner(clz);
		} else if (nullInstead.contains(clz)) {
			return NULL_CLONER;
		} else if (ignored.contains(clz)) {
			return IGNORE_CLONER;
		} else if (isImmutable(clz)) {
			return IGNORE_CLONER;
		} else if (clz.isArray()) {
			return new CloneArrayCloner(clz);
		} else {
			final IFastCloner fastCloner = fastCloners.get(clz);
			if (fastCloner != null) {
				return new FastClonerCloner(fastCloner);
			} else {
				for (final Class<?> iClz : ignoredInstanceOf) {
					if (iClz.isAssignableFrom(clz)) {
						return IGNORE_CLONER;
					}
				}
			}
		}
		return new CloneObjectCloner(clz);
	}

	private class CloneArrayCloner implements IDeepCloner {

		private boolean primitive;
		private boolean immutable;
		private Class<?> componentType;

		CloneArrayCloner(Class<?> clz) {
			primitive = clz.getComponentType().isPrimitive();
			immutable = isImmutable(clz.getComponentType());
			componentType = clz.getComponentType();
		}

		public <T> T deepClone(T o, Map<Object, Object> clones) {
			if (dumpCloned != null) {
				dumpCloned.startCloning(o.getClass());
			}
			int length = Array.getLength(o);
			@SuppressWarnings("unchecked") T newInstance = (T) Array.newInstance(componentType, length);
			if (clones != null) {
				clones.put(o, newInstance);
			}
			if (primitive || immutable) {
				System.arraycopy(o, 0, newInstance, 0, length);
			} else {
				if (clones == null) {
					for (int i = 0; i < length; i++) {
						Array.set(newInstance, i, Array.get(o, i));
					}
				} else {
					for (int i = 0; i < length; i++) {
						Array.set(newInstance, i, cloneInternal(Array.get(o, i), clones));
					}
				}
			}
			return newInstance;
		}
	}

	private class FastClonerCloner implements IDeepCloner {
		private IFastCloner fastCloner;
		private IDeepCloner cloneInternal;

		FastClonerCloner(IFastCloner fastCloner) {
			this.fastCloner = fastCloner;
			this.cloneInternal = deepCloner;
		}

		public <T> T deepClone(T o, Map<Object, Object> clones) {
			@SuppressWarnings("unchecked") T clone = (T) fastCloner.clone(o, cloneInternal, clones);
			if (clones != null) clones.put(o, clone);
			return clone;
		}
	}

	private static IDeepCloner IGNORE_CLONER = new IgnoreClassCloner();
	private static IDeepCloner NULL_CLONER = new NullClassCloner();

	private static class IgnoreClassCloner implements IDeepCloner {
		public <T> T deepClone(T o, Map<Object, Object> clones) {
			throw new CloningException("Don't call this directly");
		}
	}

	private static class NullClassCloner implements IDeepCloner {
		public <T> T deepClone(T o, Map<Object, Object> clones) {
			throw new CloningException("Don't call this directly");
		}
	}

	private class IFreezableCloner implements IDeepCloner {
		IDeepCloner cloner;

		public IFreezableCloner(Class<?> clz) {
			cloner = new CloneObjectCloner(clz);
		}

		public <T> T deepClone(T o, Map<Object, Object> clones) {
			if (o instanceof IFreezable) {
				IFreezable f = (IFreezable) o;
				if (f.isFrozen()) return o;
			}
			return cloner.deepClone(o, clones);
		}
	}

	private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

	private class CloneObjectCloner implements IDeepCloner {

		private final Field[] fields;
		private final boolean[] shouldClone;
		private final int numFields;
		private final ObjectInstantiator<?> instantiator;

		CloneObjectCloner(Class<?> clz) {
			List<Field> l = new ArrayList<Field>();
			List<Boolean> shouldCloneList = new ArrayList<Boolean>();
			Class<?> sc = clz;
			do {
				Field[] fs = sc.getDeclaredFields();
				for (final Field f : fs) {
					if (!f.isAccessible()) {
						f.setAccessible(true);
					}
					int modifiers = f.getModifiers();
					if (!Modifier.isStatic(modifiers)) {
						if (!(nullTransient && Modifier.isTransient(modifiers)) && !isFieldNullInsteadBecauseOfAnnotation(f)) {
							l.add(f);
							boolean shouldClone = (cloneSynthetics || !f.isSynthetic()) && (cloneAnonymousParent || !isAnonymousParent(f));
							shouldCloneList.add(shouldClone);
						}
					}
				}
			} while ((sc = sc.getSuperclass()) != Object.class && sc != null);
			fields = l.toArray(EMPTY_FIELD_ARRAY);
			numFields = fields.length;
			shouldClone = new boolean[numFields];
			for (int i = 0; i < shouldCloneList.size(); i++) {
				shouldClone[i] = shouldCloneList.get(i);
			}
			instantiator = instantiationStrategy.getInstantiatorOf(clz);
		}

		private boolean isFieldNullInsteadBecauseOfAnnotation(Field f) {
		    if(!nullInsteadFieldAnnotations.isEmpty()) {
                for (Annotation annotation : f.getAnnotations()) {
                    boolean isAnnotatedWithNullInsteadAnnotation =
                            nullInsteadFieldAnnotations.contains(annotation.annotationType());
                    if (isAnnotatedWithNullInsteadAnnotation) {
                        return true;
                    }
                }
            }
			return false;
		}

		public <T> T deepClone(T o, Map<Object, Object> clones) {
			try {
				if (dumpCloned != null) {
					dumpCloned.startCloning(o.getClass());
				}
				@SuppressWarnings("unchecked") T newInstance = (T) instantiator.newInstance();
				if (clones != null) {
					clones.put(o, newInstance);
					for (int i = 0; i < numFields; i++) {
						Field field = fields[i];
						Object fieldObject = field.get(o);
						Object fieldObjectClone = shouldClone[i] ? applyCloningStrategy(clones, o, fieldObject, field) : fieldObject;
						field.set(newInstance, fieldObjectClone);
						if (dumpCloned != null && fieldObjectClone != fieldObject) {
							dumpCloned.cloning(field, o.getClass());
						}
					}
				} else {
					// Shallow clone
					for (int i = 0; i < numFields; i++) {
						Field field = fields[i];
						field.set(newInstance, field.get(o));
					}
				}
				return newInstance;
			} catch (IllegalAccessException e) {
				throw new CloningException(e);
			}
		}
	}

	private Object applyCloningStrategy(Map<Object, Object> clones, Object o, Object fieldObject, Field field) {
		if (cloningStrategies != null) {
			for (ICloningStrategy strategy : cloningStrategies) {
				ICloningStrategy.Strategy s = strategy.strategyFor(o, field);
				if (s == ICloningStrategy.Strategy.NULL_INSTEAD_OF_CLONE) return null;
				if (s == ICloningStrategy.Strategy.SAME_INSTANCE_INSTEAD_OF_CLONE) return fieldObject;
			}
		}
		return cloneInternal(fieldObject, clones);
	}

	private boolean isAnonymousParent(final Field field) {
		return "this$0".equals(field.getName());
	}

	/**
	 * copies all properties from src to dest. Src and dest can be of different class, provided they contain same field names/types
	 *
	 * @param src  the source object
	 * @param dest the destination object which must contain as minimum all the fields of src
	 */
	public <T, E extends T> void copyPropertiesOfInheritedClass(final T src, final E dest) {
		if (src == null) throw new IllegalArgumentException("src can't be null");
		if (dest == null) throw new IllegalArgumentException("dest can't be null");
		final Class<? extends Object> srcClz = src.getClass();
		final Class<? extends Object> destClz = dest.getClass();
		if (srcClz.isArray()) {
			if (!destClz.isArray())
				throw new IllegalArgumentException("can't copy from array to non-array class " + destClz);
			final int length = Array.getLength(src);
			for (int i = 0; i < length; i++) {
				final Object v = Array.get(src, i);
				Array.set(dest, i, v);
			}
			return;
		}
		final List<Field> fields = allFields(srcClz);
		final List<Field> destFields = allFields(dest.getClass());
		for (final Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				try {
					final Object fieldObject = field.get(src);
					field.setAccessible(true);
					if (destFields.contains(field)) {
						field.set(dest, fieldObject);
					}
				} catch (final IllegalArgumentException e) {
					throw new CloningException(e);
				} catch (final IllegalAccessException e) {
					throw new CloningException(e);
				}
			}
		}
	}

	/**
	 * reflection utils
	 */
	private void addAll(final List<Field> l, final Field[] fields) {
		for (final Field field : fields) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			l.add(field);
		}
	}

	/**
	 * reflection utils, override this to choose which fields to clone
	 */
	protected List<Field> allFields(final Class<?> c) {
		List<Field> l = fieldsCache.get(c);
		if (l == null) {
			l = new LinkedList<Field>();
			final Field[] fields = c.getDeclaredFields();
			addAll(l, fields);
			Class<?> sc = c;
			while ((sc = sc.getSuperclass()) != Object.class && sc != null) {
				addAll(l, sc.getDeclaredFields());
			}
			fieldsCache.putIfAbsent(c, l);
		}
		return l;
	}

	public boolean isDumpClonedClasses() {
		return dumpCloned != null;
	}

	/**
	 * will println() all cloned classes. Useful for debugging only. Use
	 * setDumpCloned() if you want to control where to print the cloned
	 * classes.
	 *
	 * @param dumpClonedClasses true to enable printing all cloned classes
	 */
	public void setDumpClonedClasses(final boolean dumpClonedClasses) {
		if (dumpClonedClasses) {
			dumpCloned = new IDumpCloned() {
				public void startCloning(Class<?> clz) {
					System.out.println("clone>" + clz);
				}

				public void cloning(Field field, Class<?> clz) {
					System.out.println("cloned field>" + field + "  -- of class " + clz);
				}
			};
		} else dumpCloned = null;
	}

	public boolean isCloningEnabled() {
		return cloningEnabled;
	}

	public void setCloningEnabled(final boolean cloningEnabled) {
		this.cloningEnabled = cloningEnabled;
	}

	/**
	 * if false, anonymous classes parent class won't be cloned. Default is true
	 */
	public void setCloneAnonymousParent(final boolean cloneAnonymousParent) {
		this.cloneAnonymousParent = cloneAnonymousParent;
	}

	public boolean isCloneAnonymousParent() {
		return cloneAnonymousParent;
	}

	/**
	 * @return a standard cloner instance, will do for most use cases
	 */
	public static Cloner standard() {
		return new Cloner();
	}

	/**
	 * @return if Cloner lib is in a shared jar folder for a container (i.e. tomcat/shared), then
	 * 		this method is preferable in order to instantiate cloner. Please
	 * 		see https://code.google.com/p/cloning/issues/detail?id=23
	 */
	public static Cloner shared() {
		return new Cloner(new ObjenesisInstantiationStrategy());
	}

	private class ClonesMap extends IdentityHashMap<Object, Object> {
		@Override
		public Object get(Object key) {
			if (ignoredInstances != null) {
				Object o = ignoredInstances.get(key);
				if (o != null) return o;
			}
			return super.get(key);
		}
	}
}
