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
import java.util.List;
import java.util.NoSuchElementException;
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
class EmptyIOStream implements PeekableIOStream
{
    private static class EmptyIOStreamWithResource extends EmptyIOStream
    {
        private final AutoCloseable resource;

        public EmptyIOStreamWithResource(final AutoCloseable resource)
        {
            this.resource = resource;
        }

        @Override
        public void close() throws IOStreamCloseException
        {
            try
            {
                resource.close();
            }
            catch (final RuntimeException | IOStreamCloseException ex)
            {
                throw ex;
            }
            catch (final Exception ex)
            {
                throw new IOStreamCloseException(ex);
            }
        }
    }

    @Override
    public void close() throws IOStreamCloseException
    {
    }

    @Override
    public void consume() throws IOStreamReadException, IOStreamCloseException
    {
    }

    @Override
    public void consume(final IOStreamConsumer consumer)
        throws IOStreamReadException, IOStreamCloseException
    {
        try
        {
            consumer.close();
        }
        catch (final RuntimeException | IOStreamCloseException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            throw new IOStreamCloseException(ex);
        }
    }

    @Override
    public IOStream filter(final IOStreamFilter filter)
    {
        return new EmptyIOStreamWithResource(filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IOStream flatMap(final IOStreamTransform transform)
    {
        return new EmptyIOStreamWithResource(transform);
    }

    @Override
    public Object fold(final Object initial, final IOStreamAccumulator accumulator)
        throws IOStreamReadException, IOStreamCloseException
    {
        try
        {
            accumulator.close();
        }
        catch (final RuntimeException | IOStreamReadException | IOStreamCloseException ex)
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
        return new EmptyIOStreamWithResource(predicate);
    }

    @Override
    public boolean hasNext()
    {
        return false;
    }

    @Override
    public IOStream keep(final IOStreamPredicate predicate)
    {
        return new EmptyIOStreamWithResource(predicate);
    }

    @Override
    public IOStream limit(int size)
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
        return new EmptyIOStreamWithResource(transform);
    }

    @Override
    public IOStream map(
        final IOStreamTransform transform,
        final IOStreamTransformExceptionHandler exceptionHandler
    )
    {
        return new EmptyIOStreamWithResource(() ->
        {
            try (
                final IOStreamTransform autoCloseTransform = transform;
                final IOStreamTransformExceptionHandler autoCloseExceptionHandler = exceptionHandler
            )
            {
                // Auto close resources
            }
            catch (RuntimeException | IOStreamCloseException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new IOStreamCloseException(ex);
            }
        });
    }

    @Override
    public Object next()
    {
        throw new NoSuchElementException("There is no next item in the stream.");
    }

    @Override
    public IOStream observe(IOStreamConsumer observer)
    {
        return new EmptyIOStreamWithResource(observer);
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
        return new EmptyIOStreamWithResource(reducer);
    }

    @Override
    public IOStream skip(IOStreamPredicate predicate)
    {
        return new EmptyIOStreamWithResource(predicate);
    }

    @Override
    public IOStream split(IOStreamBiPredicate predicate)
    {
        return new EmptyIOStreamWithResource(predicate);
    }

    @Override
    public Object[] toArray(final IntFunction supplier) throws IOStreamReadException, IOStreamCloseException
    {
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
    public Object peek() throws IOStreamReadException
    {
        throw new NoSuchElementException("The stream does not contain any items.");
    }

    @Override
    public Iterable peek(int n) throws IOStreamReadException
    {
        return Collections.emptyList();
    }
}
