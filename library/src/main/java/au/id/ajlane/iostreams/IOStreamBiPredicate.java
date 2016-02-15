package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamBiPredicate<A,B> extends AutoCloseable {

     default void close() throws Exception {}

     boolean test(final A a, final B b) throws Exception;
}
