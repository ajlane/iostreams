package au.id.ajlane.common.streams.examples;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import au.id.ajlane.common.streams.*;

@SuppressWarnings({
                          "JavaDoc",
                          "UseOfSystemOutOrSystemErr",
                          "DynamicRegexReplaceableByCompiledPattern",
                          "UtilityClassWithoutPrivateConstructor"
                  })
public final class LineReadingExample
{
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
}
