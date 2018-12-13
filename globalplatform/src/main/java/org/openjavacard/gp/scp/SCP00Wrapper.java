package org.openjavacard.gp.scp;

import org.openjavacard.gp.keys.GPKeySet;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class SCP00Wrapper extends SCPWrapper {

    public SCP00Wrapper(GPKeySet keys, SCP00Parameters parameters) {
        super(keys);
        if(!keys.getKeys().isEmpty()) {
            throw new UnsupportedOperationException("SCP00 should be used with an empty keyset");
        }
    }

    @Override
    protected int getMaxSize() {
        return 255;
    }

    @Override
    public byte[] encryptSensitiveData(byte[] data) throws CardException {
        return data;
    }

    @Override
    public CommandAPDU wrap(CommandAPDU command) throws CardException {
        return command;
    }

    @Override
    public ResponseAPDU unwrap(ResponseAPDU response) throws CardException {
        return response;
    }

}
