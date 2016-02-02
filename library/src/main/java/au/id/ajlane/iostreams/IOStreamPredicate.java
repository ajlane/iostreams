package au.id.ajlane.iostreams;

public interface IOStreamPredicate<T> {
     boolean test(final T item) throws Exception;
}
