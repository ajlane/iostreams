/*
 * Copyright 2013 Aaron Lane
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import au.id.ajlane.iostreams.AbstractIOStream;
import au.id.ajlane.iostreams.IOStream;
import au.id.ajlane.iostreams.IOStreamCloseException;
import au.id.ajlane.iostreams.IOStreamReadException;

/**
 * An {@link IOStream} which lazily reads each line of text from a file.
 * <p>
 * The {@code IOStream} will open the file when either {@link #hasNext} or {@link #next} is first called. It will close
 * the file when {@link #close} is called.
 */
public class FileLineReadingIOStream extends AbstractIOStream<NumberedLine>
{
    /**
     * Provides a {@code FileLineReadingIOStream} for the given file, using the given
     * encoding.
     *
     * @param file
     *         The text file to read.
     * @param charset
     *         The encoding of the text file.
     * @return An instance of {@code FileLineReadingIOStream}.
     */
    public static FileLineReadingIOStream fromFile(final Path file, final Charset charset)
    {
        return new FileLineReadingIOStream(file, charset);
    }

    /**
     * Provides a {@code FileLineReadingIOStream} for the given file, using the
     * system default encoding.
     *
     * @param file
     *         The text file to read.
     * @return An instance of {@code FileLineReadingIOStream}.
     */
    public static FileLineReadingIOStream fromFile(final Path file)
    {
        return FileLineReadingIOStream.fromFile(file, Charset.defaultCharset());
    }

    /**
     * Provides a {@code FileLineReadingIOStream} for the given file, using the given
     * encoding.
     *
     * @param file
     *         The text file to read.
     * @param charset
     *         The encoding of the text file.
     * @return An instance of {@code FileLineReadingIOStream}.
     */
    public static FileLineReadingIOStream fromFile(final File file, final Charset charset)
    {
        return FileLineReadingIOStream.fromFile(file.toPath(), charset);
    }

    /**
     * Provides a {@code FileLineReadingIOStream} for the given file, using the
     * system default encoding.
     *
     * @param file
     *         The text file to read.
     * @return An instance of {@code FileLineReadingIOStream}.
     */
    public static FileLineReadingIOStream fromFile(final File file)
    {
        return FileLineReadingIOStream.fromFile(file.toPath(), Charset.defaultCharset());
    }

    private final Charset charset;
    private final Path file;
    private int count = 0;
    private BufferedReader reader = null;

    private FileLineReadingIOStream(final Path file, final Charset charset)
    {
        this.file = file;
        this.charset = charset;
    }

    /**
     * Provides the number of lines which have been read from this {@code IOStream} so far.
     *
     * @return A positive integer, or zero.
     */
    public int getLineCount()
    {
        return this.count;
    }

    @Override
    public String toString()
    {
        return this.file + " (" + this.charset + "): " + this.count + (this.reader != null ? "+" : "") + "lines";
    }

    @Override
    protected void end() throws IOStreamCloseException
    {
        if (this.reader != null)
        {
            try
            {
                this.reader.close();
                this.reader = null;
            }
            catch (final IOException ex)
            {
                throw new IOStreamCloseException("Could not close the underlying buffered reader.", ex);
            }
        }
    }

    @Override
    protected NumberedLine find() throws IOStreamReadException
    {
        final String value;
        try
        {
            value = this.reader.readLine();
        }
        catch (final IOException ex)
        {
            throw new IOStreamReadException("Could not read from the file.", ex);
        }
        if (value != null)
        {
            int index = this.count;
            this.count++;
            return new NumberedLine(index, value);
        }
        return terminate();
    }

    @Override
    protected void open() throws IOStreamReadException
    {
        try
        {
            this.reader = Files.newBufferedReader(this.file, this.charset);
        }
        catch (final IOException ex)
        {
            throw new IOStreamReadException("Could not open " + this.file.toString() + '.', ex);
        }
    }
}
