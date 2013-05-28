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

package btrplace.model.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.ShutdownVM;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link Overbook}.
 *
 * @author Fabien Hermenier
 */
public class OverbookTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));
        Overbook o = new Overbook(s, "foo", 1.5);
        Assert.assertNotNull(o.getChecker());
        Assert.assertEquals(s, o.getInvolvedNodes());
        Assert.assertEquals("foo", o.getResource());
        Assert.assertTrue(o.getInvolvedVMs().isEmpty());
        Assert.assertEquals(1.5, o.getRatio());
        Assert.assertNotNull(o.toString());
        Assert.assertTrue(o.isContinuous());
        Assert.assertTrue(o.setContinuous(false));
        Assert.assertFalse(o.isContinuous());
        System.out.println(o);

        o = new Overbook(s, "foo", 1.5, true);
        Assert.assertTrue(o.isContinuous());
    }

    @Test
    public void testDiscreteIsSatisfied() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));

        Model i = new DefaultModel();
        Mapping cfg = i.getMapping();
        cfg.addOnlineNode(n1);
        cfg.addOnlineNode(n2);

        ShareableResource rc = new ShareableResource("cpu");
        rc.setNodeCapacity(n1, 1);
        rc.setNodeCapacity(n2, 4);

        rc.setVMConsumption(vm1, 2);
        rc.setVMConsumption(vm2, 2);
        rc.setVMConsumption(vm3, 4);

        cfg.addRunningVM(vm1, n1);
        cfg.addRunningVM(vm2, n2);
        cfg.addRunningVM(vm3, n2);
        cfg.addRunningVM(vm4, n2);

        i.attach(rc);

        Overbook o = new Overbook(s, "cpu", 2);
        Assert.assertEquals(o.isSatisfied(i), true);

        rc.setVMConsumption(vm1, 4);
        Assert.assertEquals(o.isSatisfied(i), false);

        cfg.addRunningVM(vm1, n2);
        Assert.assertEquals(o.isSatisfied(i), false);

        Overbook o2 = new Overbook(s, "mem", 2);
        Assert.assertEquals(o2.isSatisfied(i), false);
    }

    @Test
    public void testContinuousIsSatisfied() {
        Set<Integer> s = new HashSet<>(Arrays.asList(n1, n2));

        Model i = new DefaultModel();
        Mapping cfg = i.getMapping();
        cfg.addOnlineNode(n1);
        cfg.addOnlineNode(n2);

        ShareableResource rc = new ShareableResource("cpu");
        rc.setNodeCapacity(n1, 1);
        rc.setNodeCapacity(n2, 4);

        rc.setVMConsumption(vm1, 2);
        rc.setVMConsumption(vm2, 2);
        rc.setVMConsumption(vm3, 4);

        cfg.addRunningVM(vm1, n1);
        cfg.addRunningVM(vm2, n2);
        cfg.addRunningVM(vm3, n2);
        cfg.addRunningVM(vm4, n2);

        i.attach(rc);

        Overbook o = new Overbook(s, "cpu", 2);
        o.setContinuous(true);
        ReconfigurationPlan p = new DefaultReconfigurationPlan(i);
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vm1, n1, "cpu", 1, 2, 5));
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vm2, n2, "cpu", 5, 2, 5));
        Assert.assertEquals(o.isSatisfied(p), false);

        p.add(new Allocate(vm3, n2, "cpu", 2, 0, 1));
        Assert.assertEquals(o.isSatisfied(p), true);

        p.add(new Allocate(vm4, n2, "cpu", 3, 4, 6));
        Assert.assertEquals(o.isSatisfied(p), false);

        p.add(new ShutdownVM(vm3, n2, 2, 3));

        Assert.assertEquals(o.isSatisfied(p), true);
    }

    @Test
    public void testEquals() {
        Set<Integer> x = new HashSet<>(Arrays.asList(n1, n2));
        Overbook s = new Overbook(x, "foo", 3);

        Assert.assertTrue(s.equals(s));
        Overbook o2 = new Overbook(x, "foo", 3);
        Assert.assertTrue(o2.equals(s));
        Assert.assertEquals(o2.hashCode(), s.hashCode());
        Assert.assertFalse(new Overbook(x, "bar", 3).equals(s));
        Assert.assertFalse(new Overbook(x, "foo", 2).equals(s));
        x = new HashSet<>(Arrays.asList(n3));
        Assert.assertFalse(new Overbook(x, "foo", 3).equals(s));
    }
}
