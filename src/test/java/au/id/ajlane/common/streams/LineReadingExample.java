package au.id.ajlane.common.streams;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class LineReadingExample {
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
}
