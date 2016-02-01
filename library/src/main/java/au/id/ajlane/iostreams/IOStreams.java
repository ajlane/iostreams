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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utilities for working with instances of {@link IOStream}.
 */
public final class IOStreams
{
    private static final IOStream<?> EMPTY = new IOStream<Object>()
    {
        @Override
        public void close()
        {
        }

        @Override
        public int hashCode()
        {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(this.getClass()) && obj == this;
        }

        @Override
        public boolean hasNext()
        {
            return false;
        }



        @Override
        public Object next()
        {
            throw new NoSuchElementException("There is no next item in the stream.");
        }
    };

    public static <T, TCollection extends Collection<T>> TCollection addToCollection(final TCollection collection, final IOStream<T> stream)
        throws IOStreamException
    {
        Objects.requireNonNull(collection, "The collection cannot be null.");
        Objects.requireNonNull(stream, "The stream cannot be null.");
        try
        {
            while (stream.hasNext())
            {
                collection.add(stream.next());
            }
        }
        finally
        {
            stream.close();
        }
        return collection;
    }

    public static <T, R> IOStream<R> cast(final IOStream<T> stream)
    {
        return IOStreams.map(
                stream, new AbstractIOStreamTransform<T, R>()
        {
            @Override
            @SuppressWarnings("unchecked")
            protected R transform(final T item)
            {
                return (R) item;
            }
        }
        );
    }

    public static <T> IOStream<T> concat(final Iterable<? extends IOStream<? extends T>> streams)
    {
        return IOStreams.concat(IOStreams.fromIterable(streams));
    }

    public static <T> IOStream<T> concat(final Iterator<? extends IOStream<? extends T>> streams)
    {
        return IOStreams.concat(IOStreams.fromIterator(streams));
    }

    public static <T> IOStream<T> concat(final IOStreamable<? extends IOStream<? extends T>> streams)
    {
        Objects.requireNonNull(streams, "The streamable cannot be null.");
        return concat(streams.stream());
    }

    public static <T> IOStream<T> concat(final IOStream<? extends IOStream<? extends T>> streams)
    {
        Objects.requireNonNull(streams, "The stream of streams cannot be null.");
        return new AbstractIOStream<T>()
        {
            private IOStream<? extends T> current = null;

            @Override
            protected void end() throws IOStreamCloseException {
                if (current != null) {
                    current.close();
                }
                streams.close();
            }

            @Override
            protected void open() throws IOStreamReadException
            {
                if (streams.hasNext())
                {
                    current = Objects.requireNonNull(streams.next(), "The first concatenated stream was null");
                }
            }

            @Override
            protected T find() throws IOStreamReadException
            {
                while (current != null)
                {
                    if (current.hasNext())
                    {
                        return current.next();
                    }
                    else
                    {
                        try
                        {
                            current.close();
                        }
                        catch (final IOStreamCloseException ex)
                        {
                            throw new IOStreamReadException("Could not close one of the concatenated streams.", ex);
                        }
                        current = streams.hasNext() ? Objects.requireNonNull(
                                streams.next(),
                                "One of the concatenated streams was null."
                        ) : null;
                    }
                }
                return super.find();
            }


        };
    }

    @SafeVarargs
    public static <T> IOStream<T> concat(final IOStream<? extends T>... streams)
    {
        Objects.requireNonNull(streams);

        return new AbstractIOStream<T>()
        {
            private int index = 0;

            @Override
            protected T find() throws IOStreamReadException
            {
                while (index < streams.length)
                {
                    final IOStream<? extends T> stream = Objects.requireNonNull(
                            streams[index],
                            "One of the concatenated streams was null."
                    );
                    if (stream.hasNext())
                    {
                        return stream.next();
                    }
                    else
                    {
                        try
                        {
                            stream.close();
                        }
                        catch (final IOStreamCloseException ex)
                        {
                            throw new IOStreamReadException("Could not close one of the concatenated streams.", ex);
                        }
                        index += 1;
                    }
                }
                return super.find();
            }

            @Override
            protected void end() throws IOStreamCloseException
            {
                if (index < streams.length)
                {
                    if (streams[index] != null) streams[index].close();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> IOStream<T> empty()
    {
        return (IOStream<T>) IOStreams.EMPTY;
    }

    public static <T> IOStream<T> keep(final IOStream<? extends T> stream, final Predicate<? super T> predicate){
        return filter(stream, IOStreamFilters.fromPredicate(predicate));
    }

    public static <T> IOStream<T> skip(final IOStream<? extends T> stream, final Predicate<? super T> predicate){
        return filter(stream, IOStreamFilters.fromPredicate(predicate).invert());
    }

    public static <T> IOStream<T> filter(final IOStream<? extends T> stream, final IOStreamFilter<? super T> filter)
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(filter, "The filter cannot be null.");
        return new AbstractIOStream<T>()
        {
            private volatile boolean terminate = false;

            @Override
            protected void end() throws IOStreamCloseException
            {
                try {
                    filter.close();
                } catch (final RuntimeException ex){
                    throw ex;
                } catch(final Exception ex) {
                    throw new IOStreamCloseException("Could not close the filter.", ex);
                } finally {
                    stream.close();
                }
            }

            @Override
            protected T find() throws IOStreamReadException
            {
                while (!terminate && stream.hasNext())
                {
                    if (Thread.interrupted())
                    {
                        throw new IOStreamReadException(
                                "The thread was interrupted while filtering the stream.",
                                new InterruptedException("The thread was interrupted.")
                        );
                    }
                    final T next = stream.next();
                    final FilterDecision decision;
                    try {
                        decision = filter.apply(next);
                    } catch (final RuntimeException ex) {
                        throw ex;
                    } catch (final Exception ex){
                        throw new IOStreamReadException("Could not decide whether to keep or skip the next item in the stream.", ex);
                    }
                    switch (decision)
                    {
                        case KEEP_AND_CONTINUE:
                            return next;
                        case SKIP_AND_CONTINUE:
                            continue;
                        case KEEP_AND_TERMINATE:
                            this.terminate = true;
                            return next;
                        case SKIP_AND_TERMINATE:
                            return terminate();
                        default:
                            throw new IllegalStateException("Unrecognised decision: " + decision);
                    }
                }
                return terminate();
            }
        };
    }

    public static <T, R> IOStream<R> flatMap(final IOStream<? extends T> stream, final IOStreamTransform<? super T, ? extends IOStream<? extends R>> transform)
    {
        return IOStreams.concat(IOStreams.map(stream, transform));
    }

    public static <T> IOStream<T> flattenArrays(final IOStream<? extends T[]> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromArray);
    }

    public static <T> IOStream<T> flattenIOStreams(final IOStream<? extends IOStream<? extends T>> stream) {
        return IOStreams.concat(stream);
    }

    public static <T> IOStream<T> flattenIterables(final IOStream<? extends Iterable<? extends T>> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromIterable);
    }

    public static <T> IOStream<T> flattenIterators(final IOStream<? extends Iterator<? extends T>> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromIterator);
    }

    public static <T> IOStream<T> flattenStreams(final IOStream<? extends Stream<? extends T>> stream)
    {
        return IOStreams.flatMap(stream, IOStreams::fromStream);
    }

    public static <T> void foreach(final IOStream<T> stream, final IOStreamConsumer<? super T> consumer)
        throws IOStreamReadException, IOStreamCloseException {
        try {
            while (stream.hasNext()) {
                try {
                    consumer.accept(stream.next());
                } catch (final RuntimeException ex) {
                    throw ex;
                } catch (final Exception ex) {
                    throw new IOStreamReadException("Could not consume the next item in the stream.", ex);
                }
            }
        } finally {
            try {
                consumer.close();
            } catch (final RuntimeException ex) {
                throw ex;
            } catch (final Exception ex) {
                throw new IOStreamCloseException("Could not close the consumer.", ex);
            } finally {
                stream.close();
            }
        }
    }

    @SafeVarargs
    public static <T> IOStream<T> fromArray(final T... values)
    {
        Objects.requireNonNull(values, "The array cannot be null. Use an empty array instead.");
        return new IOStream<T>()
        {
            private int index = 0;

            @Override
            public void close()
            {
            }

            @Override
            public boolean hasNext()
            {
                return index < values.length;
            }

            @Override
            public T next()
            {
                if (index < values.length)
                {
                    final T next = values[index];
                    index += 1;
                    return next;
                }
                throw new NoSuchElementException("There is not next item in the stream.");
            }
        };
    }

    public static <T> IOStream<T> fromIterable(final Iterable<? extends T> iterable)
    {
        Objects.requireNonNull(iterable, "The iterable cannot be null.");
        return IOStreams.fromIterator(iterable.iterator());
    }

    public static <T> IOStream<T> fromIterator(final Iterator<? extends T> iterator)
    {
        Objects.requireNonNull(iterator, "The iterator cannot be null.");
        return new IOStream<T>()
        {
            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public T next()
            {
                return iterator.next();
            }

            @Override
            public void close() throws IOStreamCloseException
            {
                if (iterator instanceof AutoCloseable)
                {
                    try
                    {
                        ((AutoCloseable) iterator).close();
                    }
                    catch (final Exception ex)
                    {
                        throw new IOStreamCloseException("Could not close underlying iterator.", ex);
                    }
                }
            }
        };
    }

    public static <T> IOStream<T> fromStream(final Stream<? extends T> stream){
        Objects.requireNonNull(stream, "The stream cannot be null.");
        // TODO: Do a deeper translation to preserve some of the stream's concurrent features.
        return fromIterator(stream.iterator());
    }

    public static <T, R> IOStream<R> map(final IOStream<? extends T> stream,
                                         final IOStreamTransform<? super T, ? extends R> transform) {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(transform, "The transform cannot be null.");
        return new IOStream<R>() {
            @Override
            public void close() throws IOStreamCloseException {
                try {
                    transform.close();
                } catch (final RuntimeException ex) {
                    throw ex;
                } catch (final Exception ex) {
                    throw new IOStreamCloseException("Could not close the transform.", ex);
                } finally {
                    stream.close();
                }
            }

            @Override
            public boolean hasNext() throws IOStreamReadException {
                return stream.hasNext();
            }

            @Override
            public R next() throws IOStreamReadException {
                try {
                    return transform.apply(stream.next());
                } catch (final RuntimeException ex){
                    throw ex;
                } catch (final Exception ex) {
                    throw new IOStreamReadException("Could not transform the next item the in the stream.", ex);
                }
            }
        };
    }

    public static <T, R> IOStream<R> map(final IOStream<? extends T> stream,
                                         final IOStreamTransform<? super T, ? extends R> transform,
                                         final IOStreamTransformExceptionHandler<? super T> exceptionHandler) {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        Objects.requireNonNull(transform, "The transform cannot be null.");
        Objects.requireNonNull(exceptionHandler, "The exception handler cannot be null.");
        return new AbstractIOStream<R>() {
            private volatile boolean terminate = false;

            @Override
            public void end() throws IOStreamCloseException {
                try {
                    exceptionHandler.close();
                } catch(final RuntimeException ex) {
                    throw ex;
                } catch (final Exception ex){
                    throw new IOStreamCloseException("Could not close the exception handler.", ex);
                } finally {
                    try {
                        transform.close();
                    } catch (final RuntimeException ex) {
                        throw ex;
                    } catch (final Exception ex){
                        throw new IOStreamCloseException("Could not close the transform.", ex);
                    } finally {
                        stream.close();
                    }
                }
            }

            @Override
            public R find() throws IOStreamReadException {
                while (!terminate && stream.hasNext()) {
                    if (Thread.interrupted()) {
                        throw new IOStreamReadException(
                            "The thread was interrupted while transforming the stream.",
                            new InterruptedException("The thread was interrupted.")
                        );
                    }
                    final T item = stream.next();
                    try {
                        return transform.apply(item);
                    } catch (final RuntimeException ex){
                        throw ex;
                    } catch (final Exception ex) {
                        final FilterDecision decision;
                        try {
                            decision = exceptionHandler.handle(item, ex);
                        } catch (final RuntimeException ex2) {
                            ex2.addSuppressed(ex);
                            throw ex2;
                        } catch (final Exception ex2){
                            ex2.addSuppressed(ex);
                            throw new IOStreamReadException("The exception handler was unable to handle an exception from the transform.", ex2);
                        }
                        switch (decision) {
                            case KEEP_AND_CONTINUE:
                            case KEEP_AND_TERMINATE:
                                throw new IOStreamReadException("Could not transform the next item in the stream, but the exception handler chose not to skip the item.", ex);
                            case SKIP_AND_CONTINUE:
                                continue;
                            case SKIP_AND_TERMINATE:
                                this.terminate = true;
                                continue;
                            default:
                                final IllegalStateException unrecognised =
                                    new IllegalStateException("Unrecognised decision: " + decision);
                                unrecognised.addSuppressed(ex);
                                throw unrecognised;
                        }
                    }
                }
                return terminate();
            }
        };
    }

    public static <T> IOStream<T> singleton(final T item)
    {
        return new IOStream<T>()
        {
            private boolean hasNext = true;

            @Override
            public void close()
            {
            }

            @Override
            public boolean hasNext()
            {
                return hasNext;
            }

            @Override
            public T next()
            {
                if (hasNext)
                {
                    hasNext = false;
                    return item;
                }
                throw new NoSuchElementException("There is no next item in the stream.");
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(final IOStream<T> stream) throws IOStreamException
    {
        Objects.requireNonNull(stream, "The stream cannot be null.");
        final List<T> list = new ArrayList<>();
        return (T[]) IOStreams.addToCollection(list, stream).toArray();
    }

    public static <T> List<T> toList(final IOStream<T> stream) throws IOStreamException
    {
        Objects.requireNonNull("The stream cannot be null.");
        final List<T> list = new ArrayList<>();
        return IOStreams.addToCollection(list, stream);
    }

    public static <T> Set<T> toSet(final IOStream<T> stream) throws IOStreamException
    {
        Objects.requireNonNull("The stream cannot be null.");
        final Set<T> set = new HashSet<>();
        return IOStreams.addToCollection(set, stream);
    }

    private IOStreams() throws InstantiationException
    {
        throw new InstantiationException("This class cannot be instantiated.");
    }

    public static <T> IOStream<IOStream<T>> group(final IOStream<T> stream, int size) {
        return new AbstractIOStream<IOStream<T>>() {
            @Override
            public void end() throws IOStreamCloseException {
                stream.close();
            }

            @Override
            public IOStream<T> find() throws IOStreamReadException {
                if(stream.hasNext()) {
                    return stream.limit(size);
                }
                return terminate();
            }
        };
    }

    public static <T> IOStream<T> limit(final IOStream<T> stream, final int size){
        return filter(stream, IOStreamFilters.limit(size));
    }
}
