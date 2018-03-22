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

package org.openjavacard.iso;

import org.openjavacard.util.HexUtil;

import javax.smartcardio.CardException;

public class SWException extends CardException {

    final int mSWCode;

    final SWInfo mSWData;

    public SWException(String message, int sw) {
        super(message);
        mSWCode = sw;
        mSWData = SWInfo.get(sw);
    }

    public SWException(int sw) {
        this(null, sw);
    }

    public int getCode() {
        return mSWCode;
    }

    public SWInfo getInfo() {
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
