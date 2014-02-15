/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco;


/**
 * A class to provide a method to customize the optimisation process of the solver.
 * Instead of trying all the possible values, you will be able to choose the value to try
 * for the objective variable. This may speed up the improvement phase.
 * <p/>
 * As the solver will not longer try all the possible values, the completeness of
 * the resolution process is no longer guarantee and may remove some (possibly good) solutions.
 *
 * @author Fabien Hermenier
 */
public interface ObjectiveAlterer {

    /**
     * compute the offset to apply to the best value computed so far
     * The new value to try will be the last computed value plus the offset.
     *
     * @param currentValue the current value of the objective
     * @return the offset to add on this value to state the new objective.
     */
    int offset(ReconfigurationProblem rp, int currentValue);

}
