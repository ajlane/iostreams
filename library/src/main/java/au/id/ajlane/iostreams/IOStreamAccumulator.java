package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamAccumulator<R,T> extends AutoCloseable {
    R add(R result, T item) throws Exception;
    default void close() throws Exception{}
}
