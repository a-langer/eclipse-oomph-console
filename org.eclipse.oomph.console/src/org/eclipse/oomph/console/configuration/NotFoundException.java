package org.eclipse.oomph.console.configuration;

import java.io.IOException;

public class NotFoundException extends IOException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

}
