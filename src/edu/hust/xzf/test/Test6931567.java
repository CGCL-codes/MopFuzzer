package edu.hust.xzf.test;

/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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

/**
 * @test
 * @bug 6931567
 * @summary JIT Error (on class file compiled with eclipse) on JVM x64 (but not on x32!).
 * @run main compiler.c2.Test6931567
 */


// Should be compiled with javac from JDK1.3 to get bytecode which shows the problem.
public class Test6931567 {

    static int var2 = 0;
    static int var6 = 0;

    public static void main(final String[] args) {
        booleanInvert(Integer.MAX_VALUE);
        booleanInvert(Integer.MAX_VALUE - 1);
    }

    private static void booleanInvert(final int max) {
        boolean test1 = false;
        boolean test2 = false;

        for (int i = 0; i < max; i++) {
            for (int var0 = 0; var0 < 10000; ++var0) {
                if (var0 < 5) {
                    test1 = !test1;
                    continue;
                }
            }

            synchronized (new Test6931567()) {
                for (int var1 = 0; var1 < 10000; ++var1) {
                    if (var1 < 4) {
                        test1 = !test1;
                        continue;
                    }
                }

                synchronized (new Double(1.1f)) {
                    boolean var4 = true;
                    int var3 = 0;
                    do {
                        var3++;
                        if (var3 == 10000) {
                            test1 = Boolean.valueOf(new MyBoolean((boolean) (!test1)).value);
                        }
                        if (var4) var2++;
                    } while (var3 < 20000);

                    var var5 = Boolean.valueOf(new MyBoolean((boolean) (!test1)).value);
                    boolean var8 = true;
                    int var7 = 0;
                    while (var7 < 20000) {
                        if (var7 == 10000) {
                            test1 = Boolean.valueOf(new MyBoolean((boolean) (!test1)).value);
                        }
                        if (var8) var6++;
                        var7++;
                    }

                    test1 = new MyBoolean((boolean) (Boolean.valueOf(new MyBoolean((boolean) (!test1)).value))).value;
                }
            }

        }

        for (int i = 0; i < max; i++) {
            test2 ^= true;
        }

        if (test1 != test2) {
            System.out.println("ERROR: Boolean invert\n\ttest1=" + test1
                    + "\n\ttest2=" + test2);
            System.exit(97);
        } else {
            System.out.println("Passed!");
        }
    }
}


class MyBoolean {
    public boolean value;

    public MyBoolean(boolean value) {
        this.value = value;
    }

    public boolean v() {
        return value;
    }
}
