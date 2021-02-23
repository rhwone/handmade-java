package se.abjorklund.deprecated.jna.xinput;

import java.io.IOException;

public class XInputLibraryNotFound extends IOException {

    private static final long serialVersionUID = 1L;

    public XInputLibraryNotFound(String message) {
        super(message);
    }

}
