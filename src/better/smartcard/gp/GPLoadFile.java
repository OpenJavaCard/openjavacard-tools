package better.smartcard.gp;

import better.smartcard.util.AID;

import java.util.ArrayList;
import java.util.List;

public class GPLoadFile {

    AID mPackageAID;

    int mBlockSize = 0;
    int mTotalSize = 0;

    List<byte[]> mBlocks = new ArrayList<>();

    public GPLoadFile(AID packageAID) {
        mPackageAID = packageAID;
    }

    public AID getPackageAID() {
        return mPackageAID;
    }

    public int getBlockSize() {
        return mBlockSize;
    }

    public int getTotalSize() {
        return mTotalSize;
    }

    public int getNumBlocks() {
        return mBlocks.size();
    }

    public byte[] getBlock(int index) {
        return mBlocks.get(index);
    }

    public List<byte[]> getBlocks() {
        return mBlocks;
    }

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
