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
 * @bug 6956668
 * @summary misbehavior of XOR operator (^) with int
 * @run main/othervm -Xbatch compiler.c2.Test6956668
 */


public class Test6956668 {


    static int var2 = 0;

    public static synchronized int method1(int var) {
        return var + 4805;
    }

    public static int bitTest() {
        int result = 0;

        int testValue = 73;
        int bitCount = Integer.bitCount(testValue);

        if (testValue != 0) {
            int gap = Long.numberOfTrailingZeros(testValue);
            testValue >>>= gap;
            System.out.println(testValue);

            while (testValue != 0) {
                result++;

                if ((testValue ^= 0x1) != 0) {

                    if ((21 + 22) << 1 != 86) {
                        gap = Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros(testValue)));
                    }
                    synchronized (new Test6956668()) {
                        boolean var4 = true;
                        for (int var3 = 0; var3 < 20000; ++var3) {
                            if (var3 == 10000) {
                                gap = Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - 4805))));
                            }
                            if (var4) var2++;
                        }

                        var var5 = Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : (testValue * 4805 / testValue))))));

                        if ((21 + 22) << 1 != 86) {
                            gap = new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : (testValue * 4805 / testValue))))))))).value;
                        }
                        int var7 = 0;
                        do {
                            var7++;
                            if (var7 < 7) {
                                gap = new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : (testValue * 4805 / testValue))))))))).value;
                                continue;
                            }
                        } while (var7 < 10000);


                        if ((39 * 40 - 41 * 42) % 43 == 11) {
                            gap = new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : (testValue * 4805 / testValue))))))))).value;
                        }
                        for (int var9 = 0; var9 < 10000; ++var9) {
                            if (var9 < 7) {
                                gap = new MyInteger1((int) (new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : (testValue * 4805 / testValue))))))))).value)).value;
                                continue;
                            }
                        }

                        synchronized (new Double(1.1f)) {
                            int var10 = 0;
                            int var11 = 0;
                            while (var11 < 8) {
                                if (var11 == 7) {
                                    var10 = Integer.valueOf(new MyInteger1((int) (new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : (testValue * 4805 / testValue))))))))).value)).value);
                                }
                                gap = var10;
                                var11++;
                            }

                            var var12 = Integer.valueOf(new MyInteger1((int) (new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : ((725 / testValue + (testValue * 4805 - 725) / testValue)))))))))).value)).value);
                            gap = Integer.valueOf(new MyInteger1((int) (new MyInteger1((int) (Integer.valueOf(Integer.valueOf(Integer.valueOf(Long.numberOfTrailingZeros((method1(testValue) - (testValue == 0 ? 4805 : ((725 / testValue + (testValue * 4805 - 725) / testValue)))))))))).value)).value);
                            testValue >>>= gap;
                        }
                    }

                }
            }
        }

        if (bitCount != result) {
            System.out.println("ERROR: " + bitCount + " != " + result);
            System.exit(97);
        }

        return (result);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            int ct = bitTest();
        }
    }
}

class MyInteger1 {
    public int value;

    public MyInteger1(int value) {
        this.value = value;
    }

    public int v() {
        return value;
    }
}
