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

package btrplace.safeplace.verification.spec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SpecModel {

    private SpecMapping sm;

    private Map<String, VerifDomain> vDoms;

    private Model mo;

    private ReconfigurationPlan plan;

    //private Map<String, Object> vars;

    private LinkedList<Map<String, Object>> stack;

    public SpecModel() {
        this(new DefaultModel());
    }

    public ReconfigurationPlan getPlan() {
        return plan;
    }

    public SpecModel(Model mo) {
        this.mo = mo;
        sm = new SpecMapping(mo.getMapping());
        vDoms = new HashMap<>();
        //this.vars = new HashMap<>();
        stack = new LinkedList<>();
        stack.add(new HashMap<String, Object>());
    }

    public Model getModel() {
        return mo;
    }

    public SpecMapping getMapping() {
        return sm;
    }

    public void setValue(String label, Object o) {
        stack.getFirst().put(label, o);
        //vars.put(label, o);
    }

    public Object getValue(String label) {
        return stack.getFirst().get(label);
        /*Object o = vars.get(label);
        if (o == null) {
            throw new RuntimeException("No value for " + label);
        }
        return o;*/
    }

    public void add(VerifDomain d) {
        vDoms.put(d.type(), d);
    }

    public Set getVerifDomain(String lbl) {
        VerifDomain v = vDoms.get(lbl);
        if (v == null) {
            return null;
        }
        return v.domain();
    }

    @Override
    public String toString() {
        return stack.toString();
        //return vars.toString();
    }

    public void saveStack() {
        stack.push(new HashMap<String, Object>());
    }

    public void restoreStack() {
        stack.pop();
    }
}
