package au.id.ajlane.iostreams;

/**
 * Represents a single line in a file.
 * <p>
 * Mutable, for efficiency. Instances should be cloned if they are going to be
 * cached or stored.
 * <p>
 * Does not enforce the validity of its fields.
 */
public final class FileLine implements Cloneable {

    /**
     * The number of the line.
     * <p>
     * Numbering starts at zero.
     */
    public int number;
    /**
     * The path of the file.
     * <p>
     * Must not be null.
     */
    public String path;
    /**
     * The text of the line.
     * <p>
     * May be empty, but must not be null.
     */
    public String text;

    /**
     * Initialises a new instance.
     *
     * @param path   The path of the file. Must not be null.
     * @param number The number of the line. Numbering starts at zero.
     * @param text   The text of the line. May be empty, but must not be null.
     */
    public FileLine(final String path, final int number, final String text) {
        this.path = path;
        this.number = number;
        this.text = text;
    }

    @Override
    protected FileLine clone() {
        return new FileLine(path, number, text);
    }

    @Override
    public String toString(){
        return path + "(" + number + "): " + text;
    }
}
