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

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import btrplace.model.Instance;
import btrplace.model.Node;
import btrplace.model.constraint.Quarantine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splitter for {@link btrplace.model.constraint.Quarantine} constraints.
 * <p/>
 * When the constraint focuses nodes among different partitions,
 * the constraint is splitted.
 * <p/>
 * This operation is conservative wrt. the constraint semantic.
 *
 * @author Fabien Hermenier
 */
public class QuarantineSplitter implements ConstraintSplitter<Quarantine> {

    @Override
    public Class<Quarantine> getKey() {
        return Quarantine.class;
    }

    @Override
    public boolean split(Quarantine cstr, List<Instance> partitions) {
        Set<Node> nodes = new HashSet<>(cstr.getInvolvedNodes());
        for (Instance i : partitions) {
            Set<Node> all = i.getModel().getNodes();
            Set<Node> in = Splitters.extractInside(nodes, all);
            if (!in.isEmpty()) {
                i.getConstraints().add(new Quarantine(in));
            }
            if (nodes.isEmpty()) {
                break;
            }
        }
        return true;
    }
}
