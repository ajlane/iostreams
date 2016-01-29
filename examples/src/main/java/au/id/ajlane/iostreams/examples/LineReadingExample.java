package au.id.ajlane.iostreams.examples;

import au.id.ajlane.iostreams.FilterDecision;
import au.id.ajlane.iostreams.IOStreamException;
import au.id.ajlane.iostreams.IOStreams;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public final class LineReadingExample
{
    public static void main(final String... args) throws IOStreamException
    {
        // Start with a list of file names
        IOStreams.fromArray(args)
            // Read each line from each file
            .flatMap(file -> FileLineReadingIOStream.fromFile(Paths.get(file), StandardCharsets.UTF_8))
            // Filter out empty lines or lines that start with a comment
            .filter(line -> {
                if (line != null && !line.isEmpty() && !line.matches("\\s*(#.*)?")) {
                    return FilterDecision.KEEP_AND_CONTINUE;
                }
                return FilterDecision.SKIP_AND_CONTINUE;
            })
            // Consume each file by printing uncommented lines to standard out.
            .foreach(System.out::println);
    }

    private LineReadingExample(){}
}
