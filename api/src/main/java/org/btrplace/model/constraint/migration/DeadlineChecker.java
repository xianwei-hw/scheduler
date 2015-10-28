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

package org.btrplace.model.constraint.migration;

import org.btrplace.model.constraint.AllowAllConstraintChecker;

/**
 * Checker for the {@link Deadline} constraint.
 *
 * @author Vincent Kherbache
 * @see Deadline
 */
public class DeadlineChecker extends AllowAllConstraintChecker<Deadline> {

    /**
     * Make a new checker.
     *
     * @param dl the deadline constraint associated to the checker.
     */
    public DeadlineChecker(Deadline dl) {
        super(dl);
    }
}