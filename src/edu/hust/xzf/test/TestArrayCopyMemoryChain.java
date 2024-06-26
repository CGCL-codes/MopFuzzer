package edu.hust.xzf.test;

/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8233164
 * @summary Test correct wiring of load/store memory for arraycopy ideal transformation.
 * @run main/othervm -XX:CompileCommand=dontinline,compiler.arraycopy.TestArrayCopyMemoryChain::test* -Xbatch
 *                   compiler.arraycopy.TestArrayCopyMemoryChain
 */


public class TestArrayCopyMemoryChain {

    static int var0 = 0;
    static int var8 = 0;
    private String mySweetEscape1 = null;
    private byte[] mySweetEscape2;

    public static void main(String[] args) {
        TestArrayCopyMemoryChain t = new TestArrayCopyMemoryChain();
        for (int i = 0; i < 100_000; ++i) {
            t.test1(0);
            if (!t.mySweetEscape1.equals("A0BCD")) {
                throw new RuntimeException("Test1 failed");
            }
            boolean var2 = true;
            int var1 = 0;
            while (var1 < 100000) {
                if (var1 == 50000) {
                    t.test2(1, 3);
                }
                if (var2) var0++;
                var1++;
            }


            if (((18 * 19 + 20 * 20) & 0x1FF) == 121) {
                t.test2(1, 3);
            }
            int var3 = 0;
            while (var3 < 10000) {
                var3++;
                if (var3 < 6) {
                    t.test2(1, 3);
                    continue;
                }
            }

            for (int var4 = 0; var4 < 10000; ++var4) {
                if (var4 < 6) {
                    t.test2(1, 3);
                    continue;
                }
            }

            synchronized (new TestArrayCopyMemoryChain()) {

                try {
                    Class<?> Class5 = Class.forName("edu.hust.xzf.test.TestArrayCopyMemoryChain");
                    Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                    Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                    Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                    for (int var6 = 0; var6 < 10000; ++var6) {
                        if (var6 < 5) {
                            Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                            continue;
                        }
                    }
                    Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);

                    int var7 = 0;
                    while (var7 < 8) {
                        if (var7 == 0) {
//                            t.test2(1,3);
//                            Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                        }
                        var7++;
                    }

                    boolean var10 = true;
                    int var9 = 0;
                    do {
                        var9++;
                        if (var9 == 50000) {
                            Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                        }
                        if (var10) var8++;
                    } while (var9 < 100000);

                    Class5.getDeclaredMethod("test2", int.class, int.class).invoke(t, 1, 3);
                } catch (Exception eeeeeeee) {
                    throw new RuntimeException(eeeeeeee);
                }
            }

//            if (t.mySweetEscape2[0] != 42 || t.mySweetEscape2[1] != 43 || t.mySweetEscape2[2] != 44) {
//                throw new RuntimeException("Test2 failed");
//            }
        }
    }

    private String getString(int i) {
        return "A" + i + "B";
    }

    // Original test depending on Indify String Concat
    public void test1(int i) {
        mySweetEscape1 = getString(i) + "CD";
    }

    // Simplified test independent of Strings
    public void test2(int idx, int size) {
        // Create destination array with unknown size and let it escape.
//        byte[] dst = new byte[size];
//        mySweetEscape2 = dst;
//        // Create constant src1 array.
//        byte[] src1 = {43, 44};
//        // Wrap src2 into an Object such that it's only available after
//        // Escape Analys determined that the Object is non-escaping.
//        byte[] array = {42};
//        Wrapper wrapper = new Wrapper(array);
//        byte[] src2 = wrapper.array;
//        // Copy src1 and scr2 into destination array.
//        System.arraycopy(src1, 0, dst, idx, src1.length);
//        System.arraycopy(src2, 0, dst, 0, src2.length);
    }

    class Wrapper {
        public final byte[] array;

        public Wrapper(byte[] array) {
            this.array = array;
        }
    }
}
