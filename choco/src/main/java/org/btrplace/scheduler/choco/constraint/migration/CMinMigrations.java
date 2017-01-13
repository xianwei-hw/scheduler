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
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMigrations;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.constraint.CObjective;
import org.btrplace.scheduler.choco.constraint.mttr.HostingVariableSelector;
import org.btrplace.scheduler.choco.constraint.mttr.MovementGraph;
import org.btrplace.scheduler.choco.constraint.mttr.MyInputOrder;
import org.btrplace.scheduler.choco.constraint.mttr.OnStableNodeFirst;
import org.btrplace.scheduler.choco.constraint.mttr.RandomVMPlacement;
import org.btrplace.scheduler.choco.constraint.mttr.StartOnLeafNodes;
import org.btrplace.scheduler.choco.constraint.mttr.VMPlacementUtils;
import org.btrplace.scheduler.choco.constraint.mttr.WorstFit;
import org.btrplace.scheduler.choco.constraint.mttr.load.BiggestDimension;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.Transition;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements {@link MinMigrations}.
 * Currently, same heuristics that with MinMttr but a different objective
 *
 * @author Fabien Hermenier
 */
public class CMinMigrations implements CObjective {

    private boolean costActivated = false;

    private boolean useResources = false;

    private ReconfigurationProblem rp;

    private IntVar cost;

    /**
     * Make a new objective.
     */
    public CMinMigrations(MinMigrations m) {
    }

    public CMinMigrations() {
        this(null);
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem p) throws SchedulerException {
        this.rp = p;
        costActivated = false;

        cost = rp.getModel().intVar(rp.makeVarLabel("#migs"), 0, Integer.MAX_VALUE / 100, true);
        rp.setObjective(true, cost);
        injectPlacementHeuristic(p, ps, cost);
        postCostConstraints();
        return true;
    }

    private void injectPlacementHeuristic(ReconfigurationProblem p, Parameters ps, IntVar cost) {

        Model mo = p.getSourceModel();
        Mapping map = mo.getMapping();

        OnStableNodeFirst schedHeuristic = new OnStableNodeFirst(p);

        //Get the VMs to place
        Set<VM> onBadNodes = new HashSet<>(p.getManageableVMs());

        //Get the VMs that runs and have a pretty low chances to move
        Set<VM> onGoodNodes = map.getRunningVMs(map.getOnlineNodes());
        onGoodNodes.removeAll(onBadNodes);

        List<VMTransition> goodActions = p.getVMActions(onGoodNodes);
        List<VMTransition> badActions = p.getVMActions(onBadNodes);

        Solver s = p.getSolver();

        //Get the VMs to move for exclusion issue
        Set<VM> vmsToExclude = new HashSet<>(p.getManageableVMs());
        for (Iterator<VM> ite = vmsToExclude.iterator(); ite.hasNext(); ) {
            VM vm = ite.next();
            if (!(map.isRunning(vm) && p.getFutureRunningVMs().contains(vm))) {
                ite.remove();
            }
        }
        List<AbstractStrategy<?>> strategies = new ArrayList<>();

        Map<IntVar, VM> pla = VMPlacementUtils.makePlacementMap(p);
        if (!vmsToExclude.isEmpty()) {
            List<VMTransition> actions = new LinkedList<>();
            //Get all the involved slices
            for (VM vm : vmsToExclude) {
                if (p.getFutureRunningVMs().contains(vm)) {
                    actions.add(p.getVMAction(vm));
                }
            }

            placeVMs(ps, strategies, actions, schedHeuristic, pla);
        }

        List<CShareableResource> rcs = rp.getSourceModel().getViews().stream()
                .filter(v -> v instanceof ShareableResource)
                .map(v -> (CShareableResource) rp.getView(v.getIdentifier()))
                .collect(Collectors.toList());
        useResources = !rcs.isEmpty();

        Map<VM, Integer> costs = CShareableResource.getWeights(rp, rcs);
        badActions.sort((v2, v1) -> costs.get(v1.getVM()) - costs.get(v2.getVM()));
        goodActions.sort((v2, v1) -> costs.get(v1.getVM()) - costs.get(v2.getVM()));
        placeVMs(ps, strategies, badActions, schedHeuristic, pla);
        placeVMs(ps, strategies, goodActions, schedHeuristic, pla);

        //Reinstantations. Try to reinstantiate first
        List<IntVar> migs = new ArrayList<>();
        for (VMTransition t : rp.getVMActions()) {
            if (t instanceof RelocatableVM) {
                migs.add(((RelocatableVM) t).getRelocationMethod());
            }
        }
        strategies.add(
                Search.intVarSearch(
                        new FirstFail(rp.getModel()), new IntDomainMax(), migs.toArray(new IntVar[migs.size()]))
        );


        if (!p.getNodeActions().isEmpty()) {
            //Boot some nodes if needed
            IntVar[] starts = p.getNodeActions().stream().map(Transition::getStart).toArray(IntVar[]::new);
            strategies.add(new IntStrategy(starts, new FirstFail(rp.getModel()), new IntDomainMin()));
        }

        ///SCHEDULING PROBLEM
        MovementGraph gr = new MovementGraph(rp);
        IntVar[] starts = dSlices(rp.getVMActions()).map(Slice::getStart).filter(v -> !v.isInstantiated()).toArray(IntVar[]::new);
        strategies.add(new IntStrategy(starts, new StartOnLeafNodes(rp, gr), new IntDomainMin()));
        strategies.add(new IntStrategy(schedHeuristic.getScope(), schedHeuristic, new IntDomainMin()));

        IntVar[] ends = rp.getVMActions().stream().map(Transition::getEnd).filter(v -> !v.isInstantiated()).toArray(IntVar[]::new);
        strategies.add(Search.intVarSearch(new MyInputOrder<>(s), new IntDomainMin(), ends));

        //At this stage only it matters to plug the cost constraints
        strategies.add(new IntStrategy(new IntVar[]{p.getEnd(), cost}, new MyInputOrder<>(s, this), new IntDomainMin()));

        s.setSearch(new StrategiesSequencer(s.getEnvironment(), strategies.toArray(new AbstractStrategy[strategies.size()])));
    }

    /*
     * Try to place the VMs associated on the actions in a random node while trying first to stay on the current node
     */
    private void placeVMs(Parameters ps, List<AbstractStrategy<?>> strategies, List<VMTransition> actions, OnStableNodeFirst schedHeuristic, Map<IntVar, VM> map) {
        IntValueSelector rnd = new WorstFit(map, rp, new BiggestDimension());
        if (!useResources) {
            rnd = new RandomVMPlacement(rp, map, true, ps.getRandomSeed());
        }
        if (!actions.isEmpty()) {
            IntVar[] hosts = dSlices(actions).map(Slice::getHoster).filter(v -> !v.isInstantiated()).toArray(IntVar[]::new);
            if (hosts.length > 0) {
                strategies.add(new IntStrategy(hosts, new HostingVariableSelector(rp.getModel(), schedHeuristic), rnd));
            }
        }
    }


    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }

    private static Stream<Slice> dSlices(List<VMTransition> l) {
        return l.stream().map(VMTransition::getDSlice).filter(Objects::nonNull);
    }

    @Override
    public void postCostConstraints() {
        if (!costActivated) {
            costActivated = true;
            rp.getLogger().debug("Post the cost-oriented constraints");
            List<IntVar> stays = new ArrayList<>();
            for (VMTransition t : rp.getVMActions()) {
                if (t instanceof RelocatableVM && rp.getManageableVMs().contains(t.getVM())) {
                    stays.add(t.getDuration());
                }
            }
            rp.getModel().post(rp.getModel().sum(stays.toArray(new IntVar[stays.size()]), "=", cost));
        }
    }

    @Override
    public String toString() {
        return "minMigrations()";
    }
}