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

package au.id.ajlane.common.streams;

import java.util.NoSuchElementException;

/**
 * A convenient abstract base class for implementing a {@link Stream}.
 *
 * @param <T>
 *         The type of the items in the {@code Stream}.
 */
public abstract class AbstractStream<T> implements Stream<T>
{
    private enum State
    {
        NEW,
        NEEDS_NEXT,
        HAS_NEXT,
        TERMINATED,
        CLOSED
    }

    private T next = null;
    private AbstractStream.State state = AbstractStream.State.NEW;

    @Override
    public final void close() throws StreamCloseException
    {
        this.end();
        this.state = AbstractStream.State.CLOSED;
    }

    @Override
    public final boolean hasNext() throws StreamReadException
    {
        while (true)
        {
            switch (this.state)
            {
                case NEW:
                    this.doOpen();
                    continue;
                case NEEDS_NEXT:
                    this.doFind();
                    continue;
                case HAS_NEXT:
                    return true;
                case TERMINATED:
                case CLOSED:
                default:
                    return false;
            }
        }
    }

    @Override
    public final T next() throws StreamReadException
    {
        while (true)
        {
            switch (this.state)
            {
                case NEW:
                    this.doOpen();
                    continue;
                case NEEDS_NEXT:
                    this.doFind();
                    continue;
                case HAS_NEXT:
                    this.state = AbstractStream.State.NEEDS_NEXT;
                    return this.next;
                case TERMINATED:
                case CLOSED:
                default:
                    throw new NoSuchElementException("There is no next item.");
            }
        }
    }

    /**
     * Releases any resources held by this {@code Stream}.
     * <p/>
     * This method will be called by the base class when {@link #close()} is called.
     * <p/>
     * Like {@code close}, successive calls to {@code end()} should have no further effect.
     *
     * @throws StreamCloseException
     *         If the {@code Stream} could not be closed for some reason. The {@code Stream} may not release all
     *         resources if this is the case.
     */
    protected void end() throws StreamCloseException
    {
        // Do nothing by default
    }

    /**
     * Finds the next item in the {@code Stream}.
     * <p/>
     * This method will be called by the base class as necessary when {@link #hasNext()} or {@link #next()} is called.
     * <p/>
     * If there is no next item, this method should call {@link #terminate()} and return the dummy value provided by
     * that method.
     * <p/>
     * If the thread is interrupted during this method, implementors may choose to throw {@link StreamReadException}
     * with an {@link InterruptedException} as the cause.
     *
     * @return The next item in the {@code Stream}, or the dummy value provided by {@link #terminate()}.
     * @throws StreamReadException
     *         If there was any problem accessing the underlying resources of this {@code Stream}.
     */
    protected T find() throws StreamReadException
    {
        return this.terminate();
    }

    /**
     * Prepares any resources required to read this {@code Stream}.
     * <p/>
     * This method will be called by the base class the first time either {@link #hasNext()} or {@link #next()} is
     * called.
     * <p/>
     * If  the thread is interrupted during this method, implementors may choose to throw {@link StreamReadException}
     * with an {@link InterruptedException} as the cause.
     *
     * @throws StreamReadException
     *         If there was any problem accessing the underlying resources of this {@code Stream}.
     */
    protected void open() throws StreamReadException
    {
        // Do nothing by default
    }

    /**
     * Terminates the current {@code Stream}.
     * <p/>
     * After this method is called, all calls to {@link #hasNext()} will return {@code false} and all calls to {@link
     * #next()} will throw {@link NoSuchElementException}.
     * <p/>
     * The return value of this method is a dummy value that can be used to return from an {@link #find()} method.
     *
     * @return A dummy value that may be returned by {@link #find()}.
     */
    @SuppressWarnings("SameReturnValue")
    protected final T terminate()
    {
        this.state = AbstractStream.State.TERMINATED;
        return null;
    }

    private void doFind() throws StreamReadException
    {
        this.next = this.find();
        if (this.state == AbstractStream.State.NEEDS_NEXT)
        {
            this.state = AbstractStream.State.HAS_NEXT;
        }
    }

    private void doOpen() throws StreamReadException
    {
        this.open();
        if (this.state == AbstractStream.State.NEW)
        {
            this.state = AbstractStream.State.NEEDS_NEXT;
        }
    }
}
