/*
 * Tier Tower
 * Copyright (c) 2025 The Tier Tower Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.tier_tower.base.math.ast;

import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.math.operations.BiOp;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

public record CallNode(String fnName, Node[] args) implements Node {
    @Override
    public double evaluate(EvaluationContext context) throws EvaluationException {
        CallEvaluator evaluator = context.getFunction(fnName);

        double[] cache = new double[args.length];
        DoubleSupplier[] evaluators = new DoubleSupplier[args.length];
        for (int i = 0; i < args.length; i++) {
            final int index = i;
            evaluators[i] = () -> {
                if (Double.isNaN(cache[index])) {
                    try {
                        cache[index] = args[index].evaluate(context);
                    } catch (EvaluationException e) {
                        throw new RuntimeException("Error evaluating argument " + index + " for function " + fnName, e);
                    }
                }
                return cache[index];
            };
            cache[i] = Double.NaN;
        }

        try {
            return evaluator.evaluate(evaluators);
        } catch (Exception e) {
            throw new EvaluationException("Error evaluating function " + fnName, e);
        }
    }

    @Override
    public String repr() {
        return fnName + "(" + String.join(", ", java.util.Arrays.stream(args).map(Node::repr).toArray(String[]::new)) + ")";
    }

    @Override
    public String reprS() {
        if (args.length == 0) {
            return "(fn<" + fnName + ">)";
        }

        return "(fn<" + fnName + "> " + String.join(" ", java.util.Arrays.stream(args).map(Node::reprS).toArray(String[]::new)) + ")";
    }

    @Override
    public void visitSelfAndChildren(Consumer<Node> consumer) {
        Node.super.visitSelfAndChildren(consumer);
        for (Node arg : args) {
            arg.visitSelfAndChildren(consumer);
        }
    }

    public interface CallEvaluator {
        double evaluate(DoubleSupplier[] args) throws EvaluationException;

        static CallEvaluator constant(double value) {
            return args -> {
                if (args.length != 0) {
                    throw new EvaluationException("Expected 0 arguments for constant function, got " + args.length);
                }
                return value;
            };
        }

        static CallEvaluator op(DoubleUnaryOperator function) {
            return args -> {
                if (args.length != 1) {
                    throw new EvaluationException("Expected 1 argument for unary operation, got " + args.length);
                }
                return function.applyAsDouble(args[0].getAsDouble());
            };
        }

        static CallEvaluator op2(BiOp.BiOpFunction function) {
            return args -> {
                if (args.length != 2) {
                    throw new EvaluationException("Expected 2 arguments for binary operation, got " + args.length);
                }
                return function.compute(args[0].getAsDouble(), args[1].getAsDouble());
            };
        }

        static CallEvaluator many0(ToDoubleFunction<double[]> function) {
            return args -> {
                double[] evaluatedArgs = new double[args.length];
                for (int i = 0; i < args.length; i++) {
                    evaluatedArgs[i] = args[i].getAsDouble();
                }
                return function.applyAsDouble(evaluatedArgs);
            };
        }

        static CallEvaluator many1(ToDoubleFunction<double[]> function) {
            return args -> {
                if (args.length < 1) {
                    throw new EvaluationException("Expected at least 1 argument, got " + args.length);
                }
                double[] evaluatedArgs = new double[args.length];
                for (int i = 0; i < args.length; i++) {
                    evaluatedArgs[i] = args[i].getAsDouble();
                }
                return function.applyAsDouble(evaluatedArgs);
            };
        }
    }
}
