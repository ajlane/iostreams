/*
 * Copyright 2016 Aaron Lane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.id.ajlane.iostreams.examples;

import au.id.ajlane.iostreams.FileLineIOStream;
import au.id.ajlane.iostreams.FilterDecision;
import au.id.ajlane.iostreams.IOStreams;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * An example application which lazily reads lines from a set of files, filters them, and prints the lines to standard
 * out.
 */
public final class LineReadingExample
{
    /**
     * Entry-point for the example application.
     *
     * @param args
     *     The list of file names provided as arguments to the application.
     *
     * @throws Exception
     *     If there was a problem in executing the example
     */
    public static void main(final String... args) throws Exception
    {
        // Start with a list of file names
        IOStreams.fromArray(args)
            // Read each line from each file
            .flatMap(file -> FileLineIOStream.fromFile(Paths.get(file), StandardCharsets.UTF_8))
            // Filter out empty lines or lines that start with a comment
            .filter(line -> {
                if (!line.text.matches("\\s*(#.*)?"))
                {
                    return FilterDecision.KEEP_AND_CONTINUE;
                }
                return FilterDecision.SKIP_AND_CONTINUE;
            })
            // Prefix with the path and line number, and trim whitespace and comments from the lines that are left
            .map(line -> line.path + "\t" + line.number + "\t" + line.text.replaceAll("^\\s+|\\s*#.*$", ""))
            // Consume each file by printing uncommented lines to standard out.
            .consume(System.out::println);
    }

    private LineReadingExample()
    {
    }
}
