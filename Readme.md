## Summary ##
The cloning library is a small, open source (Apache licensed) Java library which deep-clones objects. The objects don't have to implement the Cloneable interface. Effectively, this library can clone ANY Java object. It can be used i.e. in cache implementations if you don't want the cached object to be modified or whenever you want to create a deep copy of objects.

Here is an example of its usage:

```java
Cloner cloner=new Cloner();

MyClass clone=cloner.deepClone(o);
// clone is a deep-clone of o
```

**IMPORTANT** : deep cloning of Java classes might mean thousands of objects are cloned! Also cloning of files and streams might make the JVM crash. Enable dumping of cloned classes to stdout during development is highly recommended in order to view what is cloned.

## Useful links ##
  * [Usage details and examples](wiki/Usage.md)
  * [Maven Dependency definition](wiki/Maven_Dependency.md)



## Our thanks

![yourkit](https://www.yourkit.com/images/yklogo.png)

To yourkit for their excellent profiler.

YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/dotnet-profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).