/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
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

package org.openjavacard.tool.command.aid;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.iso.AID;

import java.io.PrintStream;

@Parameters(
        commandNames = "aid-info",
        commandDescription = "AID: Show information about an AID"
)
public class AIDInfo implements Runnable {

    @Parameter(
            required = true,
            description = "AID"
    )
    private AID aid;

    @Override
    public void run() {
        PrintStream os = System.out;

        os.println("AID: " + aid);

        org.openjavacard.iso.AIDInfo info =
            org.openjavacard.iso.AIDInfo.get(aid);

        if(info != null) {

        }
    }

}
