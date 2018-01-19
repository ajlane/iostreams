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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * An internal stream implementation for representing immutable empty streams.
 * <p>
 * Allows most operations to be performed more efficiently - most methods just close or ignore any given resources.
 * <p>
 * Because the stream is always empty, we can define the type without using type parameters to specify the type of the
 * items.
 */
public class EmptyIOStream implements PeekableIOStream
{
    private static class EmptyIOStreamWithResource extends EmptyIOStream
    {
        private final AutoCloseable resource;

        /**
         * Initialises the empty stream.
         *
         * @param resource
         *     A dependent resource to close with this stream. Must not be null.
         */
        public EmptyIOStreamWithResource(final AutoCloseable resource)
        {
            this.resource = resource;
        }

        @Override
        public void close() throws IOStreamCloseException, InterruptedException
        {
            try
            {
                resource.close();
            }
            catch (final InterruptedException | RuntimeException ex)
            {
                throw ex;
            }
            catch (final Exception ex)
            {
                throw new IOStreamCloseException(ex);
            }
        }
    }

    /**
     * An empty stream which will close a dependent resource when it is closed.
     *
     * @param resource
     *     The dependent resource. Must not be null.
     *
     * @return An empty stream.
     */
    static EmptyIOStream withResource(final AutoCloseable resource)
    {
        if (resource == null)
        {
            throw new NullPointerException("A non-null resource must be provided.");
        }
        if (resource instanceof EmptyIOStream)
        {
            return (EmptyIOStream) resource;
        }
        return new EmptyIOStreamWithResource(resource);
    }

    @Override
    public void close() throws IOStreamCloseException, InterruptedException
    {
    }

    @Override
    public void consume() throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
    }

    @Override
    public void consume(final IOStreamConsumer consumer)
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        try
        {
            consumer.close();
        }
        catch (final InterruptedException | RuntimeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamCloseException(ex);
        }
    }

    @Override
    public long count() throws IOStreamReadException, IOStreamCloseException
    {
        return 0;
    }

    @Override
    public IOStream filter(final IOStreamFilter filter)
    {
        if (filter == null)
        {
            throw new NullPointerException("A non-null filter must be provided.");
        }
        return new EmptyIOStreamWithResource(filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IOStream flatMap(final IOStreamTransform transform)
    {
        if (transform == null)
        {
            throw new NullPointerException("A non-null transform must be provided.");
        }
        return withResource(transform);
    }

    @Override
    public Object fold(final Object initial, final IOStreamAccumulator accumulator)
        throws IOStreamReadException, IOStreamCloseException, InterruptedException
    {
        try
        {
            accumulator.close();
        }
        catch (final InterruptedException | IOStreamReadException | RuntimeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamCloseException(ex);
        }
        return initial;
    }

    @Override
    public IOStream group(final int size)
    {
        if (size < 1)
        {
            throw new IllegalArgumentException("The size of the groups must be at least 1.");
        }
        return IOStreams.empty();
    }

    @Override
    public IOStream group(final IOStreamBiPredicate predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("A non-null predicate must be provided.");
        }
        return withResource(predicate);
    }

    @Override
    public boolean hasNext()
    {
        return false;
    }

    @Override
    public IOStream keep(final IOStreamPredicate predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("A non-null predicate must be provided.");
        }
        return withResource(predicate);
    }

    @Override
    public IOStream limit(long size)
    {
        if (size < 0)
        {
            throw new IllegalArgumentException("The size of the new stream must be non-negative.");
        }
        return this;
    }

    @Override
    public IOStream map(IOStreamTransform transform)
    {
        return withResource(transform);
    }

    @Override
    public IOStream map(
        final IOStreamTransform transform,
        final IOStreamTransformExceptionHandler exceptionHandler
    )
    {
        if (transform == null)
        {
            throw new NullPointerException("A non-null transform must be provided.");
        }
        if (exceptionHandler == null)
        {
            throw new NullPointerException("A non-null exception handler must be provided.");
        }
        return withResource(() ->
        {
            try (
                final IOStreamTransform autoCloseTransform = transform;
                final IOStreamTransformExceptionHandler autoCloseExceptionHandler = exceptionHandler
            )
            {
                // Auto close resources
            }
            catch (final InterruptedException | RuntimeException ex)
            {
                throw ex;
            }
            catch (final Exception ex)
            {
                throw new IOStreamCloseException(ex);
            }
        });
    }

    @Override
    public Optional max(final Comparator comparator) throws IOStreamReadException, IOStreamCloseException
    {
        if (comparator == null)
        {
            throw new NullPointerException("A non-null comparator must be provided.");
        }
        return Optional.empty();
    }

    @Override
    public Optional min(final Comparator comparator) throws IOStreamReadException, IOStreamCloseException
    {
        if (comparator == null)
        {
            throw new NullPointerException("A non-null comparator must be provided.");
        }
        return Optional.empty();
    }

    @Override
    public Object next()
    {
        throw new NoSuchElementException("There is no next item in the stream.");
    }

    @Override
    public IOStream observe(IOStreamConsumer observer)
    {
        if (observer == null)
        {
            throw new NullPointerException("A non-null observer must be provided.");
        }
        return withResource(observer);
    }

    @Override
    public Object peek() throws IOStreamReadException
    {
        throw new NoSuchElementException("The stream does not contain any items.");
    }

    @Override
    public Iterable peek(int n) throws IOStreamReadException
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("A non-negative number must be provided.");
        }
        return Collections.emptyList();
    }

    @Override
    public PeekableIOStream peekable()
    {
        return this;
    }

    @Override
    public Object reduce(final IOStreamTransform reducer)
        throws IOStreamReadException, IOStreamCloseException
    {
        if (reducer == null)
        {
            throw new NullPointerException("A non-null reducer must be provided.");
        }
        return withResource(reducer);
    }

    @Override
    public IOStream skip(IOStreamPredicate predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("A non-null predicate must be provided.");
        }
        return withResource(predicate);
    }

    @Override
    public IOStream split(IOStreamBiPredicate predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("A non-null predicate must be provided.");
        }
        return withResource(predicate);
    }

    @Override
    public Object[] toArray(final IntFunction supplier) throws IOStreamReadException, IOStreamCloseException
    {
        if (supplier == null)
        {
            throw new NullPointerException("A non-null supplier must be provided.");
        }
        return (Object[]) supplier.apply(0);
    }

    @Override
    public List<Object> toList() throws IOStreamReadException, IOStreamCloseException
    {
        return Collections.emptyList();
    }

    @Override
    public Set<Object> toSet() throws IOStreamReadException, IOStreamCloseException
    {
        return Collections.emptySet();
    }

    @Override
    public IOStream truncate()
    {
        return this;
    }

    @Override
    public IOStream until(final IOStreamPredicate predicate)
    {
        if (predicate == null)
        {
            throw new NullPointerException("A non-null predicate must be provided.");
        }
        return this;
    }
}
