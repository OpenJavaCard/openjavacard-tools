package better.smartcard.cap;

public class CapComponent {

    CapComponentType mType;

    byte[] mData;

    CapComponent(CapComponentType type, byte[] data) {
        mType = type;
        mData = data;
    }

    public String getName() {
        return mType.name();
    }

    public String getFilename() {
        return mType.filename();
    }

    public CapComponentType getType() {
        return mType;
    }

    public byte[] getData() {
        return mData;
    }

    public int getSize() {
        return mData.length;
    }

}
