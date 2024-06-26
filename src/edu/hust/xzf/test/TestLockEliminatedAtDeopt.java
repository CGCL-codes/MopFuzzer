package edu.hust.xzf.test;

public class TestLockEliminatedAtDeopt {

    static void m1(boolean trap) {
        A a = new A();
        // This lock is eliminated by c2
        synchronized (a) {
            a.m2(trap);
            a.m();
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 20000; i++) {
            m1(false);
        }
        // Trigger uncommon trap in A.m2() (class unloaded) and
        // deoptimization of m1() (CHA invalidated). Uncommon trap
        // code locks monitor in m1's frame where's it's eliminated.
        m1(true);
    }

    static class A {
        void m() {
        }

        // This lock is not eliminated but biased to main thread on
        // first call
        synchronized void m2(boolean trap) {
            if (trap) {
                new B();
            }
        }
    }

    static class B extends A {
        void m() {
        }
    }
}
