IOStreams
=======

Composable heavy-weight iterators for Java. Like Java 8 Streams that can throw checked exceptions.

An `IOStream` provides `hasNext` and `next` methods, just like an `Iterator`, but is also `Closeable` and throws predictable checked exceptions.

Like `Iterable`, `IOStreamable` types can provide fresh instances of `IOStream` to provide sequential access to a resource.

Utility methods on `IOStreams` and `IOStreamables` allow streams to be transformed and composed.

IOStreams is provided under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Example
-------

This example uses IOStreams to lazily read the given text files and output their contents line-by-line.

```java
public static void main(final String... args) throws IOStreamException
{
    // Start with a list of file names
    IOStreams.fromArray(args)
        // Read each line from each file
        .flatMap(file -> FileLineIOStream.fromFile(Paths.get(file), StandardCharsets.UTF_8))
        // Filter out empty lines or lines that start with a comment
        .filter(line -> {
            if (!line.text.matches("\\s*(#.*)?")) {
                return FilterDecision.KEEP_AND_CONTINUE;
            }
            return FilterDecision.SKIP_AND_CONTINUE;
        })
        // Prefix with the path and line number, and trim whitespace and comments from the lines that are left
        .map(line -> line.path + "\t" + line.number + "\t" + line.text.replaceAll("^\\s+|\\s*#.*$", ""))
        // Consume each file by printing uncommented lines to standard out.
        .foreach(System.out::println);
}
```
