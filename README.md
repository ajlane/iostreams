Streams
=======

Composable heavy-weight iterators for Java.

A `Stream` provides `hasNext` and `next` methods, just like an `Iterator`, but is also `Closeable` and throws predictable checked exceptions (`StreamReadException` and `StreamCloseException`).

Like `Iterable`, `Streamable` types can provide fresh instances of `Stream` to provide sequential access to a resource.

Utility methods on `Streams` and `Streamable` allow streams to be transformed and composed.

Example
-------
```java

public static void main(final String... args) throws StreamException {
    // Collect all of the file names and convert them to NIO Paths
    final Stream<Path> files = Streams.transform(Streams.fromArray(args), new AbstractStreamTransform<String, Path>() {
        @Override
        protected Path transform(String filename) {
            return Paths.get(filename);
        }
    });

    // Convert each file into a stream of lines and merge the results for all files into a single stream
    final Stream<String> lines = Streams.flatten(files, new AbstractStreamTransform<Path, Stream<String>>() {
        @Override
        protected Stream<String> transform(Path file) {
            return LineReadingStream.fromFile(file, StandardCharsets.UTF_8);
        }
    });

    // Read each line and print to standard out
    try {
        // Lines will be read lazily, one at a time. Each file will be properly closed before reading the next one.
        while (lines.hasNext()) {
            System.out.println(lines.next());
        }
    } finally {
        lines.close();
    }
}

```
