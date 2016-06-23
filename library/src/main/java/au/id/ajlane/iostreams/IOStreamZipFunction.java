package au.id.ajlane.iostreams;

/**
 * A function which combines items from a pair of streams into a single item.
 *
 * @param <A> The type of the first item.
 * @param <B> The type of the second item.
 * @param <Z> The type of the zipped result.
 */
@FunctionalInterface
public interface IOStreamZipFunction<A, B, Z> extends AutoCloseable
{
    /**
     * Applies the function to the given pair of items.
     *
     * @param left The item from the left stream.
     * @param right The item from the right stream.
     * @return The zipped result.
     * @throws Exception If there was a problem in combining the items.
     */
    Z apply(A left, B right) throws Exception;

    @Override
    default void close() throws Exception
    {
    }
}
