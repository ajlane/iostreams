Streams
=======

Composable heavy-weight iterators for Java.

A `Stream` provides `hasNext` and `next` methods, just like an `Iterator`, but is also `Closeable` and throws predictable checked exceptions.

Like `Iterable`, `Streamable` types can provide fresh instances of `Stream` to provide sequential access to a resource.

Utility methods on `Streams` and `Streamables` allow streams to be transformed and composed.

Example
-------

This example uses Streams to lazily read an arbitrary number of text files and output their contents line-by-line.

```java

    public static void main(final String... args) throws StreamException {

        // Create a stream to read each file and emit the lines of text
        Stream<String> lines = Streams.flatten(Streams.fromArray(args), new AbstractStreamTransform<String, Stream<String>>() {
            @Override
            protected Stream<String> transform(final String file) {
                return LineReadingStream.fromFile(Paths.get(file), StandardCharsets.UTF_8);
            }
        });

        // Filter out blank lines and lines beginning with #
        lines = Streams.filter(lines, new AbstractStreamFilter<String>() {
            @Override
            public boolean keep(final String line) {
                return line != null && line.length() > 0 && !line.matches("\\s*(\\#.*)?");
            }
        });

        // Print to standard out
        print(lines);
    }

    public static void print(final Stream<String> lines) throws StreamException {
        // Read each line and print to standard out
        // We don't care about files or encoding here, the stream will handle all of that for us.
        try {
            while (lines.hasNext()) {
                System.out.println(lines.next());
            }
        } finally {
            lines.close();
        }
    }

```

Maven
-----

Include the following in your pom.xml to start using Streams.

```xml
<dependencies>
    <dependency>
        <groupId>au.id.ajlane.common</groupId>
        <artifactId>streams</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>ajlane-snapshots</id>
        <layout>default</layout>
        <url>http://repository-ajlane.forge.cloudbees.com/snapshot/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```