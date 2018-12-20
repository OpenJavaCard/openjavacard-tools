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

package org.openjavacard.cap.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CapVersion {

    public static final CapVersion CAP21 = new CapVersion(2, 1);
    public static final CapVersion CAP22 = new CapVersion(2, 2);

    public final int major;
    public final int minor;

    public CapVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public String toString() {
        return major + "." + minor;
    }

}
