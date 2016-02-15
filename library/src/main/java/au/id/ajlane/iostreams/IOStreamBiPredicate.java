package au.id.ajlane.iostreams;

public interface IOStreamBiPredicate<A,B> {
     boolean test(final A a, final B b) throws Exception;
}
