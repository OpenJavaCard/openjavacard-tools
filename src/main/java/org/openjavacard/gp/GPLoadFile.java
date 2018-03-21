/*
 *  openjavacard-tools: OpenJavaCard development tools
 *  Copyright (C) 2018  Ingo Albrecht (prom@berlin.ccc.de)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package org.openjavacard.gp;

import org.openjavacard.iso.AID;

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
 * {@link org.openjavacard.cap.CapPackage}.
 */
public class GPLoadFile {

    /** AID of the package */
    AID mPackageAID;

    /** Maximum size among all blocks */
    int mBlockSize = 0;
    /** Total size of the load file */
    int mTotalSize = 0;

    /** List of the actual blocks */
    List<byte[]> mBlocks = new ArrayList<>();

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

}
