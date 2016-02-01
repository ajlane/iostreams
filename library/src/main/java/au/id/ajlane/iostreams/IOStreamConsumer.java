package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamConsumer<T> extends AutoCloseable {
    void accept(T item) throws Exception;
    default void close() throws Exception { }
}
