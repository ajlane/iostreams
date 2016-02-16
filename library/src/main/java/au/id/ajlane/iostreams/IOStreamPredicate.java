package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamPredicate<T> extends AutoCloseable {

    default void close() throws Exception {
    }

    default IOStreamPredicate<T> invert() {
        return IOStreamPredicates.invert(this);
    }

    boolean test(final T item) throws Exception;
}
