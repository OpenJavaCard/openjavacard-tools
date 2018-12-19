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

package org.openjavacard.tool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.openjavacard.gp.client.GPContext;
import org.openjavacard.packaging.manager.OJCPackage;
import org.openjavacard.packaging.manager.OJCPackageManager;
import org.openjavacard.packaging.model.OJCPackageInfo;

import javax.smartcardio.CardException;

@Parameters(
        commandNames = "pkg-remove",
        commandDescription = "Packages: Remove a package from the card"
)
public class PkgRemove extends PkgCommand {

    @Parameter(
            required = true
    )
    String packageName;

    public PkgRemove(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(OJCPackageManager manager) throws CardException {
        OJCPackage info = manager.findPackageByName(packageName);
    }

}
