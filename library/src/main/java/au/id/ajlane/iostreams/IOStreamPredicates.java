package au.id.ajlane.iostreams;

public abstract class IOStreamPredicates {

    public static <T> IOStreamPredicate<T> invert(final IOStreamPredicate<T> predicate){
        return new IOStreamPredicate<T>() {
            @Override
            public void close() throws Exception {
                predicate.close();
            }

            @Override
            public boolean test(T item) throws Exception {
                return !predicate.test(item);
            }

            @Override
            public IOStreamPredicate<T> invert() {
                return predicate;
            }
        };
    }

    public static <A,B> IOStreamBiPredicate<A,B> invert(final IOStreamBiPredicate<A,B> predicate){
        return new IOStreamBiPredicate<A, B>() {
            @Override
            public void close() throws Exception {
                predicate.close();
            }

            @Override
            public boolean test(A a, B b) throws Exception {
                return !predicate.test(a,b);
            }

            @Override
            public IOStreamBiPredicate<A,B> invert(){
                return predicate;
            }
        };
    }

    private IOStreamPredicates(){}
}
