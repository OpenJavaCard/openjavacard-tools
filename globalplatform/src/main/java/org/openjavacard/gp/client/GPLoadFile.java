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

package org.openjavacard.gp.client;

import org.openjavacard.cap.file.CapComponentType;
import org.openjavacard.cap.file.CapFileComponent;
import org.openjavacard.cap.file.CapFilePackage;
import org.openjavacard.iso.AID;
import org.openjavacard.tlv.TLVLength;
import org.openjavacard.tlv.TLVTag;
import org.openjavacard.util.ArrayUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a CAP file prepared for loading
 * <p/>
 * This contains CAP file components in load order,
 * split into blocks no bigger than indicated.
 * <p/>
 * Once loaded each of these will become an ELF on the card.
 * <p/>
 * These objects can be produced using methods in
 * {@link CapFilePackage}.
 */
public class GPLoadFile {

    /** AID of the package */
    private final AID mPackageAID;

    /** Maximum size among all blocks */
    private int mBlockSize = 0;
    /** Total size of the load file */
    private int mTotalSize = 0;

    /** List of the actual blocks */
    private final List<byte[]> mBlocks = new ArrayList<>();

    /** Construct a new load file */
    public GPLoadFile(AID packageAID) {
        mPackageAID = packageAID;
    }

    /** @return the AID of the package contained in this load file */
    public AID getPackageAID() {
        return mPackageAID;
    }

    /** @return the maximum block size (in bytes) of this load file */
    public int getBlockSize() {
        return mBlockSize;
    }

    /** @return the total size (in bytes) of this load file */
    public int getTotalSize() {
        return mTotalSize;
    }

    /** @return the number of blocks in this load file */
    public int getNumBlocks() {
        return mBlocks.size();
    }

    /** @return the blocks comprising this load file */
    public List<byte[]> getBlocks() {
        return mBlocks;
    }

    /**
     * Add a block to the load file
     * <p/>
     * Use with care! Maximum block size will be adjusted!
     * <p/>
     * @param block to add
     */
    public void addBlock(byte[] block) {
        int length = block.length;
        // keep total size
        mTotalSize += length;
        // adjust block size
        if(length > mBlockSize) {
            mBlockSize = length;
        }
        // add the block
        mBlocks.add(block);
    }


    /**
     * Generate a combined load file
     * @param blockSize for the file
     * @return a GPLoadFile
     */
    public static GPLoadFile generateCombinedLoadFile(CapFilePackage capFilePackage, int blockSize) {
        GPLoadFile res = new GPLoadFile(capFilePackage.getPackageAID());
        try {
            // need to know total length
            int totalSize = 0;
            // need to know components to emit
            List<CapFileComponent> components = new ArrayList<>();

            // find components in load order
            for (CapComponentType type : CapComponentType.LOAD_ORDER) {
                CapFileComponent component = capFilePackage.getComponentByType(type);
                // if we have a component of the given type
                if (component != null) {
                    byte[] data = component.getData();
                    // add up the total size
                    totalSize += data.length;
                    // remember the components
                    components.add(component);
                }
            }

            // emit one tag with all the components
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(TLVTag.tagBytes(0xC400));
            bos.write(TLVLength.lengthBytes(totalSize));
            for(CapFileComponent component : components) {
                byte[] data = component.getData();
                bos.write(data);
            }

            // get ourselves an array with all the data
            byte[] raw = bos.toByteArray();

            // split the result into appropriate blocks
            byte[][] blocks = ArrayUtil.splitBlocks(raw, blockSize);

            // add the blocks to the load file
            for(byte[] block: blocks) {
                res.addBlock(block);
            }
        } catch (IOException e) {
            throw new Error("Error generating load file", e);
        }
        return res;
    }

}
