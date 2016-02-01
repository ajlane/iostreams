package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamTransformExceptionHandler<T> extends AutoCloseable{
    FilterDecision handle(T item, Exception ex) throws Exception;
    default void close() throws Exception{}
}
