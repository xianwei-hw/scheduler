package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Constant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeStateType implements Type {

    public static enum Type {online, offline}

    private static NodeStateType instance = new NodeStateType();

    private Set<Constant> vals;

    private NodeStateType() {
        Set<Constant> s = new HashSet<>();
        for (Type t : Type.values()) {
            s.add(new Constant(t, this));
        }
        vals = Collections.unmodifiableSet(s);
    }

    public static NodeStateType getInstance() {
        return instance;
    }

    @Override
    public Set<Constant> domain(Model mo) {
        return vals;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Type.valueOf(n);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public String label() {
        return "nodeState";
    }

    @Override
    public Constant newValue(String n) {
        return new Constant(Type.valueOf(n), this);
    }

}
