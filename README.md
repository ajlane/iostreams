IOStreams
=======

Composable heavy-weight iterators for Java. Like Java 8 Streams that can throw checked exceptions.

An `IOStream` provides `hasNext` and `next` methods, just like an `Iterator`, but is also `Closeable` and throws predictable checked exceptions.

Like `Iterable`, `IOStreamable` types can provide fresh instances of `IOStream` to provide sequential access to a resource.

Utility methods on `IOStreams` and `IOStreamables` allow streams to be transformed and composed.

IOStreams is provided under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Example
-------

This example uses Streams to lazily read an arbitrary number of text files and output their contents line-by-line.

```java

public static void main(final String... args) throws IOStreamException
{
    final IOStream<String> files = IOStreams.fromArray(args);
    
    // Convert each file into a stream of lines.
    final IOStream<String> lines = IOStreams.flatten(
            files,
            file -> FileLineReadingIOStream.fromFile(Paths.get(file), StandardCharsets.UTF_8)
    );

    // Filter out any blank lines or lines starting with '#'.
    final IOStream<String> filteredLines = IOStreams.filter(lines, line -> {
        if (line != null && !line.isEmpty() && !line.matches("\\s*(#.*)?")) {
            return FilterDecision.KEEP_AND_CONTINUE;
        }
        return FilterDecision.SKIP_AND_CONTINUE;
    });

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
