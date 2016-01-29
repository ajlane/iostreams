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

import java.util.NoSuchElementException;

/**
 * A convenient abstract base class for implementing a {@link IOStream}.
 *
 * @param <T>
 *         The type of the items in the {@code IOStream}.
 */
public abstract class AbstractIOStream<T> implements IOStream<T>
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
    private AbstractIOStream.State state = AbstractIOStream.State.NEW;

    @Override
    public final void close() throws IOStreamCloseException
    {
        this.end();
        this.state = AbstractIOStream.State.CLOSED;
    }

    @Override
    public final boolean hasNext() throws IOStreamReadException
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
    public final T next() throws IOStreamReadException
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
                    this.state = AbstractIOStream.State.NEEDS_NEXT;
                    return this.next;
                case TERMINATED:
                case CLOSED:
                default:
                    throw new NoSuchElementException("There is no next item.");
            }
        }
    }

    /**
     * Releases any resources held by this {@code IOStream}.
     * <p>
     * This method will be called by the base class when {@link #close()} is called.
     * <p>
     * Like {@code close}, successive calls to {@code end()} should have no further effect.
     *
     * @throws IOStreamCloseException
     *         If the {@code IOStream} could not be closed for some reason. The {@code IOStream} may not release all
     *         resources if this is the case.
     */
    protected void end() throws IOStreamCloseException
    {
        // Do nothing by default
    }

    /**
     * Finds the next item in the {@code IOStream}.
     * <p>
     * This method will be called by the base class as necessary when {@link #hasNext()} or {@link #next()} is called.
     * <p>
     * If there is no next item, this method should call {@link #terminate()} and return the dummy value provided by
     * that method.
     * <p>
     * If the thread is interrupted during this method, implementors may choose to throw {@link IOStreamReadException}
     * with an {@link InterruptedException} as the cause.
     *
     * @return The next item in the {@code IOStream}, or the dummy value provided by {@link #terminate()}.
     * @throws IOStreamReadException
     *         If there was any problem accessing the underlying resources of this {@code IOStream}.
     */
    protected T find() throws IOStreamReadException
    {
        return this.terminate();
    }

    /**
     * Prepares any resources required to read this {@code IOStream}.
     * <p>
     * This method will be called by the base class the first time either {@link #hasNext()} or {@link #next()} is
     * called.
     * <p>
     * If  the thread is interrupted during this method, implementors may choose to throw {@link IOStreamReadException}
     * with an {@link InterruptedException} as the cause.
     *
     * @throws IOStreamReadException
     *         If there was any problem accessing the underlying resources of this {@code IOStream}.
     */
    protected void open() throws IOStreamReadException
    {
        // Do nothing by default
    }

    /**
     * Terminates the current {@code IOStream}.
     * <p>
     * After this method is called, all calls to {@link #hasNext()} will return {@code false} and all calls to {@link
     * #next()} will throw {@link NoSuchElementException}.
     * <p>
     * The return value of this method is a dummy value that can be used to return from an {@link #find()} method.
     *
     * @return A dummy value that may be returned by {@link #find()}.
     */
    @SuppressWarnings("SameReturnValue")
    protected final T terminate()
    {
        this.state = AbstractIOStream.State.TERMINATED;
        return null;
    }

    private void doFind() throws IOStreamReadException
    {
        this.next = this.find();
        if (this.state == AbstractIOStream.State.NEEDS_NEXT)
        {
            this.state = AbstractIOStream.State.HAS_NEXT;
        }
    }

    private void doOpen() throws IOStreamReadException
    {
        this.open();
        if (this.state == AbstractIOStream.State.NEW)
        {
            this.state = AbstractIOStream.State.NEEDS_NEXT;
        }
    }
}
