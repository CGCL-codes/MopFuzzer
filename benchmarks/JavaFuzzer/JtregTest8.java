package JavaFuzzer;/*
 * Copyright (c) 2021, Red Hat, Inc. All rights reserved.
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
 *
 */

/*
 * @test
 * @library . /testlibrary
 * @run driver JtregTest
 */
import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.oracle.java.testlibrary.OutputAnalyzer;
import com.oracle.java.testlibrary.ProcessTools;
import com.oracle.java.testlibrary.Utils;

public class JtregTest {
    public static void main(String... args) throws Throwable {
        OutputAnalyzer output = ProcessTools.executeTestJvm(
                "-Xmx512m",
		"Test"
	);
	output.shouldHaveExitValue(0);

        String goldenFile = Utils.TEST_SRC + File.separator + "golden.out";
        List<String> expected = Files.readAllLines(new File(goldenFile).toPath());
        List<String> actual = output.asLines();

        if (expected.size() != actual.size()) {
            throw new IllegalStateException("Expected and actual output length differ: " + expected.size() + " vs " + actual.size());
        }

        for (int c = 0; c < expected.size(); c++) {
            String e = expected.get(c);
            String a = actual.get(c);
            if (!e.equals(a)) {
                throw new IllegalStateException("Expected and actual output differ at line " + c + ":\n Expected: " + e + "\n Actual: " + a);
            }
	}
    }
}
