package au.id.ajlane.iostreams.examples;

public final class NumberedLine {
    private final int number;
    private final String text;

    public int number() {
        return number;
    }

    public String text() {
        return text;
    }

    public NumberedLine(final int number, final String text){
        this.number = number;
        this.text = text;
    }

    public String toString(){
        return number + "|" + text;
    }
}
