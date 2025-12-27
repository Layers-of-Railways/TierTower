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
import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.parser.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    private static final EvaluationContext EMPTY = new EvaluationContext();
    private static final double EPSILON = 1e-9;

    void testCalculation(String expression, double expected) {
        testCalculation(expression, EMPTY, expected);
    }

    @SuppressWarnings("SameParameterValue")
    void testCalculation(String expression, EvaluationContext ctx, double expected) {
        Node node;
        try {
            node = Parser.parse(expression);
        } catch (ParseException e) {
            fail("Failed to parse '" + expression + "'", e);
            return;
        }

        try {
            double result = node.evaluate(ctx);
            assertEquals(expected, result, EPSILON);
        } catch (EvaluationException e) {
            fail("Failed to evaluate expression '" + expression + "'", e);
        }
    }

    @Test
    void simpleMath() {
        assertAll(
            () -> testCalculation("3", 3.0),
            () -> testCalculation("1 + 2", 3.0),
            () -> testCalculation("6 * 7", 42.0),
            () -> testCalculation("4 * 0.5", 2.0),
            () -> testCalculation("2 ^ 3", 8.0),
            () -> testCalculation("3 ^ 2", 9.0),
            () -> testCalculation("2 ^ 3 ^ 4", 2417851639229258349412352.0)
        );
    }

    @Test
    void parenMath() {
        assertAll(
            () -> testCalculation("1 + 2 * 3", 7.0),
            () -> testCalculation("1 + (2 * 3)", 7.0),
            () -> testCalculation("(1 + 2) * 3", 9.0)
        );
    }

    @Test
    void variableMath() {
        EvaluationContext ctx = new EvaluationContext()
            .set("x", 2.0)
            .set("y", -3.0);

        assertAll(
            () -> testCalculation("x + y", ctx, -1.0),
            () -> testCalculation("x * y", ctx, -6.0),
            () -> testCalculation("x ^ y", ctx, 0.125),
            () -> testCalculation("(x + y) * 2", ctx, -2.0)
        );
    }

    @Test
    void functionMath() {
        assertAll(
            () -> testCalculation("sin(pi() / 2)", 1.0),
            () -> testCalculation("cos(0)", 1.0),
            () -> testCalculation("tan(pi() / 4)", 1.0),
            () -> testCalculation("atan2(1, 1)", Math.PI / 4)
        );
    }

    @Test
    void varargMath() {
        assertAll(
            () -> testCalculation("min(1)", 1.0),
            () -> testCalculation("min(3, 1, 2)", 1.0),
            () -> testCalculation("max(2)", 2.0),
            () -> testCalculation("max(1, 3, 2)", 3.0)
        );
    }
}