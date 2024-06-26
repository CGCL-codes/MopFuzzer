package edu.hust.xzf.mutator.deoptpatterns;

import edu.hust.xzf.mutator.config.Configuration;
import edu.hust.xzf.mutator.mutatetemplate.MutateTemplate;

public enum Mutators {

    optimisticNullnessAssertions {
        public OptimisticNullnessAssertions createInstance() {
            return new OptimisticNullnessAssertions();
        }
    }, //乐观空值断言

    optimisticTypeAssertions {
        public OptimisticTypeAssertions createInstance() {
            return new OptimisticTypeAssertions();
        }
    }, //乐观类型断言

    optimisticTypeStrengthening {
        public OptimisticTypeStrengthening createInstance() {
            return new OptimisticTypeStrengthening();
        }
    }, //乐观类型增强

    untakenBranchPruning {
        public UntakenBranchPruning createInstance() {
            return new UntakenBranchPruning();
        }
    },//裁剪未被选择的分支

    optimisticArrayLengthStrengthening {
        public OptimisticArrayLengthStrengthening createInstance() {
            return new OptimisticArrayLengthStrengthening();
        }
    },//乐观数组长度增强


    optimisticNmorphicInlining {
        public OptimisticNmorphicInlining createInstance() {
            return new OptimisticNmorphicInlining();
        }
    },// 乐观的多态内联

    reassociation {
        public Reassociation createInstance() {
            return new Reassociation();
        }
    },//重组

    operatorStrengthReduction {
        public OperatorStrengthReduction createInstance() {
            return new OperatorStrengthReduction();
        }
    }, // 操作符强度减弱

    nullCheckElimination {
        public NullCheckElimination createInstance() {
            return new NullCheckElimination();
        }
    }, // 空检查消除

    typeTestElimination {
        public TypeTestElimination createInstance() {
            return new TypeTestElimination();
        }
    }, // 类型检查消除

    algebraicSimplification {
        @Override
        public int getMaxHitTimes() {
            return 10;
        }

        public AlgebraicSimplification createInstance() {
            return new AlgebraicSimplification();
        }
    }, // 代数简化

    commonSubexpressionElimination {
        @Override
        public int getMaxHitTimes() {
            return 10;
        }

        public CommonSubexpressionElimination createInstance() {
            return new CommonSubexpressionElimination();
        }
    }, // 公共子表达式消除

    conditionalConstantPropagation {
        public ConditionalConstantPropagation createInstance() {
            return new ConditionalConstantPropagation();
        }
    }, // 条件常量传播

    deadCodeElimination {
        public DeadCodeElimination createInstance() {
            return new DeadCodeElimination();
        }
    }, // 死代码消除

    autoboxElimination {
        @Override
        public int getMaxHitTimes() {
            return 5;
        }

        public AutoboxElimination createInstance() {
            return new AutoboxElimination();
        }
    }, // 自动装箱消除

    lockElision {
        public LockElision createInstance() {
            return new LockElision();
        }
    }, // 锁消除

    lockFusion {
        public LockFusion createInstance() {
            return new LockFusion();
        }
    }, // 锁融合

    escapeAnalysis {
        @Override
        public int getMaxHitTimes() {
            return 10;
        }

        public EscapeAnalysis createInstance() {
            return new EscapeAnalysis();
        }
    }, // 逃逸分析

    escapeAnalysis2 {
        public EscapeAnalysis2 createInstance() {
            return new EscapeAnalysis2();
        }
    }, // 逃逸分析

    deReflection {
        @Override
        public int getMaxHitTimes() {
            return 10;
        }

        public DeReflection createInstance() {
            return new DeReflection();
        }
    }, // 反射消除


    loopUnrolling {
        @Override
        public int getMaxHitTimes() {
            return 3;
        }

        public LoopUnrolling createInstance() {
            return new LoopUnrolling();
        }
    }, // 循环展开

    loopPeeling {
        @Override
        public int getMaxHitTimes() {
            return 3;
        }
        public LoopPeeling createInstance() {
            return new LoopPeeling();
        }
    }, //循环剥离

    loopUnswitch {
        @Override
        public int getMaxHitTimes() {
            return 3;
        }

        public LoopUnswitch createInstance() {
            return new LoopUnswitch();
        }
    }, //循环剥离

    loop {
        @Override
        public int getMaxHitTimes() {
            return 6;
        }

        public Loop createInstance() {
            return new Loop();
        }
    }, //自己构造的复杂loog

    safepointElimination {
        public SafepointElimination createInstance() {
            return new SafepointElimination();
        } // 安全点消除
    },
    iterationRangeSplitting {
        public IterationRangeSplitting createInstance() {
            return new IterationRangeSplitting();
        }
    }, // 迭代范围分离
//
    rangeCheckElimination {
        public RangeCheckElimination createInstance() {
            return new RangeCheckElimination();
        }
    }, // 范围检查消除
//
//    loopInvariantCodeMotion {
//        public LoopInvariantCodeMotion createInstance() {
//            return new LoopInvariantCodeMotion();
//        }
//    }, // 循环不变代码运动
//
//    loopVectorization {
//        public LoopVectorization createInstance() {
//            return new LoopVectorization();
//        }
//    },//    循环向量化 loopVectorization

    inline {
        @Override
        public int getMaxHitTimes() {
            return 10;
        }

        public Inline createInstance() {
            return new Inline();
        }
    }; // 内联


    public abstract MutateTemplate createInstance();

    public int getMaxHitTimes() {
        return Configuration.MaxHitMutatorTimes;
    }
}
