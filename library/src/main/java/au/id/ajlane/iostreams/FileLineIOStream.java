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
package au.id.ajlane.iostreams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An {@link IOStream} which lazily reads each line of text from a file.
 * <p>
 * The {@code IOStream} will open the file when either {@link #hasNext} or {@link #next} is first called. It will close
 * the file when {@link #close} is called.
 * <p>
 * The instance of {@link FileLine} is reused between lines. Consumers should {@link FileLine#clone()} a line if they
 * want to preserve its values.
 */
public class FileLineIOStream extends AbstractIOStream<FileLine>
{
    /**
     * Provides a {@code FileLineIOStream} for the given file, using the given encoding.
     *
     * @param file
     *         The text file to read.
     * @param charset
     *         The encoding of the text file.
     * @return A ready stream.
     */
    public static FileLineIOStream fromFile(final Path file, final Charset charset)
    {
        return new FileLineIOStream(file, charset);
    }

    /**
     * Provides a {@code FileLineIOStream} for the given file, using the system default encoding.
     *
     * @param file
     *         The text file to read.
     * @return A ready stream.
     */
    public static FileLineIOStream fromFile(final Path file)
    {
        return FileLineIOStream.fromFile(file, Charset.defaultCharset());
    }

    /**
     * Provides a {@code FileLineIOStream} for the given file, using the given encoding.
     *
     * @param file
     *         The text file to read.
     * @param charset
     *         The encoding of the text file.
     * @return A ready stream.
     */
    public static FileLineIOStream fromFile(final File file, final Charset charset)
    {
        return FileLineIOStream.fromFile(file.toPath(), charset);
    }

    /**
     * Provides a {@code FileLineIOStream} for the given file, using the system default encoding.
     *
     * @param file
     *         The text file to read.
     * @return A ready stream.
     */
    public static FileLineIOStream fromFile(final File file)
    {
        return FileLineIOStream.fromFile(file.toPath(), Charset.defaultCharset());
    }

    private final Charset charset;
    private final Path path;
    private final FileLine lastLine;
    private BufferedReader reader = null;

    private FileLineIOStream(final Path path, final Charset charset)
    {
        this.path = path;
        this.charset = charset;
        this.lastLine = new FileLine(path, -1, null);
    }

    /**
     * The character set this stream is using to decode the file.
     *
     * @return The character set.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * The path to the file that this stream is reading.
     *
     * @return The path to the file.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Provides the number of lines which have been read from this {@code FileLineIOStream} so far.
     *
     * @return A positive integer, or zero.
     */
    public int getLineCount()
    {
        return lastLine.number + 1;
    }

    @Override
    public String toString()
    {
        return this.path + " (" + this.charset + "): " + getLineCount() + (this.reader != null ? "+" : "") + "lines";
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
    protected FileLine find() throws IOStreamReadException {
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
            lastLine.number++;
            lastLine.text = value;
            return lastLine;
        }
        return terminate();
    }

    @Override
    protected void open() throws IOStreamReadException
    {
        try
        {
            this.reader = Files.newBufferedReader(this.path, this.charset);
        }
        catch (final IOException ex)
        {
            throw new IOStreamReadException("Could not open " + this.path.toString() + '.', ex);
        }
    }
}
