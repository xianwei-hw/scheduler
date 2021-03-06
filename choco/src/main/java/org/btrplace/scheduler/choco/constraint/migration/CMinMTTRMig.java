/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.constraint.CObjective;
import org.btrplace.scheduler.choco.constraint.mttr.MovementGraph;
import org.btrplace.scheduler.choco.constraint.mttr.MyInputOrder;
import org.btrplace.scheduler.choco.constraint.mttr.OnStableNodeFirst;
import org.btrplace.scheduler.choco.constraint.mttr.StartOnLeafNodes;
import org.btrplace.scheduler.choco.transition.BootableNode;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.ShutdownableNode;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An objective that minimizes the time to repair a non-viable model involving a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class CMinMTTRMig implements CObjective {

    private ReconfigurationProblem rp;
    private List<Constraint> costConstraints;
    private boolean costActivated = false;

    /**
     * Make a new Objective.
     */
    public CMinMTTRMig(@SuppressWarnings("unused") MinMTTRMig m) {
        costConstraints = new ArrayList<>();
    }

    public CMinMTTRMig() {
        this(null);
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        this.rp = rp;
        List<IntVar> endVars = new ArrayList<>();

        // Define the cost constraint: sum of all actions' end time
        for (VMTransition m : rp.getVMActions()) {
            endVars.add(m.getEnd());
        }
        for (NodeTransition m : rp.getNodeActions()) {
            endVars.add(m.getEnd());
        }
        IntVar[] costs = endVars.toArray(new IntVar[endVars.size()]);
        IntVar cost = rp.getModel().intVar(rp.makeVarLabel("costEndVars"), 0, Integer.MAX_VALUE / 100, true);
        costConstraints.add(rp.getModel().sum(costs, "=", cost));

        // Set the objective, minimize the cost
        rp.setObjective(true, cost);

        // Inject the scheduling heuristic
        injectSchedulingHeuristic(cost);

        // Post the cost constraint
        postCostConstraints();

        return true;
    }

    /**
     * Inject a specific scheduling heuristic to the solver.
     *
     * @param cost the global cost variable.
     */
    private void injectSchedulingHeuristic(IntVar cost) {

        // Init a list of strategies
        List<AbstractStrategy<?>> strategies = new ArrayList<>();

        // Init a list of vars
        List<IntVar> endVars = new ArrayList<>();
        
        // Boot nodes
        for (Node n : rp.getNodes()) {
            if (rp.getNodeAction(n) instanceof BootableNode) {
                endVars.add(rp.getNodeAction(n).getEnd());
            }
        }
        if (!endVars.isEmpty()) {
            strategies.add(Search.intVarSearch(
                    new FirstFail(rp.getModel()),
                    new IntDomainMin(),
                    DecisionOperatorFactory.makeIntSplit(), // Split from max
                    endVars.toArray(new IntVar[endVars.size()])
            ));
        }
        endVars.clear();

        // Migrate VMs
        MovementGraph gr = new MovementGraph(rp);
        OnStableNodeFirst schedHeuristic = new OnStableNodeFirst(rp);
        Stream<Slice> s = rp.getVMActions().stream().map(VMTransition::getDSlice).filter(Objects::nonNull);
        IntVar[] starts = s.map(Slice::getStart).toArray(IntVar[]::new);
        strategies.add(new IntStrategy(starts, new StartOnLeafNodes(rp, gr), new IntDomainMin()));
        strategies.add(new IntStrategy(schedHeuristic.getScope(), schedHeuristic, new IntDomainMin()));

        // Add remaining VMs actions
        for (VMTransition a : rp.getVMActions()) {
            endVars.add(a.getEnd());
        }
        if (!endVars.isEmpty()) {
            strategies.add(Search.intVarSearch(
                    new FirstFail(rp.getModel()),
                    new IntDomainMin(),
                    DecisionOperatorFactory.makeIntSplit(), // Split from max
                    endVars.toArray(new IntVar[endVars.size()])
            ));
        }
        endVars.clear();

        // Shutdown nodes
        for (Node n : rp.getNodes()) {
            if (rp.getNodeAction(n) instanceof ShutdownableNode) {
                endVars.add(rp.getNodeAction(n).getEnd());
            }
        }
        if (!endVars.isEmpty()) {
            strategies.add(Search.intVarSearch(
                    new FirstFail(rp.getModel()),
                    new IntDomainMin(),
                    DecisionOperatorFactory.makeIntSplit(),
                    endVars.toArray(new IntVar[endVars.size()])
            ));
        }

        // Set the strategies in the correct order (as added before)
        strategies.add(new IntStrategy(new IntVar[]{rp.getEnd(), cost}, new MyInputOrder<>(rp.getSolver(), this), new IntDomainMin()));

        // Add all defined strategies
        rp.getSolver().setSearch(
                new StrategiesSequencer(
                        rp.getModel().getEnvironment(),
                        strategies.toArray(new AbstractStrategy[strategies.size()])
                )
        );
    }

    @Override
    public void postCostConstraints() {
        //TODO: Delay insertion ?
        if (!costActivated) {
            rp.getLogger().debug("Post the cost-oriented constraints");
            costActivated = true;
            Model s = rp.getModel();
            costConstraints.forEach(s::post);
        }
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }
}
