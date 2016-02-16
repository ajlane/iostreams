package au.id.ajlane.iostreams;

public interface PeekableIOStream<T> extends IOStream<T> {
    default T peek() throws IOStreamReadException {
        return this.peek(1).iterator().next();
    }

    Iterable<T> peek(int n) throws IOStreamReadException;
}