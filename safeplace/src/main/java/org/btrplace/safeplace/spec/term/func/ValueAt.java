/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.ColType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.List;

/**
 * Get the value at a given index of a list.
 *
 * @author Fabien Hermenier
 */
public class ValueAt implements Term {

    private Term<List<?>> arr;
    private Term<Integer> idx;

    public ValueAt(Term<List<?>> arr, Term<Integer> idx) {
        this.arr = arr;
        this.idx = idx;
    }

    @Override
    public Type type() {
        return ((ColType) arr.type()).inside();
    }

    @Override
    public Object eval(Context mo, Object... args) {
        List<?> l = arr.eval(mo);
        if (l == null) {
            return null;
        }
        Integer i = idx.eval(mo);
        if (i == null) {
            return null;
        }
        return l.get(i);
    }

    @Override
    public String toString() {
        return arr.toString() + "[" + idx.toString() + "]";
    }
}
