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

/**
 * A convenient abstract base class for implementing {@link StreamFilter}.
 *
 * @param <T>
 *         The type of the items in the {@link Stream}.
 */
public abstract class AbstractStreamFilter<T> implements StreamFilter<T> {

    private boolean open = false;
    private boolean terminate = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public final FilterDecision apply(final T item) throws StreamFilterException {
        if (!open) {
            open();
            this.open = false;
        }
        if (keep(item)) {
            if (terminate) return FilterDecision.KEEP_AND_TERMINATE;
            return FilterDecision.KEEP_AND_CONTINUE;
        } else {
            if (terminate) return FilterDecision.SKIP_AND_TERMINATE;
            return FilterDecision.SKIP_AND_CONTINUE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws StreamCloseException {
    }

    /**
     * Determines whether a particular value in the {@link Stream} should be kept.
     * <p/>
     * If all subsequent values in the {@code Stream} should be skipped, for efficiency, wrap your return value with a
     * call to {@link #terminate(boolean)} to have the {@code Stream} terminate early. For example: <pre>{@code
     * final boolean keep = item > 0;
     * return isLast ? terminate(keep) : keep;
     * }</pre>
     *
     * @param item
     *         The item being considered.
     * @return {@code true} to keep the value, {@code false} to skip it. Wrap the return value with a call to {@link
     *         #terminate(boolean)} to terminate the {@code Stream} early.
     * @throws StreamFilterException
     *         If there was any problem in applying the filter.
     * @see #terminate(boolean)
     */
    protected boolean keep(final T item) throws StreamFilterException {
        return true;
    }

    protected void open() throws StreamFilterException {
        // Do nothing by default
    }

    /**
     * Indicates that the {@code Stream} should terminate early.
     * <p/>
     * This method sets a hidden variable on the base class. Once set, the base class will advise the {@code Stream} to
     * terminate early by modifying the result of calls to {@link StreamFilter#apply}.
     * <p/>
     * <i>A {@code StreamFilter} can advise that a {@code Stream} terminate early, but cannot force it to.</i> In some
     * cases (like when using {@link StreamFilters#invert}) filters will be asked to filter values even after they have
     * advised termination.
     * <p/>
     * For convenience, this method passes through the given value for use as a return value in {@link #keep}.
     *
     * @param keep
     *         A
     * @return The value given by {@code keep}.
     * @see #keep
     */
    protected final boolean terminate(final boolean keep) {
        terminate = true;
        return keep;
    }
}
