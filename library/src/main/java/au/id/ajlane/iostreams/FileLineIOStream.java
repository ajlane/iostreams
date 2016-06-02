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

package au.id.ajlane.iostreams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

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
     * Provides a {@code FileLineIOStream} for the given classpath resource, using the given encoding.
     *
     * @param path
     *     The path of the text file.
     * @param charset
     *     The encoding of the text file.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromClasspath(final String path, final Charset charset)
    {
        return fromByteStream(
            path,
            () -> ClassLoader.getSystemClassLoader()
                .getResourceAsStream(path),
            charset
        );
    }

    /**
     * Provides a {@code FileLineIOStream} for the given classpath resource, using the system default encoding.
     *
     * @param path
     *     The path of the text file.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromClasspath(final String path)
    {
        return fromByteStream(
            path,
            () -> ClassLoader.getSystemClassLoader()
                .getResourceAsStream(path),
            Charset.defaultCharset()
        );
    }

    /**
     * Provides a {@code FileLineIOStream} for the given classpath resource, using the given encoding.
     *
     * @param siblingClass
     *     A class loaded from the same {@link ClassLoader}, with the same package.
     * @param path
     *     The path of the text file.
     * @param charset
     *     The encoding of the text file.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromClasspath(
        final Class<?> siblingClass,
        final String path,
        final Charset charset
    )
    {
        return fromByteStream(path, () -> siblingClass.getResourceAsStream(path), charset);
    }

    /**
     * Provides a {@code FileLineIOStream} for the given classpath resource, using the system default encoding.
     *
     * @param siblingClass
     *     A class loaded from the same {@link ClassLoader}, with the same package.
     * @param path
     *     The path of the text file.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromClasspath(final Class<?> siblingClass, final String path)
    {
        return fromClasspath(siblingClass, path, Charset.defaultCharset());
    }

    /**
     * Provides a {@code FileLineIOStream} for the given file, using the given encoding.
     *
     * @param file
     *     The text file to read.
     * @param charset
     *     The encoding of the text file.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromFile(final Path file, final Charset charset)
    {
        return new FileLineIOStream(file.toString(), () -> Files.newBufferedReader(file, charset));
    }

    /**
     * Provides a {@code FileLineIOStream} for the given file, using the system default encoding.
     *
     * @param file
     *     The text file to read.
     *
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
     *     The text file to read.
     * @param charset
     *     The encoding of the text file.
     *
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
     *     The text file to read.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromFile(final File file)
    {
        return FileLineIOStream.fromFile(file.toPath(), Charset.defaultCharset());
    }

    /**
     * Provides a {@code FileLineIOStream} for the data supplied by a function, using the given encoding.
     *
     * @param path
     *     The path of the text file.
     * @param supplier
     *     A function to fetch the data stream.
     * @param charset
     *     The encoding of the text file.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromByteStream(
        final String path,
        final Callable<InputStream> supplier,
        final Charset charset
    )
    {
        return new FileLineIOStream(path, () -> new BufferedReader(new InputStreamReader(supplier.call(), charset)));
    }

    /**
     * Provides a {@code FileLineIOStream} for the data supplied by a function, using the system default encoding.
     *
     * @param path
     *     The path of the text file.
     * @param supplier
     *     A function to fetch the data stream.
     *
     * @return A ready stream.
     */
    public static FileLineIOStream fromByteStream(final String path, final Callable<InputStream> supplier)
    {
        return new FileLineIOStream(path, () -> new BufferedReader(
            new InputStreamReader(supplier.call(), Charset.defaultCharset())));
    }

    private final FileLine lastLine;
    private final String path;
    private final Callable<BufferedReader> supplier;
    private BufferedReader reader = null;

    private FileLineIOStream(final String path, final Callable<BufferedReader> supplier)
    {
        this.path = path;
        this.supplier = supplier;
        this.lastLine = new FileLine(path, -1, null);
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
                throw new IOStreamCloseException(ex);
            }
        }
    }

    @Override
    protected FileLine find() throws IOStreamReadException
    {
        final String value;
        try
        {
            value = this.reader.readLine();
        }
        catch (final IOException ex)
        {
            throw new IOStreamReadException(ex);
        }
        if (value != null)
        {
            lastLine.number++;
            lastLine.text = value;
            return lastLine;
        }
        return terminate();
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

    /**
     * The path to the file that this stream is reading.
     *
     * @return The path to the file.
     */
    public String getPath()
    {
        return path;
    }

    @Override
    protected void open() throws IOStreamReadException
    {
        try
        {
            this.reader = supplier.call();
        }
        catch (final RuntimeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamReadException(new IOException("Could not open " + path + '.', ex));
        }
    }

    @Override
    public String toString()
    {
        return this.path + ": " + getLineCount() + (this.reader != null ? "+" : "") + "lines";
    }
}
