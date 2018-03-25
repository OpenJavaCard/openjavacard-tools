package better.smartcard.gp.scp;

import better.smartcard.gp.keys.GPKey;
import better.smartcard.gp.keys.GPKeySet;
import better.smartcard.gp.keys.GPKeyType;
import better.smartcard.gp.protocol.GPCrypto;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * APDU wrapper object for SCP01 and SCP02
 *
 * This is common for both protocols because
 * they are closely related and very similar.
 */
public class SCP0102Wrapper extends SCPWrapper {

    /**
     * SCP parameters in use
     */
    private final SCP0102Parameters mSCP;
    /**
     * IV for C-MAC
     */
    private byte[] mICV;
    /**
     * IV for R-MAC
     */
    private byte[] mRICV;
    /**
     * Buffer for R-MAC data
     */
    private ByteArrayOutputStream mRMACBuffer;

    /**
     * Construct an SCP01/02 wrapper using the given keys and parameters
     *
     * The wrapper will start with MAC enabled and other features disabled.
     *
     * @param keys to use for the session
     * @param parameters determined for the session
     */
    SCP0102Wrapper(GPKeySet keys, SCP0102Parameters parameters) {
        super(keys, true); // we start with MAC enabled

        // check for unsupported parameters
        if (!parameters.initExplicit) {
            throw new UnsupportedOperationException(parameters + " implicit init is not supported");
        }
        if (parameters.icvMACAID) {
            throw new UnsupportedOperationException(parameters + " ICV=MAC(AID) is not supported");
        }

        // remember protocol parameters
        mSCP = parameters;
    }

    @Override
    public int getMaxSize() {
        int res = 255;
        if (mMAC)
            res -= 8;
        if (mENC)
            res -= 8; // XXX is this strictly correct? i think reasoning was padding.
        return res;
    }

    @Override
    protected void startRMAC() {
        if (!mSCP.rmacSupport) {
            throw new UnsupportedOperationException(mSCP + " does not support RMAC");
        }
        super.startRMAC();
        mRICV = mICV.clone();
    }

    @Override
    public CommandAPDU wrap(CommandAPDU command) throws CardException {
        // fields from the original command
        int cla = command.getCLA();
        int ins = command.getINS();
        int p1 = command.getP1();
        int p2 = command.getP2();
        int dataLen = command.getNc();
        int respLen = command.getNe();
        byte[] data = command.getData();
        // and those fields that get replaced
        int wrappedCla = cla;
        int wrappedLen = dataLen;
        byte[] wrappedData = data;

        // prepare RMAC for unwrapping the response
        if (mRMAC) {
            // create the RMAC buffer
            if (mRMACBuffer == null) {
                mRMACBuffer = new ByteArrayOutputStream();
            }

            // get the RMAC buffer and reset it
            ByteArrayOutputStream rmac = mRMACBuffer;
            rmac.reset();

            // XXX what does the spec say about this?
            rmac.write(clearBits((byte) cla, (byte) 0x07));

            rmac.write(ins);
            rmac.write(p1);
            rmac.write(p2);
            if (dataLen > 0) {
                rmac.write(dataLen);
                rmac.write(data, 0, dataLen);
            }
        }

        // check for length limit
        if (data.length > getMaxSize()) {
            throw new CardException("Can not wrap: CardAPDU too long");
        }

        // we can return the original command if MAC and ENC are disabled
        if (!mMAC && !mENC) {
            return command;
        }

        // perform MAC operation
        if (mMAC) {
            GPKey macKey = mKeys.getKeyByType(GPKeyType.MAC);

            // determine the IV of the MAC
            if (mICV == null) {
                // initially we use a null IV
                mICV = new byte[8];
            } else if (mSCP.icvEncrypt) {
                // encrypt the ICV if enabled
                if (mSCP.scpProtocol == 1) {
                    mICV = GPCrypto.enc_3des_ecb(macKey, mICV);
                } else {
                    mICV = GPCrypto.enc_des_ecb(macKey, mICV);
                }
            }

            // wrap now in case of CMAC-modified
            if (!mSCP.cmacUnmodified) {
                wrappedCla = setBits((byte) cla, (byte) 0x04);
                wrappedLen += 8;
            }

            // collect data for the MAC
            ByteArrayOutputStream macBuffer = new ByteArrayOutputStream();
            macBuffer.write(wrappedCla);
            macBuffer.write(ins);
            macBuffer.write(p1);
            macBuffer.write(p2);
            macBuffer.write(wrappedLen);
            macBuffer.write(data, 0, data.length);
            byte[] macData = macBuffer.toByteArray();

            // perform the MAC computation
            if (mSCP.scpProtocol == 1) {
                mICV = GPCrypto.mac_3des(macKey, macData, mICV);
            } else {
                mICV = GPCrypto.mac_des_3des(macKey, macData, mICV);
            }

            // wrap now in case of CMAC-unmodified
            if (mSCP.cmacUnmodified) {
                wrappedCla = setBits((byte) cla, (byte) 0x04);
                wrappedLen += 8;
            }
        }

        // perform ENC operation
        if (mENC && dataLen > 0) {
            GPKey encKey = mKeys.getKeyByType(GPKeyType.ENC);

            // enc requires mac
            if (!mMAC) {
                throw new UnsupportedOperationException("Can not wrap: ENC without MAC");
            }

            // perform version-dependent padding
            byte[] plainBuf;
            if (mSCP.scpProtocol == 1) {
                // SCP01 has its own magic padding scheme
                plainBuf = GPCrypto.pad80_scp01(data, 8);
            } else {
                // SCP02 just uses pad80
                plainBuf = GPCrypto.pad80(data, 8);
            }

            // perform the encryption
            byte[] encrypted = GPCrypto.enc_3des_cbc_nulliv(encKey, plainBuf);

            // replace data, adjusting size accordingly
            wrappedLen += encrypted.length - dataLen;
            wrappedData = encrypted;
        }

        // build wrapped command
        ByteArrayOutputStream wrapped = new ByteArrayOutputStream();
        wrapped.write(wrappedCla);
        wrapped.write(ins);
        wrapped.write(p1);
        wrapped.write(p2);
        if (wrappedLen > 0) {
            wrapped.write(wrappedLen);
            wrapped.write(wrappedData, 0, wrappedData.length);
        }
        if (mMAC) {
            wrapped.write(mICV, 0, mICV.length);
        }
        if (respLen > 0) {
            wrapped.write(respLen);
        }

        // construct a wrapper for the command
        return new CommandAPDU(wrapped.toByteArray());
    }

    @Override
    public ResponseAPDU unwrap(ResponseAPDU response) throws CardException {
        ResponseAPDU unwrapped = response;

        if (mRMAC) {
            // get the right key
            GPKey key = mKeys.getKeyByType(GPKeyType.RMAC);

            // get fields
            byte[] data = response.getData();
            int sw1 = response.getSW1();
            int sw2 = response.getSW2();

            // check for sufficient length
            if (data.length < 8) {
                throw new CardException("Can not unwrap: response too short");
            }

            // extract the MAC value
            byte[] mac = Arrays.copyOfRange(data, data.length - 8, data.length);

            // complete MAC context with response
            ByteArrayOutputStream rmac = mRMACBuffer;
            rmac.write(data.length - 8);
            rmac.write(data, 0, data.length - 8);
            rmac.write(sw1);
            rmac.write(sw2);

            // perform MAC computation
            byte[] myMAC = GPCrypto.mac_des_3des(key, rmac.toByteArray(), mRICV);

            // compare MAC values
            if (!Arrays.equals(mac, myMAC)) {
                throw new CardException("Can not unwrap: bad response MAC");
            }

            // remember MAC as IV for next round
            mRICV = myMAC;

            // assemble result CardAPDU
            ByteArrayOutputStream myData = new ByteArrayOutputStream();
            myData.write(data, 0, data.length - 8);
            myData.write(sw1);
            myData.write(sw2);

            // and construct an object wrapping it
            unwrapped = new ResponseAPDU(myData.toByteArray());
        }

        return unwrapped;
    }

    private byte clearBits(byte b, byte mask) {
        return (byte) ((b & ~mask) & 0xFF);
    }

    private byte setBits(byte b, byte mask) {
        return (byte) ((b | mask) & 0xFF);
    }

}
