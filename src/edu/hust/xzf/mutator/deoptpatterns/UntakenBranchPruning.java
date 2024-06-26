package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;

import java.util.Random;

public class UntakenBranchPruning extends MutateTemplate {
    @Override
    public void generatePatches() {
        Random r = new Random();
        int select = r.nextInt(3);
        String fixedCodeStr1 = "";
        int random = Math.abs(r.nextInt());
//        String[] falseExp = new String[]{"Math.sin(Math.PI / 2) == 0", "Math.sqrt(-1) == Math.sqrt(-1)",
//                "\"hello\".startsWith(\"g\")", "Math.log(1) == Math.exp(1)", "(Math.PI * Math.sqrt(2)) == 22.0 / 7.0",
//                "Math.exp(0) != 1", "Math.cos(Math.PI) != -1", "Character.isDigit('A')", "\"abc\".contains(\"xyz\")",
//                random + "> Integer.parseInt(\"" + (random + 1) + "\")"};

        String[] falseAnd1KTrue = new String[]{
                // these are genearated with the help of GPT-4

                //int countOnes = Integer.bitCount((int) (System.nanoTime() % 1024));
                // boolean condition5 = countOnes == 10;
                "Integer.bitCount((int) (System.nanoTime() % 1024)) == 10",

                // int randomInt = (int) (System.nanoTime() % 100) + 1;
                // boolean condition3 = randomInt == 50;
                "(int) (System.nanoTime() % 100) + 1 == 50",

                // int randomInt1 = (int) (System.nanoTime() % 100000) + 1;
                //int randomInt2 = (int) ((System.nanoTime() / 2) % 100000) + 1;
                //boolean condition1 = Math.abs(randomInt1 - randomInt2) == 12345;
                "Math.abs((int) (System.nanoTime() % 100000) + 1 - (int) ((System.nanoTime() / 2) % 100000) + 1) == 12345",

                // int randomInt = (int) (System.nanoTime() % 100000) + 1;
                // int cubeRoot = (int) Math.round(Math.cbrt(randomInt));
                // boolean condition3 = cubeRoot == 11;
                "Math.round(Math.cbrt((int) (System.nanoTime() % 100000) + 1)) == 11",

                // int randomInt = (int) (System.nanoTime() % 10000) + 1;
                // int firstDigit = Integer.parseInt(Integer.toString(randomInt).substring(0, 1));
                // int lastDigit = randomInt % 10;
                // boolean condition5 = firstDigit == lastDigit;
                "Integer.parseInt(Integer.toString((int) (System.nanoTime() % 10000) + 1).substring(0, 1)) == ((int) (System.nanoTime() % 10000) + 1) % 10",

                // int randomInt = (int) (System.nanoTime() % 100000) + 1;
                // int sumOfDigits = String.valueOf(randomInt).chars().map(Character::getNumericValue).sum();
                // boolean condition2 = sumOfDigits == 42;
                "String.valueOf((int) (System.nanoTime() % 100000) + 1).chars().map(Character::getNumericValue).sum() == 42",

                // int randomInt = (int) (System.nanoTime() % 1000) + 1;
                //String binaryStr = Integer.toBinaryString(randomInt);
                //boolean condition3 = binaryStr.contains("000000");
                "Integer.toBinaryString((int) (System.nanoTime() % 1000) + 1).contains(\"000000\")",

                // int randomInt = (int) (System.nanoTime() % 100000) + 1;
                // boolean condition2 = String.valueOf(randomInt).matches("^[13579]+$");
                "String.valueOf((int) (System.nanoTime() % 100000) + 1).matches(\"^[13579]+$\")",

                // int randomInt = (int) (System.nanoTime() % 10000) + 1;
                //String hexStr = Integer.toHexString(randomInt).toUpperCase();
                //int firstDigit = hexStr.charAt(0);
                //int lastDigit = hexStr.charAt(hexStr.length() - 1);
                //boolean condition3 = firstDigit == lastDigit;
                "Integer.toHexString((int) (System.nanoTime() % 10000) + 1).toUpperCase().charAt(0) " +
                        "== Integer.toHexString((int) " +
                        "(System.nanoTime() % 10000) + 1).toUpperCase().charAt(Integer.toHexString((int)" +
                        " (System.nanoTime() % 10000) + 1).toUpperCase().length() - 1)",
        };

        String[] falseAnd1WTrue = new String[]{
                // these are genearated with the help of GPT-4

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //int squareRoot = (int) Math.round(Math.sqrt(randomInt));
                //boolean condition1 = squareRoot * squareRoot == randomInt;
                "Math.round(Math.sqrt((int) (System.nanoTime() % 1000000) + 1)) " +
                        "* Math.round(Math.sqrt((int) (System.nanoTime() % 1000000) + 1)) == (int) (System.nanoTime() % 1000000) + 1",

                // int randomInt = (int) (System.nanoTime() % 10000000) + 1;
                //int sumOfDigits = String.valueOf(randomInt).chars().map(Character::getNumericValue).sum();
                //boolean condition2 = sumOfDigits == 100;
                "String.valueOf((int) (System.nanoTime() % 10000000) + 1).chars().map(Character::getNumericValue).sum() == 100",

                // int randomInt = (int) (System.nanoTime() % 100000) + 1;
                //String binaryStr = Integer.toBinaryString(randomInt);
                //boolean condition3 = binaryStr.contains("00000000");
                "Integer.toBinaryString((int) (System.nanoTime() % 100000) + 1).contains(\"00000000\")",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //String reversedStr = new StringBuilder(str).reverse().toString();
                //boolean condition4 = str.length() == 6 && str.equals(reversedStr);
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).length() == 6 && " +
                        "String.valueOf((int) (System.nanoTime() % 1000000) + 1).equals(new StringBuilder(String.valueOf((int) " +
                        "(System.nanoTime() % 1000000) + 1)).reverse().toString())",

                // int randomInt = (int) (System.nanoTime() % 10000000) + 1;
                //boolean condition7 = String.valueOf(randomInt).matches("^[02468]+$");
                "String.valueOf((int) (System.nanoTime() % 10000000) + 1).matches(\"^[02468]+$\")",

                // int randomInt1 = (int) (System.nanoTime() % 1000000) + 1;
                //int randomInt2 = (int) ((System.nanoTime() / 2) % 1000000) + 1;
                //long square = (long) randomInt1 * randomInt1;
                //long cube = (long) randomInt2 * randomInt2 * randomInt2;
                //boolean condition9 = square == cube;
                "((long) ((int) (System.nanoTime() % 1000000) + 1) * (long) ((int) ((System.nanoTime() / 2) % 1000000) + 1)) " +
                        "== (long) ((int) ((System.nanoTime() / 2) % 1000000) + 1) * (long) ((int) ((System.nanoTime() / 2) % 1000000) + 1) * " +
                        "(long) ((int) ((System.nanoTime() / 2) % 1000000) + 1)",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //int sumOfDigits = str.chars().map(Character::getNumericValue).sum();
                //boolean condition11 = str.length() == 6 && sumOfDigits == 42;
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).length() == 6 && " +
                        "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().map(Character::getNumericValue).sum() == 42",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String binaryStr = Integer.toBinaryString(randomInt);
                //long countZero = binaryStr.chars().filter(c -> c == '0').count();
                //long countOne = binaryStr.chars().filter(c -> c == '1').count();
                //boolean condition12 = countOne == countZero + 2;
                "Integer.toBinaryString((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '1').count() " +
                        "== Integer.toBinaryString((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '0').count() + 2",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //String reversedStr = new StringBuilder(str).reverse().toString();
                //boolean condition13 = str.equals(reversedStr);
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).equals(new StringBuilder(String.valueOf((int) " +
                        "(System.nanoTime() % 1000000) + 1)).reverse().toString())",


                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //int product = str.chars().map(Character::getNumericValue).reduce(1, (a, b) -> a * b);
                //boolean condition14 = product == 1000;
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().map(Character::getNumericValue).reduce(1, (aaa, bbb) -> aaa * bbb) == 1000",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //boolean condition15 = randomInt % 11 == 0 && randomInt % 13 == 0 && randomInt % 17 != 0;
                "((int) (System.nanoTime() % 1000000) + 1) % 11 == 0 && ((int) (System.nanoTime() % 1000000) + 1) % 13 == 0 && " +
                        "((int) (System.nanoTime() % 1000000) + 1) % 17 != 0",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //long countSeven = str.chars().filter(c -> c == '7').count();
                //boolean condition16 = countSeven == 3;
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(ccc -> ccc == '7').count() == 3",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String binaryStr = Integer.toBinaryString(randomInt);
                //boolean condition17 = binaryStr.contains("1111111");
                "Integer.toBinaryString((int) (System.nanoTime() % 1000000) + 1).contains(\"1111111\")",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String hexStr = Integer.toHexString(randomInt).toUpperCase();
                //long countA = hexStr.chars().filter(c -> c == 'A').count();
                //long countF = hexStr.chars().filter(c -> c == 'F').count();
                //boolean condition18 = countA == countF;
                "Integer.toHexString((int) (System.nanoTime() % 1000000) + 1).toUpperCase().chars().filter(ccc -> ccc == 'A').count() " +
                        "== Integer.toHexString((int) (System.nanoTime() % 1000000) + 1).toUpperCase().chars().filter(ccc -> ccc == 'F').count()",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                // String binaryStr = Integer.toBinaryString(randomInt);
                // long countZero = binaryStr.chars().filter(c -> c == '0').count();
                // long countOne = binaryStr.chars().filter(c -> c == '1').count();
                // boolean condition19 = countOne % 2 == 0 && countZero % 2 == 1;
                "Integer.toBinaryString((int) (System.nanoTime() % 1000000) + 1).chars().filter(ccc -> ccc == '1').count() % 2 == 0 && " +
                        "Integer.toBinaryString((int) (System.nanoTime() % 1000000) + 1).chars().filter(ccc -> ccc == '0').count() % 2 == 1",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //long countOne = str.chars().filter(c -> c == '1').count();
                //long countFour = str.chars().filter(c -> c == '4').count();
                //boolean condition21 = countOne == 2 * countFour;
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(ccc -> ccc == '1').count() == 2 * " +
                        "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(ccc -> ccc == '4').count()",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String str = String.valueOf(randomInt);
                //long countZero = str.chars().filter(c -> c == '0').count();
                //long countOne = str.chars().filter(c -> c == '1').count();
                //long countTwo = str.chars().filter(c -> c == '2').count();
                //boolean condition23 = countZero + countOne + countTwo == 7;
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '0').count() + " +
                        "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '1').count() + " +
                        "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '2').count() == 7",

                //  int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String binaryStr = Integer.toBinaryString(randomInt);
                //boolean condition27 = binaryStr.contains("000000000");
                "Integer.toBinaryString((int) (System.nanoTime() % 1000000) + 1).contains(\"000000000\")",

                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                //String hexStr = Integer.toHexString(randomInt).toUpperCase();
                //long countB = hexStr.chars().filter(c -> c == 'B').count();
                //long countD = hexStr.chars().filter(c -> c == 'D').count();
                //boolean condition28 = countB == countD;
                "Integer.toHexString((int) (System.nanoTime() % 1000000) + 1).toUpperCase().chars().filter(cc -> cc == 'B').count() " +
                        "== Integer.toHexString((int) (System.nanoTime() % 1000000) + 1).toUpperCase().chars().filter(cc -> cc == 'D').count()",


                // int randomInt = (int) (System.nanoTime() % 1000000) + 1;
                // String str = String.valueOf(randomInt);
                // long countTwo = str.chars().filter(c -> c == '2').count();
                // long countFive = str.chars().filter(c -> c == '5').count();
                // long countEight = str.chars().filter(c -> c == '8').count();
                //boolean condition30 = countTwo == 2 * (countFive + countEight);
                "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '2').count() == 2 * " +
                        "(String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '5').count() + " +
                        "String.valueOf((int) (System.nanoTime() % 1000000) + 1).chars().filter(cc -> cc == '8').count())",
        };

        int select2 = r.nextInt(falseAnd1WTrue.length);
        String newVar = generateUniqueVarName();
        fixedCodeStr1 += "for (int " + newVar + " = 0; " + newVar + " < 150; " + newVar + "++) {\n";
        switch (select) {
            case 0 -> {
                // if statement
                fixedCodeStr1 += "if (" + falseAnd1WTrue[select2] + ") {\n";
                fixedCodeStr1 += this.getSuspiciousCodeStr();
                fixedCodeStr1 += "\n}}\n";
            }
            case 1 -> {
                // do statement
                fixedCodeStr1 += "do {\n";
                fixedCodeStr1 += this.getSuspiciousCodeStr();
                fixedCodeStr1 += "\n} while (" + falseAnd1WTrue[select2] + ");}\n";
            }
            case 2 -> {
                // while statement
                fixedCodeStr1 += "while (" + falseAnd1WTrue[select2] + "){\n ";
                fixedCodeStr1 += this.getSuspiciousCodeStr();
                fixedCodeStr1 += "\n}}\n";
            }
        }
        this.generatePatch(suspCodeEndPos, fixedCodeStr1);
        offset += 4;
    }

}
