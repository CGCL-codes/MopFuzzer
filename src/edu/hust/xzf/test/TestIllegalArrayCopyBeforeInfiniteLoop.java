package edu.hust.xzf.test;

/*
 * Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
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
 * @bug 8272131
 * @requires vm.compiler2.enabled
 * @summary ArrayCopy with negative index before infinite loop
 * @run main/othervm -Xbatch -XX:-TieredCompilation
 * -XX:CompileCommand=compileonly,"*TestIllegalArrayCopyBeforeInfiniteLoop::foo"
 * compiler.arraycopy.TestIllegalArrayCopyBeforeInfiniteLoop
 * @test
 * @bug 8272131
 * @requires vm.compiler2.enabled
 * @summary ArrayCopy with negative index before infinite loop
 * @run main/othervm -Xbatch -XX:-TieredCompilation
 * -XX:CompileCommand=compileonly,"*TestIllegalArrayCopyBeforeInfiniteLoop::foo"
 * compiler.arraycopy.TestIllegalArrayCopyBeforeInfiniteLoop
 */

/*
 * Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
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
 * @bug 8272131
 * @requires vm.compiler2.enabled
 * @summary ArrayCopy with negative index before infinite loop
 * @run main/othervm -Xbatch -XX:-TieredCompilation
 *                   -XX:CompileCommand=compileonly,"*TestIllegalArrayCopyBeforeInfiniteLoop::foo"
 *                   compiler.arraycopy.TestIllegalArrayCopyBeforeInfiniteLoop
 */


import java.util.Arrays;

public class TestIllegalArrayCopyBeforeInfiniteLoop {
    private static final int iter = 10_000;
    static int var0 = 0;
    static int var3 = 0;
    static int var9 = 0;
    private static char[] src = new char[10];
    private static int count = 0;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < iter; ++i) {

            if ((52 + 53) << 1 != 210) {
                foo();
            }
            boolean var2 = true;
            int var1 = 0;
            while (var1 < 100000) {
                if (var1 == 50000) {
                    foo();
                }
                if (var2) var0++;
                var1++;
            }

            boolean var5 = true;
            for (int var4 = 0; var4 < 100000; ++var4) {
                if (var4 == 50000) {
                    foo();
                }
                if (var5) var3++;
            }

            synchronized (TestIllegalArrayCopyBeforeInfiniteLoop.class) {
                for (int var6 = 0; var6 < 8; ++var6) {
                    if (var6 == 9999) {
                        foo();
                    }
                    var6 = var6;
                }

                int var7 = 0;
                do {
                    if (var7 == 0) {
                        foo();
                        var7++;
                    }
                } while (var7 < 8);


                try {
                    Class<?> Class8 = Class.forName("compiler.arraycopy.TestIllegalArrayCopyBeforeInfiniteLoop");
                    boolean var11 = true;
                    for (int var10 = 0; var10 < 100000; ++var10) {
                        if (var10 == 50000) {
                            Class8.getDeclaredMethod("foo").invoke(null);
                        }
                        if (var11) var9++;
                    }

                    synchronized (TestIllegalArrayCopyBeforeInfiniteLoop.class) {
                        Class8.getDeclaredMethod("foo").invoke(null);
                    }

                } catch (Exception eeeeeeee) {
                    throw new RuntimeException(eeeeeeee);
                }
            }

        }
        if (count != iter) {
            throw new RuntimeException("test failed");
        }
    }

    static void foo() {
        try {
            Arrays.copyOfRange(src, -1, 128);
            do {
            } while (true);
        } catch (ArrayIndexOutOfBoundsException ex) {
            count++;
        }
    }
}
