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

package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.*;

/**
 * Checker for the {@link btrplace.model.constraint.CumulatedResourceCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.CumulatedResourceCapacity
 */
public class CumulatedResourceCapacityChecker extends AllowAllConstraintChecker<CumulatedResourceCapacity> {

    ShareableResource rc;

    private int free;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public CumulatedResourceCapacityChecker(CumulatedResourceCapacity s) {
        super(s);
    }

    private boolean leave(int amount, int n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            free += amount;
        }
        return true;
    }

    private boolean arrive(int amount, int n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            free -= amount;
            if (free < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(rc.getVMConsumption(a.getVM()), a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (getConstraint().isContinuous()/* && srcRunnings.remove(a.getVM())*/) {
            return leave(rc.getVMConsumption(a.getVM()), a.getNode());
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getConstraint().isContinuous()) {
            if (!(getNodes().contains(a.getSourceNode()) && getNodes().contains(a.getDestinationNode()))) {
                return leave(rc.getVMConsumption(a.getVM()), a.getSourceNode()) && arrive(rc.getVMConsumption(a.getVM()), a.getDestinationNode());
            }
        }
        return true;
    }

    @Override
    public boolean start(ResumeVM a) {
        return arrive(rc.getVMConsumption(a.getVM()), a.getDestinationNode());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return leave(rc.getVMConsumption(a.getVM()), a.getNode());
    }

    @Override
    public boolean start(SuspendVM a) {
        return leave(rc.getVMConsumption(a.getVM()), a.getSourceNode());
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            rc = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + getConstraint().getResource());
            free = getConstraint().getAmount();
            Mapping map = mo.getMapping();
            for (int n : getNodes()) {
                free -= rc.sum(map.getRunningVMs(n), true, true);
                if (free < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean start(Allocate e) {
        return arrive(rc.getVMConsumption(e.getVM()), e.getHost());
    }

    @Override
    public boolean consume(AllocateEvent e) {
        //TODO: Get its current location to check if it is on a node in the constraint
        return true;
    }

    @Override
    public boolean endsWith(Model i) {
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + getConstraint().getResource());
        if (rc == null) {
            return false;
        }

        int remainder = getConstraint().getAmount();
        for (int id : getNodes()) {
            if (i.getMapping().getOnlineNodes().contains(id)) {
                remainder -= rc.sum(i.getMapping().getRunningVMs(id), true, true);
                if (remainder < 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
