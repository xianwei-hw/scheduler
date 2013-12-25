package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetPlus extends Plus<Set<?>> {

    public SetPlus(Term<Set<?>> t1, Term<Set<?>> t2) {
        super(t1, t2);
    }

    @Override
    public Set eval(Model mo) {
        Set o1 = a.eval(mo);
        Set o2 = b.eval(mo);
        Set<?> l = new HashSet(o1);
        l.addAll(o2);
        return l;
    }
}
