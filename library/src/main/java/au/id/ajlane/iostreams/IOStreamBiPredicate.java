package au.id.ajlane.iostreams;

@FunctionalInterface
public interface IOStreamBiPredicate<A,B> extends AutoCloseable {

    default void close() throws Exception {}

    default IOStreamBiPredicate<A, B> invert() {
        return IOStreamPredicates.invert(this);
    }

    boolean test(final A a, final B b) throws Exception;
}
