package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.ListType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Lists extends Function<java.util.List> {

    @Override
    public Type type() {
        return new SetType(new ListType(null));
    }


    @Override
    public java.util.List eval(SpecModel mo, java.util.List<Object> args) {
        Collection c = (Collection) args.get(0);
        if (c == null) {
            return null;
        }
        Set<ArrayList> s = new HashSet<>();
        return new ArrayList<>(c);
    }

    @Override
    public String id() {
        return "lists";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(null)};
    }

    @Override
    public Type type(List<Term> args) {
        return new SetType(new ListType(args.get(0).type().inside()));
    }

}
