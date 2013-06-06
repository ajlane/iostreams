package au.id.ajlane.common.streams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class LineReadingStream extends AbstractStream<String> {

    public static Stream<String> fromFile(final Path file, final Charset charset) {
        return new LineReadingStream(file, charset);
    }

    public static Stream<String> fromFile(final Path file) {
        return fromFile(file, Charset.defaultCharset());
    }

    public static Stream<String> fromFile(final File file, final Charset charset) {
        return fromFile(file.toPath(), charset);
    }

    public static Stream<String> fromFile(final File file) {
        return fromFile(file.toPath(), Charset.defaultCharset());
    }

    private final Path file;
    private final Charset charset;
    private BufferedReader reader = null;

    private LineReadingStream(Path file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    @Override
    public void end() throws StreamCloseException {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException ex) {
                throw new StreamCloseException("Could not close the underlying buffered reader.", ex);
            }
        }
    }

    @Override
    public String find() throws StreamReadException {
        final String value;
        try {
            value = this.reader.readLine();
        } catch (IOException ex) {
            throw new StreamReadException("Could not read from the file.", ex);
        }
        return value != null ? value : terminate();
    }

    @Override
    protected void open() throws StreamReadException {
        try {
            this.reader = Files.newBufferedReader(file, charset);
        } catch (IOException ex) {
            throw new StreamReadException("Could not open " + file.toString() + ".", ex);
        }
    }
}
