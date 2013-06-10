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
 * A convenient abstract base class for implementing {@link Stream}.
 *
 * @param <T>
 *         The type of the items in the {@code Stream}.
 */
public abstract class AbstractStream<T> implements Stream<T> {
    private static enum State {
        NEW,
        NEEDS_NEXT,
        HAS_NEXT,
        TERMINATED,
        CLOSED
    }

    private T next = null;
    private State state = State.NEW;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() throws StreamCloseException {
        end();
        state = State.CLOSED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() throws StreamReadException {
        switch (state) {
            case NEW:
                doOpen();
                return hasNext();
            case NEEDS_NEXT:
                doFind();
                return hasNext();
            case HAS_NEXT:
                return true;
            case TERMINATED:
            case CLOSED:
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final T next() throws NoSuchElementException, StreamReadException {
        switch (state) {
            case NEW:
                doOpen();
                return next();
            case NEEDS_NEXT:
                doFind();
                return next();
            case HAS_NEXT:
                return next;
            case TERMINATED:
            case CLOSED:
            default:
                throw new NoSuchElementException();
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
    protected void end() throws StreamCloseException {
    }

    protected T find() throws StreamReadException {
        return terminate();
    }

    protected void open() throws StreamReadException {
    }

    @SuppressWarnings("SameReturnValue")
    protected final T terminate() {
        state = State.TERMINATED;
        return null;
    }

    private final void doFind() throws StreamReadException {
        this.next = find();
        if (state == State.NEEDS_NEXT) {
            state = State.HAS_NEXT;
        }
    }

    ;

    private final void doOpen() throws StreamReadException {
        open();
        if (state == State.NEW) {
            state = State.NEEDS_NEXT;
        }
    }
}
