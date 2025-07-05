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

public record OpNode(Node left, BiOp op, Node right) implements Node {
    @Override
    public double evaluate(EvaluationContext context) throws EvaluationException {
        double leftValue, rightValue;

        try {
            leftValue = left.evaluate(context);
        } catch (EvaluationException e) {
            throw new EvaluationException("Error evaluating left node", e);
        }

        try {
            rightValue = right.evaluate(context);
        } catch (EvaluationException e) {
            throw new EvaluationException("Error evaluating right node", e);
        }

        try {
            return op.compute(leftValue, rightValue);
        } catch (ArithmeticException e) {
            throw new EvaluationException("Arithmetic error during evaluation", e);
        }
    }

    @Override
    public String repr() {
        return "(" + left.repr() + " " + op.symbol + " " + right.repr() + ")";
    }

    @Override
    public String reprS() {
        return "(" + op.symbol + " " + left.reprS() + " " + right.reprS() + ")";
    }

    @Override
    public void visitSelfAndChildren(Consumer<Node> consumer) {
        Node.super.visitSelfAndChildren(consumer);
        left.visitSelfAndChildren(consumer);
        right.visitSelfAndChildren(consumer);
    }
}
