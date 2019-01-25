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

package org.openjavacard.cap.base;

import org.openjavacard.cap.component.CapMethodComponent;
import org.openjavacard.cap.structure.CapClassRef;
import org.openjavacard.cap.structure.CapMethodRef;
import org.openjavacard.iso.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CapStructureReader {

    private static final Logger LOG = LoggerFactory.getLogger(CapStructureReader.class);

    protected final BufferedInputStream mStream;

    CapStructureReader(byte[] data) {
        mStream = new BufferedInputStream(new ByteArrayInputStream(data));
    }

    public void error(String message) throws IOException {
        throw new IOException(message);
    }

    public int available() throws IOException {
        return mStream.available();
    }

    public boolean hasMore() throws IOException {
        return mStream.available() > 0;
    }

    public int peekByte() throws IOException {
        mStream.mark(1);
        int res = mStream.read();
        if(res == -1) {
            throw new IOException("Unexpected end of file");
        }
        mStream.reset();
        return res;
    }

    public int readByte() throws IOException {
        int res = mStream.read();
        if(res == -1) {
            throw new IOException("Unexpected end of file");
        }
        return res;
    }

    public byte[] readBytes(int count) throws IOException {
        byte[] res = new byte[count];
        if(count == 0) {
            return res;
        }
        if(mStream.read(res) != count) {
            throw new IOException("Unexpected end of file");
        }
        return res;
    }

    public void needBytes(int count) throws IOException {
        if(count > mStream.available()) {
            throw new IOException("Unexpected end of file");
        }
    }

    public int peekU1() throws IOException {
        return peekByte();
    }

    public int readU1() throws IOException {
        return readByte();
    }

    public int[] readU1Array(int count) throws IOException {
        needBytes(count);
        int[] res = new int[count];
        for(int i = 0; i < count; i++) {
            res[i] = readU1();
        }
        return res;
    }

    public int readU2() throws IOException {
        needBytes(2);
        int first = mStream.read();
        int second = mStream.read();
        return (first << 8) | second;
    }

    public int[] readU2Array(int count) throws IOException {
        needBytes(count * 2);
        int[] res = new int[count];
        for(int i = 0; i < count; i++) {
            res[i] = readU2();
        }
        return res;
    }

    public long readU4() throws IOException {
        needBytes(4);
        int first = mStream.read();
        int second = mStream.read();
        int third = mStream.read();
        int fourth = mStream.read();
        return (first << 24) | (second << 16) | (third << 8) | fourth;
    }

    public AID readAID() throws IOException {
        int aidLen = readU1();
        return new AID(readBytes(aidLen));
    }

    public String readString() throws IOException {
        int strLen = readU1();
        return new String(readBytes(strLen), StandardCharsets.UTF_8);
    }

    public
    <S extends CapStructure>
    S readStructure(S instance) throws IOException {
        instance.read(this);
        return instance;
    }

    public
    <S extends CapStructure>
    S readStructure(Class<S> structureClass) throws IOException {
        try {
            S instance = structureClass.newInstance();
            readStructure(instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    public
    <S extends CapStructure>
    ArrayList<S> readStructureArray(int count, Class<S> structureClass) throws IOException {
        ArrayList<S> res = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            res.add(readStructure(structureClass));
        }
        return res;
    }

    public CapClassRef readClassRef() throws IOException {
        return readStructure(CapClassRef.class);
    }

    public CapMethodRef readMethodRef() throws IOException {
        return readStructure(CapMethodRef.class);
    }

}
