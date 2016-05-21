package au.id.ajlane.iostreams;

/**
 * A stream which will throw given errors. Used for testing.
 * <p>
 * If the stream has no exception to throw, it will behave as an infinite stream of null values.
 *
 * @param <T>
 *     The type of the items in the stream.
 */
public class ErrorStream<T> implements IOStream<T>
{
    private final Exception closeException;
    private final Exception readException;

    /**
     * Initialises the stream to throw the given exception.
     *
     * @param exception
     *     An exception to throw on read or close. May be null.
     */
    public ErrorStream(final Exception exception)
    {
        this(exception, exception);
    }

    /**
     * Initialises the stream to throw the given exceptions.
     *
     * @param readException
     *     An exception to throw on read. May be null.
     * @param closeException
     *     An exception to throw on close. May be null.
     */
    public ErrorStream(final Exception readException, final Exception closeException)
    {
        this.readException = readException;
        this.closeException = closeException;
    }

    @Override
    public void close() throws IOStreamCloseException
    {
        if (closeException != null)
        {
            throw new IOStreamCloseException(closeException);
        }
    }

    @Override
    public boolean hasNext() throws IOStreamReadException
    {
        if (readException != null)
        {
            throw new IOStreamReadException(readException);
        }
        return true;
    }

    @Override
    public T next() throws IOStreamReadException
    {
        if (readException != null)
        {
            throw new IOStreamReadException(readException);
        }
        return null;
    }
}
