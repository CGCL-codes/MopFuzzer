package edu.hust.xzf.test;

/*
 * Copyright (c) 2019, Red Hat, Inc. All rights reserved.
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
 * @bug 8229701
 * @summary C2 OSR compilation fails with "shouldn't process one node several times" in final graph reshaping
 * @run main/othervm ConvI2LWideningAssertTooStrong
 */

public class ConvI2LWideningAssertTooStrong {

    public static final int N = 400;

    public static long instanceCount = 708L;
    public static volatile int iFld1 = 30517;
    public static int[] iArrFld = new int[N];
    static int var1 = 0;
    static int var4 = 0;
    static int var8 = 0;
    static int var13 = 0;

    public static void vMeth(short s) {
        int i9 = 29117, i11 = -6;

        for (i9 = 11; i9 < 377; i9++) {
            switch ((i9 % 8) + 22) {
                case 24:
                    instanceCount = i9;
                    instanceCount += instanceCount;
                    break;
                case 25:
                    try {
                        i11 = (20705 % i11);
                        iArrFld[i9 - 1] = (55094 / iFld1);
                    } catch (ArithmeticException a_e) {
                    }
                    break;
                default:
            }
        }
    }

    public static void main(String[] strArr) {
        ConvI2LWideningAssertTooStrong _instance = new ConvI2LWideningAssertTooStrong();
        for (int i = 0; i < 10 * 202 * 8; i++) {
            synchronized (ConvI2LWideningAssertTooStrong.class) {

                try {
                    Class<?> Class0 = Class.forName("edu.hust.xzf.test.ConvI2LWideningAssertTooStrong");
                    boolean var3 = true;
                    int var2 = 0;
                    do {
                        var2++;
                        if (var2 == 10000) {
                            Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                        }
                        if (var3) var1++;
                    } while (var2 < 20000);

                    synchronized (ConvI2LWideningAssertTooStrong.class) {
                        synchronized (new Double(1.1f)) {
                            boolean var6 = true;
                            int var5 = 0;
                            while (var5 < 20000) {
                                if (var5 == 10000) {
                                    Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                }
                                if (var6) var4++;
                                var5++;
                            }

                            synchronized (new ConvI2LWideningAssertTooStrong()) {
                                int var7 = 0;
                                do {
                                    var7++;
                                    if (var7 == 0) {
                                        Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                    }
                                } while (var7 < 8);

                                boolean var10 = true;
                                int var9 = 0;
                                do {
                                    var9++;
                                    if (var9 == 10000) {
                                        Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                    }
                                    if (var10) var8++;
                                } while (var9 < 20000);

                                if (_instance instanceof ConvI2LWideningAssertTooStrong) {
                                    synchronized (new ConvI2LWideningAssertTooStrong()) {
                                        int var11 = 0;
                                        do {
                                            var11++;
                                            if (var11 == 0) {
                                                Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                            }
                                        } while (var11 < 8);

                                        int var12 = 0;
                                        do {
                                            var12++;
                                            if (var12 == 0) {
                                                Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                            }
                                        } while (var12 < 8);

                                        boolean var15 = true;
                                        for (int var14 = 0; var14 < 20000; ++var14) {
                                            if (var14 == 10000) {
                                                Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                            }
                                            if (var15) var13++;
                                        }

                                        for (int var16 = 0; var16 < 10000; ++var16) {
                                            if (var16 < 4) {
                                                Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                                continue;
                                            }
                                        }

                                        int var17 = 0;
                                        while (var17 < 10000) {
                                            var17++;
                                            if (var17 < 6) {
                                                Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                                continue;
                                            }
                                        }


                                        if ((52 + 53) << 1 != 210) {
                                            Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                        }
                                        if (_instance instanceof ConvI2LWideningAssertTooStrong) {
                                            Class0.getDeclaredMethod("vMeth", short.class).invoke(_instance, (short) 20806);
                                        }
                                    }

                                }
                            }
                        }
                    }

                } catch (Exception eeeeeeee) {
                    throw new RuntimeException(eeeeeeee);
                }
            }

        }
    }
}
