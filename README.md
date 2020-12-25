# continuous-async-profiler
This is a spring boot library that runs async-profiler in continuous mode.

## Third party software needed

This project is just a set of tool that run **async-profiler**. You need to install it on your operating system. 

### Step 1 - download release or build from sources

Simply go to [async-profiler GitHub](https://github.com/jvm-profiling-tools/async-profiler) and follow instructions written in README.

### Step 2 - copy library to your filesystem

Next you need to copy ```libasyncProfiler.so``` file to any of ```java.library.path``` dir. Here are the defaults:

* ```/usr/java/packages/lib```
* ```/usr/lib64```
* ```/lib64```
* ```/lib```
* ```/usr/lib``` 

If you want to use the other path to  ```libasyncProfiler.so``` you can do it with spring application properties/yaml/... For example:

```properties
asyncProfiler.continuous.profilerLibPath=/path/to/libasyncProfiler.so
```

## How to add use it in spring boot application?

You just need to add dependency to your spring boot application

```xml
<dependency>
    <groupId>com.github.krzysztofslusarski</groupId>
    <artifactId>continuous-async-profiler-spring-starter</artifactId>
    <version>1.1</version>
</dependency>
```

with a proper version.

## How to add use it in spring application without spring boot?

### Step 1 - add dependency 

```xml
<dependency>
    <groupId>com.github.krzysztofslusarski</groupId>
    <artifactId>continuous-async-profiler</artifactId>
    <version>1.1</version>
</dependency>
```

### Step 2 - import configuration

Add a following import in your ```@Configuration``` file:
```java
@Import(ContinuousAsyncProfilerConfiguration.class)
```

## How it works on defaults

The async-profiler is run **all the time** in **wall-clock mode**. Output from the profiler is dumped to the **logs/continuous** directory every 
**60** seconds. The files in the **logs/continuous** directory are stored for **24h**. Once a day files from that directory that matches regex 
```.*_13:0.*``` are copied to **logs/archive** directory. On defaults the archive contains 10 minutes of each profiled day. Files in the 
**logs/archive** directory are stored for **30 days**. 

## Configuration properties and defaults

* ```asyncProfiler.continuous.enabled = true``` - if the tool should work or not
* ```asyncProfiler.continuous.dumpIntervalSeconds = 60``` - time in seconds, how often tool should dump profiler outputs
* ```asyncProfiler.continuous.continuousOutputsMaxAgeHours = 24``` - time in hours, how long to keep files in the continuous directory
* ```asyncProfiler.continuous.archiveOutputsMaxAgeDays = 30``` - time in days, how long to keep files in the archive directory
* ```asyncProfiler.continuous.archiveCopyRegex = .*_13:0.*``` - regex for file name, which files should be copied from the continuous to the archive directory
* ```asyncProfiler.continuous.event = wall``` - async-profiler event to fetch
* ```asyncProfiler.continuous.outputDir.continuous = logs/continuous``` - where continuous output should be stored
* ```asyncProfiler.continuous.outputDir.archive = logs/archive``` - where archive of the outputs should be stored
* ```asyncProfiler.continuous.stopWorkFile = profiler-stop``` - path to a file, if the file exists then profiler is not running, using this file you can turn
on/off profiling at runtime
* ```asyncProfiler.continuous.profilerLibPath``` - path to ```libasyncProfiler.so```

## Troubleshooting

First of all this tool is just an async-profiler runner. If you cannot run plain async-profiler on your OS then you cannot use this tool.