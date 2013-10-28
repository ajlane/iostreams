Streams
=======

Composable heavy-weight iterators for Java.

A `Stream` provides `hasNext` and `next` methods, just like an `Iterator`, but is also `Closeable` and throws predictable checked exceptions.

Like `Iterable`, `Streamable` types can provide fresh instances of `Stream` to provide sequential access to a resource.

Utility methods on `Streams` and `Streamables` allow streams to be transformed and composed.

Streams is provided under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Example
-------

This example uses Streams to lazily read an arbitrary number of text files and output their contents line-by-line.

```java

public static void main(final String... args) throws StreamException
{
    final Stream<String> files = Streams.fromArray(args);

    // Convert each file into a stream of lines.
    final Stream<String> lines = Streams.flatten(
            files,
            new AbstractStreamTransform<String, Stream<String>>()
            {
                @Override
                protected Stream<String> transform(final String file)
                {
                    return FileLineReadingStream.fromFile(Paths.get(file), StandardCharsets.UTF_8);
                }
            }
    );

    // Filter out any blank lines or lines starting with '#'.
    final Stream<String> filteredLines = Streams.filter(
            lines,
            new AbstractStreamFilter<String>()
            {
                @Override
                public boolean keep(final String line)
                {
                    return line != null && !line.isEmpty() && !line.matches("\\s*(#.*)?");
                }
            }
    );

    // Consume the stream of lines by printing to standard out.
    // We don't care about files or encoding here, the stream will handle all of that for us.
    try
    {
        while (lines.hasNext())
        {
            System.out.println(lines.next());
        }
    }
    finally
    {
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