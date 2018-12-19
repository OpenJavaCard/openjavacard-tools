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

package org.openjavacard.cap.test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CapTestFiles {

    private static final String[] FILES = new String[] {
            "openjavacard-lib-ber.cap",
            "openjavacard-lib-debug.cap",
            "openjavacard-lib-fortuna.cap",
            "openjavacard-app-demo.cap",
    };

    public static List<InputStream> getFiles() {
        Class cls = CapTestFiles.class;
        ArrayList<InputStream> streams = new ArrayList<>();
        for(String fileName: FILES) {
            InputStream stream = cls.getResourceAsStream(fileName);
            streams.add(stream);
        }
        return streams;
    }

}
