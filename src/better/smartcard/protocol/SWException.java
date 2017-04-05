package better.smartcard.protocol;

import better.smartcard.util.HexUtil;

import javax.smartcardio.CardException;

public class SWException extends CardException {

    int mSWCode;

    SW mSWData;

    public SWException(String message, int sw) {
        super(message);
        mSWCode = sw;
        mSWData = SW.get(sw);
    }

    public int getCode() {
        return mSWCode;
    }

    public SW getData() {
        return mSWData;
    }

    public String getName() {
        return SW.toString(mSWCode);
    }

    @Override
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        String message = super.getMessage();
        if (message != null) {
            sb.append(message);
            sb.append(" (");
        } else {
            sb.append("Card returned ");
        }
        sb.append("SW " + HexUtil.hex16(mSWCode));
        if (mSWData != null) {
            sb.append(" - ");
            sb.append(mSWData.name);
        }
        if (message != null) {
            sb.append(")");
        }
        return sb.toString();
    }

}
