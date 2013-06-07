package au.id.ajlane.common.streams;

public class AbstractStreamFilter<T> implements StreamFilter<T> {

    private boolean terminate = false;

    @Override
    public final FilterDecision apply(final T item) throws StreamFilterException {
        if (keep(item)) {
            if (terminate) return FilterDecision.KEEP_AND_CONTINUE;
            return FilterDecision.SKIP_AND_CONTINUE;
        } else {
            if (terminate) return FilterDecision.SKIP_AND_TERMINATE;
            return FilterDecision.SKIP_AND_CONTINUE;
        }
    }

    @Override
    public void close() throws StreamCloseException {
    }

    protected boolean keep(final T item) throws StreamFilterException {
        return true;
    }

    protected final boolean terminate(final boolean keep) {
        terminate = true;
        return keep;
    }
}
