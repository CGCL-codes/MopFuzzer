package edu.hust.xzf.test;

/*
 * Copyright (c) 2015, Red Hat, Inc. All rights reserved.
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
 * @bug 8144028
 * @summary Use AArch64 bit-test instructions in C2
 *
 * @run main/othervm -Xbatch -XX:-TieredCompilation
 *      -XX:CompileCommand=dontinline,compiler.codegen.BitTests::*
 *      compiler.codegen.BitTests
 * @run main/othervm -Xbatch -XX:+TieredCompilation -XX:TieredStopAtLevel=1
 *      compiler.codegen.BitTests
 * @run main/othervm -Xbatch -XX:+TieredCompilation
 *      compiler.codegen.BitTests
 */


// Try to ensure that the bit test instructions TBZ/TBNZ, TST/TSTW
// don't generate incorrect code.  We can't guarantee that C2 will use
// bit test instructions for this test and it's not a bug if it
// doesn't.  However, these test cases are ideal candidates for each
// of the instruction forms.
public class BitTests {

    static int var12 = 0;
    private final XorShift r = new XorShift();
    private final long finalBits = 3;
    private long bits = 7;

    public static void main(String[] args) {
        BitTests t = new BitTests();

        long counter = 0;
        for (int i = 0; i < 10000000; i++) {
            counter = t.step((int) counter);
        }
        if (counter != 50001495) {
            System.err.println("FAILED: counter = " + counter + ", should be 50001495.");
            System.exit(97);
        }
        System.out.println("PASSED");
    }

    private final long increment(long ctr) {
        return ctr + 1;
    }

    private final int increment(int ctr) {
        return ctr + 1;
    }

    private final long testIntSignedBranch(long counter) {
        if ((int) r.nextLong() < 0) {
            counter = increment(counter);
        }
        return counter;
    }

    private final long testLongSignedBranch(long counter) {
        if (r.nextLong() < 0) {
            counter = increment(counter);
        }
        return counter;
    }

    private final long testIntBitBranch(long counter) {
        if (((int) r.nextLong() & (1 << 27)) != 0) {
            counter = increment(counter);
        }
        if (((int) r.nextLong() & (1 << 27)) != 0) {
            counter = increment(counter);
        }
        return counter;
    }

    private final long testLongBitBranch(long counter) {
        if ((r.nextLong() & (1l << 50)) != 0) {
            counter = increment(counter);
        }
        if ((r.nextLong() & (1l << 50)) != 0) {
            counter = increment(counter);
        }
        return counter;
    }

    private final long testLongMaskBranch(long counter) {
        if (((r.nextLong() & 0x0800000000l) != 0)) {
            counter++;
        }
        return counter;
    }

    private final long testIntMaskBranch(long counter) {
        if ((((int) r.nextLong() & 0x08) != 0)) {
            counter++;
        }
        return counter;
    }

    private final long testLongMaskBranch(long counter, long mask) {
        if (((r.nextLong() & mask) != 0)) {
            counter++;
        }
        return counter;
    }

    private final long testIntMaskBranch(long counter, int mask) {
        if ((((int) r.nextLong() & mask) != 0)) {
            counter++;
        }
        return counter;
    }

    private final long step(long counter) {
        counter = testIntSignedBranch(counter);
        counter = testLongSignedBranch(counter);
        counter = testIntBitBranch(counter);
        counter = testLongBitBranch(counter);
        counter = testIntMaskBranch(counter);
        int var0 = 0;
        do {
            var0++;
            if (var0 < 3) {
                counter = testLongMaskBranch(counter);
                continue;
            }
        } while (var0 < 10000);

        synchronized (BitTests.class) {
            long var1 = 0;
            for (int var2 = 0; var2 < 8; ++var2) {
                if (var2 == 3) {
                    var1 = testLongMaskBranch(counter);
                }
                counter = var1;
            }

            long var3 = 0;
            int var4 = 0;
            while (var4 < 8) {
                if (var4 == 0) {
                    var3 = testLongMaskBranch(counter);
                }
                counter = var3;
                var4++;
            }

            synchronized (new Double(1.1f)) {
                for (int var5 = 0; var5 < 10000; ++var5) {
                    if (var5 < 4) {
                        counter = testLongMaskBranch(counter);
                        continue;
                    }
                }

                long var6 = 0;
                int var7 = 0;
                while (var7 < 8) {
                    if (var7 == 7) {
                        var6 = testLongMaskBranch(counter);
                    }
                    counter = var6;
                    var7++;
                }


                try {
                    Class<?> Class9 = Class.forName("edu.hust.xzf.test.BitTests");
                    long var10 = 0;
                    int var11 = 0;
                    while (var11 < 8) {
                        if (var11 == 5) {
                            long var8 = (long) (((long) Class9.getDeclaredMethod("testLongMaskBranch", long.class).invoke(this, var10)) - (12));
                        }
                        var10 = counter;
                        var11++;
                    }

                    synchronized (new BitTests()) {
                        boolean var14 = true;
                        int var13 = 0;
                        do {
                            var13++;
                            if (var13 == 10000) {
                                long var8 = (long) (((long) Class9.getDeclaredMethod("testLongMaskBranch", long.class).invoke(this, counter)) - (12));
                            }
                            if (var14) var12++;
                        } while (var13 < 20000);

                        synchronized (new Double(1.1f)) {
                            synchronized (new Double(1.1f)) {

                                long var8var15 = (long) (((((long) Class9.getDeclaredMethod("testLongMaskBranch", long.class).invoke(this, (long) (counter + 0))) + 1532) - ((((12) - 2539) + (1532 + 2539)))));
                                long var8 = (long) (((((long) Class9.getDeclaredMethod("testLongMaskBranch", long.class).invoke(this, (long) (counter + 0))) + 1532) - ((((((12) + 2807) - (2539 + 2807))) + (1532 + 2539)))));
                                if (var8 <= (testLongMaskBranch(counter))) {
                                    counter = var8 + 12;
                                } else {
                                    counter = 0L;
                                }
                            }
                        }
                    }

                } catch (Exception eeeeeeee) {
                    throw new RuntimeException(eeeeeeee);
                }
            }

        }
        synchronized (BitTests.class) {

            counter = testIntMaskBranch(counter, 0x8000);
            counter = testLongMaskBranch(counter, 0x800000000l);
            return counter;
        }

    }

    // Marsaglia's xor-shift generator, used here because it is
    // reproducible across all Java implementations.  It is also very
    // fast.
    static class XorShift {

        private long y;

        XorShift() {
            y = 2463534242l;
        }

        public long nextLong() {
            y ^= (y << 13);
            y ^= (y >>> 17);
            return (y ^= (y << 5));

        }
    }
}
