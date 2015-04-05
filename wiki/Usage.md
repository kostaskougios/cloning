# WARNING #

Cloning can be potentially dangerous. Cloning files, streams can make the JVM crash. Also cloning proxies (i.e. objects returned by ORM libraries) means a big graph of objects might be cloned which can lead to performance issues and potential crashes. Always enable cloner's debug mode during development, which will print all cloned classes to the console : cloner.setDumpClonedClasses(true)

# Example #
You can create a single instance of cloner and use it throughout your application to deep clone objects. Once instantiated and configured, then the cloner is thread safe and can be reused, provided it's configuration is not altered.

i.e.

```
Cloner cloner=new Cloner();

MyClass clone=cloner.deepClone(o);
// clone is a deep-clone of o
```

Cloner is thread safe.

# Spring framework #

If you use spring, you can declare Cloner as a bean:
```
	<bean id="cloner" class="com.rits.cloning.Cloner"/>
```

or ...
```
<bean id="cloner" class="com.rits.cloning.Cloner">
	<property name="dumpClonedClasses" value="false"/>
	<property name="cloningEnabled" value="true"/>
	<property name="extraImmutables">
		<set>
			<value>... class full name ....</value>
			<value>... class full name ....</value>
		</set>
	</property>
	<property name="extraNullInsteadOfClone">
		<set>
			<value>org.hibernate.engine.SessionImplementor</value>
			<value>org.hibernate.impl.SessionImpl</value>
			<value>org.hibernate.impl.StatelessSessionImpl</value>
			<value>org.hibernate.transaction.JDBCTransaction</value>
		</set>
	</property>
</bean>
```

# Avoid cloning certain classes, null-ifying other classes and registering your own immutables #

The library doesn't clone jdk's immutable objects, i.e. Strings won't be cloned (this is hardcoded into the lib since there is no way to know which classes are immutable). Also you can skip cloning of certain classes if you call cloner.dontClone(X.class). The reference of any instance of X will be used instead of cloning X and any modification of X in the cloned object will be reflected to the original object. This is useful for singletons and i.e. if you clone hibernate entities because the SessionImplementor class shouldn't be cloned:

```
cloner.dontClone(SessionImplementor.class, JDBCTransaction.class, SessionImpl.class);
```

To avoid cloning a class hierarchy :
```
cloner.dontCloneInstanceOf(A.class); // won't clone A and all subclasses
```


If instead of cloning an object, you would rather nullify it, you can do that by calling cloner.nullInsteadOfClone(X.class). This means that whenever an instance of X is encountered, the cloned object reference to X will be replaced by null. Again useful if you prefer to nullify hibernate objects:

```
cloner.nullInsteadOfClone(SessionImplementor.class, JDBCTransaction.class, SessionImpl.class);
```

And you can register immutables calling

```
cloner.registerImmutable(X.class);
```

# Constants #

Constants that are used with the == operator, must be registered (once) in order for them not to be cloned, i.e.

```
...
private static final Object MUTEX=new Object();
...
	
cloner.registerConstant(MyClass.class,"MUTEX"); // reflection is used to read the private field
```

or
```
cloner.registerConstant( MUTEX ); // if you have access to the constant
```

# Dumping cloned classes to the console #

During dev, periodically enable dumping of cloned classes to the console. This way you can see which classes are cloned and exclude those that shouldn't be cloned:

```
cloner.setDumpClonedClasses(true);
```

# Fast cloners #

You can manually clone some of your classes to improve cloning performance. Instantiating the class and copying fields might be faster in several cases. Please Check IFastCloner interface and cloner.registerFastCloner(Class c, IFastCloner fastCloner).
In case you need to clone a custom collection or map, please extend one of the abstract FastClonerCustom**classes.**

# Immutable #

Since 1.7.5 there is a new annotation: @Immutable . Marking a class as @Immutable instructs the cloner to avoid cloning it - a performance optimisation. Please check the source of com.rits.cloning.Immutable for further info.

By default, cloner comes with it's own `@Immutable`. If you want to override this, subclass cloner, i.e.

```
@Target(TYPE)
@Retention(RUNTIME)
static private @interface MyImmutable {}
...
final Cloner cloner = new Cloner()
{
	@Override
	protected Class<?> getImmutableAnnotation()
	{
		return MyImmutable.class;
	}
};
```

Also, `considerImmutable()` offers a more fine-grained alternative:

```
final Cloner cloner = new Cloner()
{
	@Override
	protected boolean considerImmutable(final Class<?> clz)
	{
		return clz == Object.class;
	}
};
```


# More ... #

Can be found by looking at the test cases : https://code.google.com/p/cloning/source/browse/#git%2Fsrc%2Ftest%2Fjava%2Fcom%2Frits%2Ftests%2Fcloning