/*
 * openjavacard-tools: OpenJavaCard Development Tools
 * Copyright (C) 2015-2018 Ingo Albrecht, prom@berlin.ccc.de
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package org.openjavacard.gp.scp;

import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyType;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class SCP03Wrapper extends SCPWrapper {

    private SCP03Protocol mSCP;

    /**
     * IV for all cryptographic operations
     *
     * The first half of this is used as the C-MAC.
     */
    private byte[] mICV = new byte[16];

    SCP03Wrapper(GPKeySet keys, SCP03Protocol parameters) {
        super(keys);
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
    public byte[] encryptSensitiveData(byte[] data) throws CardException {
        throw new Error("Not implemented");
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

        // check for length limit
        if (data.length > getMaxSize()) {
            throw new CardException("Can not wrap: C-APDU too long");
        }

        // we can return the original command if MAC and ENC are disabled
        if (!mMAC && !mENC) {
            return command;
        }

        // perform ENC operation
        if (mENC && dataLen > 0) {
            GPKey encKey = mKeys.getKeyByType(GPKeyType.ENC);

            // enc requires mac
            if (!mMAC) {
                throw new UnsupportedOperationException("Can not wrap: ENC without MAC");
            }

            // perform padding
            byte[] plainBuf = GPCrypto.pad80(data, 16);

            // perform the encryption
            byte[] encrypted = GPCrypto.enc_aes_cbc(encKey, plainBuf);

            // replace data, adjusting size accordingly
            wrappedLen += encrypted.length - dataLen;
            wrappedData = encrypted;
        }

        // perform MAC operation
        if (mMAC) {
            GPKey macKey = mKeys.getKeyByType(GPKeyType.MAC);

            // MAC is computed on length including MAC
            wrappedLen += 8;

            // collect data for the MAC
            ByteArrayOutputStream macBuffer = new ByteArrayOutputStream();
            macBuffer.write(mICV, 0, mICV.length);
            macBuffer.write(wrappedCla);
            macBuffer.write(ins);
            macBuffer.write(p1);
            macBuffer.write(p2);
            macBuffer.write(wrappedLen);
            macBuffer.write(wrappedData, 0, wrappedData.length);
            byte[] macData = macBuffer.toByteArray();

            // perform the MAC computation
            mICV = GPCrypto.mac_aes(macKey, macData, mICV);
        }

        // build wrapped command
        ByteArrayOutputStream wrapped = new ByteArrayOutputStream();
        wrapped.write(wrappedCla);
        wrapped.write(ins);
        wrapped.write(p1);
        wrapped.write(p2);
        wrapped.write(wrappedLen);
        wrapped.write(wrappedData, 0, wrappedData.length);
        if (mMAC) {
            wrapped.write(mICV, 0, 8);
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
            if (data.length < 16) {
                throw new CardException("Can not unwrap: response too short");
            }

            // extract the MAC value
            byte[] mac = Arrays.copyOfRange(data, data.length - 8, data.length);

            // complete MAC context with response
            ByteArrayOutputStream rmac = new ByteArrayOutputStream();
            rmac.write(data, 0, data.length - 8);
            rmac.write(sw1);
            rmac.write(sw2);

            // perform MAC computation
            byte[] macResult = GPCrypto.mac_aes(key, rmac.toByteArray(), mICV);
            byte[] myMAC = Arrays.copyOfRange(macResult, 0, 8);

            // compare MAC values
            if (!Arrays.equals(mac, myMAC)) {
                throw new CardException("Can not unwrap: bad response MAC");
            }

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

}
