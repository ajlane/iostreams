package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamPredicate<T> extends AutoCloseable {
     boolean test(final T item) throws Exception;
     default void close() throws Exception{}
}
