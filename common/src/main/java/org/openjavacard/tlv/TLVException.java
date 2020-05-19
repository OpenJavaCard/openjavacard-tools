package org.openjavacard.tlv;

public class TLVException extends Exception {

    public TLVException(String message) {
        super(message);
    }

    public TLVException(String message, Throwable cause) {
        super(message, cause);
    }

    public TLVException(Throwable cause) {
        super(cause);
    }

}
