package btrplace.solver.api.cstrSpec.test;

import btrplace.solver.api.cstrSpec.annotations.CstrTest;
import btrplace.solver.api.cstrSpec.runner.CTestCasesRunner;

/**
 * @author Fabien Hermenier
 */
public class TestBan {

    @CstrTest(constraint = "ban", groups = {"vm2pm"})
    public void testDiscrete(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000);
    }

    @CstrTest(constraint = "ban", groups = {"vm2pm"})
    public void testDiscreteRepair(CTestCasesRunner r) {
        r.discrete().timeout(5).maxTests(1000).impl().repair(true);
    }
}
