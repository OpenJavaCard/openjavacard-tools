/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
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
 */

package org.openjavacard.gp.scp;

import org.openjavacard.gp.crypto.GPBouncy;
import org.openjavacard.gp.crypto.GPCrypto;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Host-side APDU wrapper for SCP03
 */
public class SCP03Wrapper extends SCPWrapper {

    private SCP03Parameters mSCP;

    /**
     * ICV for MAC operations
     *
     * The first half of this is used as the C-MAC.
     */
    private byte[] mICV = new byte[16];

    /**
     * Counter for ENC operations
     *
     * Used to generate the ENC key.
     */
    private long mCTR = 1;

    SCP03Wrapper(GPKeySet keys, SCP03Parameters parameters) {
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

    private byte[] getEncryptionCounter() {
        ByteBuffer ctrBuf = ByteBuffer.allocate(16);
        ctrBuf.put(new byte[]{0,0,0,0,0,0,0,0});
        ctrBuf.putLong(mCTR);
        return ctrBuf.array();
    }

    @Override
    public byte[] encryptSensitiveData(byte[] data) throws CardException {
        // the DEK is static but contained in session keys
        GPKey dek = mKeys.getKeyByUsage(GPKeyUsage.KEK);

        // check size
        if((data.length % 16) != 0) {
            throw new CardException("SCP03 does not allow sensitive data that needs padding");
        }

        // perform encryption
        byte[] encrypted = GPCrypto.enc_aes_cbc_nulliv(dek, data);

        // prepend a length byte
        byte[] result = new byte[encrypted.length + 1];
        result[0] = (byte)encrypted.length;
        for(int i = 0; i < encrypted.length; i++) {
            result[i+1] = encrypted[i];
        }

        // return result
        return result;
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
        int wrappedCla = cla | 0x04;
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
        if (mENC && wrappedLen > 0) {
            GPKey encKey = mKeys.getKeyByUsage(GPKeyUsage.ENC);

            // generate counter-derived IV
            byte[] ctr = getEncryptionCounter();
            byte[] icv = GPCrypto.enc_aes_ecb(encKey, ctr);

            // perform padding
            byte[] plainBuf = GPCrypto.pad80(wrappedData, 16);

            // perform the encryption
            byte[] encrypted = GPCrypto.enc_aes_cbc(encKey, plainBuf, icv);

            // replace data, adjusting size accordingly
            wrappedLen += encrypted.length - dataLen;
            wrappedData = encrypted;
        }

        // increment encryption counter even if no data
        if(mENC) {
            mCTR++;
        }

        // perform MAC operation
        if (mMAC) {
            GPKey macKey = mKeys.getKeyByUsage(GPKeyUsage.MAC);

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
            mICV = GPBouncy.scp03_mac(macKey, macData, 128);
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
        // get fields
        byte[] data = response.getData();
        int sw1 = response.getSW1();
        int sw2 = response.getSW2();

        // perform RMAC
        if (mRMAC) {
            // get the right key
            GPKey rmacKey = mKeys.getKeyByUsage(GPKeyUsage.RMAC);

            // check for sufficient length
            if (data.length < 16) {
                throw new CardException("Can not unwrap: response too short");
            }

            // extract the MAC value
            byte[] mac = Arrays.copyOfRange(data, data.length - 8, data.length);

            // complete MAC context with response
            ByteArrayOutputStream rmac = new ByteArrayOutputStream();
            rmac.write(mICV, 0, mICV.length);
            rmac.write(data, 0, data.length - 8);
            rmac.write(sw1);
            rmac.write(sw2);

            // perform MAC computation
            byte[] macResult = GPBouncy.scp03_mac(rmacKey, rmac.toByteArray(), 128);
            byte[] myMAC = Arrays.copyOfRange(macResult, 0, 8);

            // compare MAC values
            if (!Arrays.equals(mac, myMAC)) {
                throw new CardException("Can not unwrap: bad response MAC");
            }

            data = Arrays.copyOfRange(data, 0, data.length - 8);
        }

        // perform RENC
        if(mRENC && data.length > 0) {
            // RENC uses the ENC key
            GPKey encKey = mKeys.getKeyByUsage(GPKeyUsage.ENC);
            // get counter and modify it for RENC
            byte[] ctr = getEncryptionCounter();
            ctr[0] = (byte)0x80;
            // derive the ICV
            byte[] icv = GPCrypto.enc_aes_ecb(encKey, ctr);
            // perform decryption
            byte[] decrypted = GPCrypto.dec_aes_cbc(encKey, data, icv);
            // remove padding
            data = GPCrypto.unpad80(decrypted);
        }

        // assemble response APDU
        ByteArrayOutputStream myData = new ByteArrayOutputStream();
        myData.write(data, 0, data.length);
        myData.write(sw1);
        myData.write(sw2);

        return new ResponseAPDU(myData.toByteArray());
    }

}
