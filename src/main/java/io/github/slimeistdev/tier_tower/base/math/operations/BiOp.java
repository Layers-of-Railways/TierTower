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

package io.github.slimeistdev.tier_tower.base.math.operations;

@SuppressWarnings("Convert2MethodRef")
public enum BiOp {
    ADD("+", (left, right) -> left + right),
    SUB("-", (left, right) -> left - right),
    MUL("*", (left, right) -> left * right),
    DIV("/", (left, right) -> left / right),
    EXP("^", (left, right) -> Math.pow(left, right))
    ;

    public final String symbol;
    private final BiOpFunction function;

    BiOp(String symbol, BiOpFunction function) {
        this.symbol = symbol;
        this.function = function;
    }

    public static BiOp of(String symbol) {
        return switch (symbol) {
            case "+" -> ADD;
            case "-" -> SUB;
            case "*" -> MUL;
            case "/" -> DIV;
            case "^" -> EXP;
            default -> throw new IllegalArgumentException("Unknown operator: " + symbol);
        };
    }

    public double compute(double left, double right) {
        return function.compute(left, right);
    }

    public interface BiOpFunction {
        double compute(double left, double right);
    }
}
