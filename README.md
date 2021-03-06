IOStreams
=========
[![javadoc](http://www.javadoc.io/badge/au.id.ajlane.iostreams/iostreams.svg)](http://www.javadoc.io/doc/au.id.ajlane.iostreams/iostreams)
[![build-status](https://travis-ci.org/ajlane/iostreams.svg?branch=master)](https://travis-ci.org/ajlane/iostreams) [![codecov](https://codecov.io/gh/ajlane/iostreams/branch/master/graph/badge.svg)](https://codecov.io/gh/ajlane/iostreams)

Composable heavy-weight iterators for Java.

An `IOStream` provides `hasNext` and `next` methods, just like an `Iterator`, but is also `Closeable` and throws predictable checked exceptions.

Like `Iterable`, `IOStreamable` types can provide fresh instances of `IOStream` to provide sequential access to a resource.

Utility methods on `IOStreams` and `IOStreamables` allow streams to be transformed and composed.

IOStreams is provided under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Example
-------

This example uses IOStreams to lazily read the given text files and output their contents line-by-line.

```java
public static void main(final String... args) throws IOStreamException, InterruptedException
{
    // Start with a list of file names
    IOStreams.fromArray(args)
        // Read each line from each file
        .flatMap(file -> FileLineIOStream.fromFile(Paths.get(file), StandardCharsets.UTF_8))
        // Filter out empty lines or lines that start with a comment
        .skip(line -> line.text.matches("\\s*(#.*)?"))
        // Prefix with the path and line number, and trim whitespace and comments from the lines that are left
        .map(line -> line.path + "\t" + line.number + "\t" + line.text.replaceAll("^\\s+|\\s*#.*$", ""))
        // Consume each file by printing uncommented lines to standard out.
        .consume(System.out::println);
}
```

**Why not just write a couple of loops?**

Sure, let's have a look at that version.
```java
public static void main(final String... args) throws IOException, InterruptedException{
    // Start with a list of file names
    for(String file : args){
        // Read each line from each file
        try(final BufferedReader reader = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8)){
            int lineNumber = 0;
            for(String lineText = reader.readLine(); lineText != null; lineText = reader.readLine(), lineNumber++){
                if(Thread.interrupted()) throw new InterruptedException();
                // Filter out empty lines or lines that start with a comment
                if(lineText.matches("\\s*(#.*)?")) {
                    // Prefix with the path and line number, and trim whitespace and comments from the lines that are left
                    String result = file + "\t" + lineNumber + "\t" + lineText.replaceAll("^\\s+|\\s*#.*$", "");
                    // Consume each file by printing uncommented lines to standard out
                    System.out.println(result)
                }
            }
        }
    }
}
```

It's just as concise, and doesn't use any libraries. If this is all you need, use this instead. But! Notice that there are three nested bits of logic here:
* Opening and closing files
* Filtering and transforming lines of text
* Displaying results

In the IOStreams example, these are all independent - you could swap out the call to `System.out.println` with something that writes results to another file, _without changing the reading or filtering code at all_. IOStreams allows you to better encapsulate and modularise your data processing code.


Maven
-----

IOStreams is available in Maven Central.
```xml
<dependency>
  <groupId>au.id.ajlane.iostreams</groupId>
  <artifactId>iostreams</artifactId>
  <version>0.0.9</version>
</dependency>
```
