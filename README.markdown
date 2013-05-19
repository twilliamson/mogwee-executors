# Mogwee Executors, making executors more debuggable

Mogwee Executors contains a few classes designed to replace the executors built in to Java.  Java's standard executors silently fail if an uncaught `RuntimeException` is thrown.  Even worse, a `ScheduledExecutorService` will silently cancel all future executions of a task that has such an exception.

While there are many simpler ways which seem like they should work (e.g., `Thread.UncaughtExceptionHandler` or creating a `ThreadFactory` that wraps a `Runnable` within another `Runnable` that's got a `try…catch`), they don't, mostly due to `ScheduledExecutorService` already having wrapped the `Runnable` before anything else can.  If you think you have a simpler or better way of producing the same behavior, the `TestExecutors` unit test should provide a quick and easy way of checking whether the approach actually works.

In addition to exception logging, Mogwee Executors also ensures the threads created by executors it returns have some base name you specified when creating the executor.  This is always a good idea and can be of great help when analyzing thread dumps.


## Maven Info

	<dependency>
		<groupId>com.mogwee</groupId>
		<artifactId>mogwee-executors</artifactId>
		<version>1.2.0</version>
	</dependency>


## Usage

Just use `com.mogwee.executors.Executors` in the same way you would use `java.util.concurrent.Executors`.

Mogwee Executors relies on SLF4J for logging.  This means you must configure SLF4J to use your logging framework.  For example, to log to System.err, you would add the following to your `pom.xml` dependencies:

	<dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-simple</artifactId>
		<version>1.5.6</version>
	</dependency>

If, on the other hand, you wanted to use Log4J for all your logging, you would instead add the following to your `pom.xml` dependencies:

	<dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-log4j12</artifactId>
		<version>1.5.6</version>
	</dependency>
	<dependency>
		<groupId>log4j</groupId>
		<artifactId>log4j</artifactId>
		<version>1.2.16</version>
	</dependency>

See http://www.slf4j.org/manual.html#binding for more details and options.


## Version History

### 1.2.1:
* Fix Future returned by submit(Runnable) returning null on failure instead of re-throwing exception.

### 1.2.0:
* Add constructor to FailsafeScheduledExecutor that allows custom ThreadFactory to be used (by request of tomdz).

### 1.1.0:
* Use SLF4J instead of Mogwee Logging.


## License (see COPYING file for full license)

Copyright 2011 Ning, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.