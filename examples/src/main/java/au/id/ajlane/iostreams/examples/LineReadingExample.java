package au.id.ajlane.iostreams.examples;

import au.id.ajlane.iostreams.FileLineIOStream;
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

    private LineReadingExample(){}
}
