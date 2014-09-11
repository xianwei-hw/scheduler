/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.safeplace;

import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Specification {

    private List<Constraint> cstrs;

    public Specification(List<Constraint> cstrs) {
        this.cstrs = cstrs;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder();
        for (Constraint c : cstrs) {
            b.append(c.pretty()).append('\n');
        }
        return b.toString();
    }

    public List<Constraint> getConstraints() {
        return cstrs;
    }

    public Constraint get(String id) {
        for (Constraint c : cstrs) {
            if (c.id().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        Iterator<Constraint> ite = cstrs.iterator();
        while (ite.hasNext()) {
            b.append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.toString();
    }
}
