Yammer Extra
============

<a href="https://raw.githubusercontent.com/ArpNetworking/metrics-yammer-extra/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://travis-ci.org/ArpNetworking/metrics-yammer-extra/">
    <img src="https://travis-ci.org/ArpNetworking/metrics-yammer-extra.png"
         alt="Travis Build">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.metrics.extras%22%20a%3A%22yammer-extra%22">
    <img src="https://img.shields.io/maven-central/v/com.arpnetworking.metrics.extras/yammer-extra.svg"
         alt="Maven Artifact">
</a>

Extension for clients migrating from Yammer that allows migration to our client library while retaining publication to Yammer.


Setup
-----

### Building

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven 3.2.5+](http://maven.apache.org/download.cgi)

Building:
    extras/yammer-extra> mvn package


### Add Dependency

Determine the latest version of the Yammer extra in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.metrics.extras%22%20a%3A%22yammer-extra%22).  Alternatively, install the current version locally:

    extras/yammer-extra> mvn install

Using the local version is intended only for testing or development.

#### Maven

Add a dependency to your pom:

```xml
<dependency>
    <groupId>com.arpnetworking.metrics.extras</groupId>
    <artifactId>yammer-extra</artifactId>
    <version>VERSION</version>
</dependency>
```

Add the Maven Central repository either to your ~/.m2/settings.xml or into your project's pom.  Alternatively, if using the local version no changes are necessary as the local repository is enabled by default.

#### Gradle

Add a dependency to your build.gradle:

    compile group: 'com.arpnetworking.metrics.extras', name: 'yammer-extra', version: 'VERSION'

Add at least one of the Maven Central Repository and/or Local Repository into build.gradle:
 
    mavenCentral()
    mavenLocal()

#### Play

Add a dependency to your project/Build.scala:

```scala
val appDependencies = Seq(
    ...
    "com.arpnetworking.metrics.extras" % "yammer-extra" % "VERSION"
    ...
)
```

The Maven Central repository is included by default.  Alternatively, if using the local version add the local repository into project/plugins.sbt:

    resolvers += Resolver.mavenLocal

### Publishing to Yammer

When your application instantiates the MetricsFactory you should also supply an instance of YammerMetricsSink as part of the sinks collection.  For example: 

```java
final MetricsFactory metricsFactory = new MetricsFactory.Builder()
    .setSinks(Arrays.asList(
        new TsdQueryLogSink.Builder()
            .setPath("/var/logs")
            .setName("myapp-query")
            .build(),
        // Additional Yammer Sink:
        new YammerMetricsSink.Builder()
            .setMetricsRegistry(metricsRegistry)
            .build()));
    .build();
```

The Yammer MetricsRegistry on YammerMetricsSink.Builder is optional and defaults to Metrics.defaultRegistry() if not set (or set to null).

### Publishing via Yammer

This extra package contains classes that can be used to supplement Yammer output.  The first way this can be done is to use the yammer-extra package and record all 
metrics against a MetricsRgistry in the com.arpnetworking.metrics.yammer package.  This will allow existing code written against the Yammer metrics interfaces to work 
ArpNetworking metrics, with the caveat that the use of static methods to create metrics is not supported.  This is the preferred method of using this library.

```java
final MetricsRegistry registry = new com.arpnetworking.metrics.yammer.MetricsFactory();
Counter counter = registry.newCounter("foo");
counter.inc();
```

The other way to use this library is as a drop-in replacement.  Through the use of shading we have created the yammer-replace library that serves as a full 
replacement for Yammer metrics.  This is recommended for times when it is not possible to modify existing code that uses Yammer metrics.  To use this method,
remove the metrics-core.jar from the classpath of the target application and add yammer-replace.jar in its place.  All use of Yammer metrics will instead be 
sent to Arpnetworking metrics (including all static references).


#### Differences

Yammer metrics provides some interfaces that ArpNetworking metrics does not.  For instance, there is no Meter in ArpNetworking metrics, 
and counters act differently in Yammer than here.  This section will detail the differences.

##### Counter

Both Yammer and ArpNetworking metrics contain counters that can be incremented by 1 or any arbitrary number.  The difference is in 
ArpNetworking metrics' separation of units of work and tracking individual samples.  This impedance mismatch is solved by storing each 
call to Yammer's counter inc as a separate sample in ArpNetworking metrics.  This means that loops where inc() is called multiple times 
will translate to multiple samples of '1'.  Normally this will not be a problem and the expected value of the Yammer metric will be in 
the 'sum' statistic's value.

##### Timer

Timers in Yammer and ArpNetworking metrics are very similar.  Their use is functionally equivalent.  The only difference is in the 
reporting of values.  Since ArpNetworking records individual samples, you will have access to statistically correct percentiles, min, max
and counts.
 
##### Meter

Meters exist in Yammer to record rates.  ArpNetworking metrics does not contain meters.  All meters are converted into counters.  
Counters in ArpNetworking metrics allow for the computation of the rates that Yammer metrics produces due to the retention of samples.

##### Histograms

Histograms exist in Yammer to record percentiles.  ArpNetworking metrics retains samples and uses histograms to store the samples.  As a 
result, histograms are converted to counters and provide the same statistics as Yammer histograms provide.

License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2014
