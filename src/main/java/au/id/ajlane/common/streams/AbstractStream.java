package au.id.ajlane.common.streams;

import java.util.NoSuchElementException;

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
    ;

    public final void close() throws StreamCloseException {
        end();
        state = State.CLOSED;
    }

    ;

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
