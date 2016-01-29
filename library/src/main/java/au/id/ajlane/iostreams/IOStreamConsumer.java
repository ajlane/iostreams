package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamConsumer<T> {
    void accept(T item) throws IOStreamReadException;
}
