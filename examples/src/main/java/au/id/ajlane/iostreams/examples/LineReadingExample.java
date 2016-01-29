package au.id.ajlane.iostreams.examples;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import au.id.ajlane.iostreams.*;

@SuppressWarnings({
                          "JavaDoc",
                          "UseOfSystemOutOrSystemErr",
                          "DynamicRegexReplaceableByCompiledPattern",
                          "UtilityClassWithoutPrivateConstructor"
                  })
public final class LineReadingExample
{
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
}
