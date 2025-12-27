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

package io.github.slimeistdev.tier_tower.base.math.parser;

import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    void testParse(String expression, String expected) {
        try {
            Node result = Parser.parse(expression);
            assertEquals(expected, result.reprS());

            Node roundTrip = Parser.parse(result.repr());
            assertEquals(expected, roundTrip.reprS());
        } catch (ParseException e) {
            fail("ParseException should not have been thrown: " + e.getMessage());
        }
    }

    void testParseFail(String expression, String expectedMessage) {
        try {
            String result = Parser.parse(expression).reprS();
            fail("ParseException(\""+expectedMessage+"\") should have been thrown, parsed: " + result);
        }  catch (ParseException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    void parseSimple() {
        assertAll(
            () -> testParse("1", "1.0"),
            () -> testParse("2.3", "2.3"),

            () -> testParse("1 + 2", "(+ 1.0 2.0)"),
            () -> testParse("3 * 4", "(* 3.0 4.0)"),
            () -> testParse("5 - 6", "(- 5.0 6.0)"),
            () -> testParse("7 / 8", "(/ 7.0 8.0)"),
            () -> testParse("9 ^ 10", "(^ 9.0 10.0)")
        );
    }

    @Test
    void parseSingleClassPrecedence() {
        assertAll(
            () -> testParse("1 + 2 + 3", "(+ (+ 1.0 2.0) 3.0)"),
            () -> testParse("1 + 2 - 3", "(- (+ 1.0 2.0) 3.0)"),
            () -> testParse("1 * 2 / 3", "(/ (* 1.0 2.0) 3.0)"),
            () -> testParse("2 ^ 3 ^ 4", "(^ 2.0 (^ 3.0 4.0))")
        );
    }

    @Test
    void parseMultiClassPrecedence() {
        assertAll(
            () -> testParse("1 + 2 * 3", "(+ 1.0 (* 2.0 3.0))"),
            () -> testParse("1 + 2 * 3 * 4", "(+ 1.0 (* (* 2.0 3.0) 4.0))"),
            () -> testParse("1 + 2 / 3", "(+ 1.0 (/ 2.0 3.0))"),
            () -> testParse("2 ^ 3 * 4 + 1", "(+ (* (^ 2.0 3.0) 4.0) 1.0)"),
            () -> testParse("2 ^ 3 ^ 4 * 5", "(* (^ 2.0 (^ 3.0 4.0)) 5.0)")
        );
    }

    @Test()
    void parseParentheses() {
        assertAll(
            () -> testParse("(1)", "1.0"),
            () -> testParse("(((1)))", "1.0"),
            () -> testParse("(1 + 2)", "(+ 1.0 2.0)"),
            () -> testParse("1 + (2 + 3)", "(+ 1.0 (+ 2.0 3.0))"),
            () -> testParse("(1 + 2) * 3", "(* (+ 1.0 2.0) 3.0)"),
            () -> testParse("1 * ((2 + 3) / 4)", "(* 1.0 (/ (+ 2.0 3.0) 4.0))"),
            () -> testParse(" 2 ^ (3 * 4)", "(^ 2.0 (* 3.0 4.0))")
        );
    }

    @Test
    void parseMismatchedParentheses() {
        assertAll(
            () -> testParseFail("1 + (2 + 3", "Missing closing parenthesis"),
            () -> testParseFail("1 + 2 + 3)", "Parse skipped tokens, next: )"),
            () -> testParseFail("1 + )2 + 3(", "Illegal token: )"),
            () -> testParseFail("()", "Illegal token: )"),
            () -> testParseFail("1 (+ 2 * 3)", "Illegal token: (")
        );
    }

    @Test
    void parseMalformedOperations() {
        assertAll(
            () -> testParseFail(" 1 + + 2", "Illegal token: Operator[symbol=+, leftBindingPower=10, rightBindingPower=11]"),
            () -> testParseFail(" 1 +* 2", "Illegal token: Operator[symbol=*, leftBindingPower=20, rightBindingPower=21]")
        );
    }

    @Test
    void parseVariables() {
        assertAll(
            () -> testParse("lorem + 2 * 3", "(+ lorem (* 2.0 3.0))"),
            () -> testParse("with_underscore_and_1 + 2", "(+ with_underscore_and_1 2.0)")
        );
    }

    @Test
    void parseMalformedVariables() {
        assertAll(
            () -> testParseFail("0lorem + 1", "Illegal token: Variable[name=lorem]"),
            () -> testParseFail("_ipsum * 2", "Invalid character: _"),
            () -> testParseFail("dolor$sit", "Invalid character: $")
        );
    }

    @Test
    void parseFunctionCalls() {
        assertAll(
            () -> testParse("f()", "(fn<f>)"),
            () -> testParse("h(1)", "(fn<h> 1.0)"),
            () -> testParse("max(1, 2)", "(fn<max> 1.0 2.0)"),
            () -> testParse("sum(a, b, c + d)", "(fn<sum> a b (+ c d))"),
            () -> testParse("outer(inner(1, 2), 3)", "(fn<outer> (fn<inner> 1.0 2.0) 3.0)")
        );
    }

    @Test
    void parseFunctionCallsInExpressions() {
        assertAll(
            () -> testParse("1 + f(2) * 3", "(+ 1.0 (* (fn<f> 2.0) 3.0))"),
            () -> testParse("g(1 + 2, 3 * 4)", "(fn<g> (+ 1.0 2.0) (* 3.0 4.0))"),
            () -> testParse("h(x) ^ 2", "(^ (fn<h> x) 2.0)"),
            () -> testParse("a + outer(b, inner(c + d, e * f))", "(+ a (fn<outer> b (fn<inner> (+ c d) (* e f))))")
        );
    }
}