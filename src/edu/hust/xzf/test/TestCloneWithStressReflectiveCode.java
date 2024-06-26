package edu.hust.xzf.test;

/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8284951
 * @summary Test clone intrinsic with StressReflectiveCode.
 * @requires vm.debug
 * @run main/othervm -XX:-InlineUnsafeOps -XX:-ReduceInitialCardMarks -XX:+StressReflectiveCode -Xbatch -XX:-TieredCompilation
 *                   -XX:CompileCommand=dontinline,compiler.arraycopy.TestCloneWithStressReflectiveCode::test
 *                   compiler.arraycopy.TestCloneWithStressReflectiveCode
 * @run main/othervm -XX:-InlineUnsafeOps -XX:-ReduceInitialCardMarks -XX:+StressReflectiveCode -Xcomp -XX:-TieredCompilation
 *                   compiler.arraycopy.TestCloneWithStressReflectiveCode
 */

public class TestCloneWithStressReflectiveCode implements Cloneable {

    static Object var1;
    static int var2 = 0;
    static int var8 = 0;

    public static void main(String[] args) throws CloneNotSupportedException {
        int var0 = 0;
        do {
            if (var0 < 7) {
                TestCloneWithStressReflectiveCode t = new TestCloneWithStressReflectiveCode();
                continue;
            }
            var0++;
        } while (var0 < 10000);
        boolean var4 = true;
        int var3 = 0;
        do {
            if (var3 == 50000) {
                TestCloneWithStressReflectiveCode t = ((TestCloneWithStressReflectiveCode) (var1 = new TestCloneWithStressReflectiveCode()));
                if (var4) var2++;
                var3++;
            }
        } while (var3 < 100000);
        int var5 = 0;
        while (var5 < 10000) {
            if (var5 < 5) {
                TestCloneWithStressReflectiveCode t = ((TestCloneWithStressReflectiveCode) (var1 = new TestCloneWithStressReflectiveCode()));
                continue;
            }
            var5++;
        }
        for (int var6 = 0; var6 < 10000; ++var6) {
            if (var6 < 7) {
                TestCloneWithStressReflectiveCode t = ((TestCloneWithStressReflectiveCode) (var1 = new TestCloneWithStressReflectiveCode()));
                continue;
            }
        }
        for (int var7 = 0; var7 < 10000; ++var7) {
            if (var7 < 3) {
                TestCloneWithStressReflectiveCode t = (TestCloneWithStressReflectiveCode) (((TestCloneWithStressReflectiveCode) (var1 = new TestCloneWithStressReflectiveCode())));
                continue;
            }
        }
        boolean var10 = true;
        int var9 = 0;
        while (var9 < 100000) {
            if (var9 == 50000) {
                TestCloneWithStressReflectiveCode t = (TestCloneWithStressReflectiveCode) (((TestCloneWithStressReflectiveCode) (var1 = new TestCloneWithStressReflectiveCode())));
            }
            if (var10) var8++;
            var9++;
        }
        synchronized (TestCloneWithStressReflectiveCode.class) {
            synchronized (new Double(1.1f)) {
                TestCloneWithStressReflectiveCode t = (TestCloneWithStressReflectiveCode) (((TestCloneWithStressReflectiveCode) (var1 = new TestCloneWithStressReflectiveCode())));
                for (int i = 0; i < 50_000; ++i) {
                    t.test();
                }
            }
        }

    }

    public Object test() throws CloneNotSupportedException {
        return clone();
    }
}
