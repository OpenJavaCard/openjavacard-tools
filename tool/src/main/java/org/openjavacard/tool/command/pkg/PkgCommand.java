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

package org.openjavacard.tool.command.pkg;

import org.openjavacard.gp.client.GPCard;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackageContext;
import org.openjavacard.packaging.manager.OJCPackageManager;
import org.openjavacard.tool.command.gp.GPCommand;

import javax.smartcardio.CardException;
import java.io.PrintStream;

public abstract class PkgCommand extends GPCommand {

    OJCPackageContext mPkgContext;

    public PkgCommand(GPContext context) {
        super(context);
    }

    @Override
    public void run() {
        initializePackaging();
        super.run();
    }

    private void initializePackaging() {
        PrintStream os = System.out;
        os.println("INITIALIZE PACKAGING");
        os.println();
        mPkgContext = new OJCPackageContext();
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        OJCPackageManager manager = new OJCPackageManager(mPkgContext, card);
        performOperation(manager);
    }

    protected abstract void performOperation(OJCPackageManager manager) throws CardException;

}
