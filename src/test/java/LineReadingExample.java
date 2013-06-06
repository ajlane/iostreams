import au.id.ajlane.common.streams.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LineReadingExample {
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
}
