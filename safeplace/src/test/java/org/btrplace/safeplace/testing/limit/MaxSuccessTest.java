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

package org.btrplace.safeplace.testing.limit;

import org.btrplace.safeplace.testing.Result;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class MaxSuccessTest {

    @Test
    public void test() {
        MaxSuccess m = new MaxSuccess(2);
        Assert.assertEquals(m.test(Result.CRASH), true);
        Assert.assertEquals(m.test(Result.SUCCESS), true);
        Assert.assertEquals(m.test(Result.SUCCESS), false);
        Assert.assertEquals(m.test(Result.SUCCESS), false);
        Assert.assertEquals(m.test(Result.OVER_FILTERING), false);
        Assert.assertEquals(m.test(Result.SUCCESS), false);
    }
}