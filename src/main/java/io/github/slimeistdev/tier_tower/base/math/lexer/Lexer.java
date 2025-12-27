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

package io.github.slimeistdev.tier_tower.base.math.lexer;

import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Lexer {
    private static final Pattern VARIABLE_START = Pattern.compile("[a-zA-Z]");
    private static final Pattern VARIABLE_PART = Pattern.compile("[a-zA-Z0-9_]");

    public static List<Token> lex(final String input) throws ParseException {
        final StringStream i = new StringStream(input);
        final List<Token> tokens = new ArrayList<>();

        while (i.hasNext()) {
            final String c = i.next();

            Token token = switch (c) {
                case " " -> null;

                case "(" -> ParenOpen.INSTANCE;
                case "," -> Comma.INSTANCE;
                case ")" -> ParenClose.INSTANCE;

                case "+", "-" -> new Operator(c, 10, 11);
                case "*", "/" -> new Operator(c, 20, 21);
                case "^"      -> new Operator(c, 31, 30);

                case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> {
                    StringBuilder number = new StringBuilder(c);
                    while (i.hasNext() && (Character.isDigit(i.peek().charAt(0)) || i.peek().equals("."))) {
                        number.append(i.next());
                    }
                    double value;
                    try {
                        value = Double.parseDouble(number.toString());
                    } catch (NumberFormatException e) {
                        throw new ParseException("Invalid number format: " + number, e);
                    }
                    yield new Literal(value);
                }

                default -> {
                    if (VARIABLE_START.matcher(c).matches()) {
                        StringBuilder variable = new StringBuilder(c);
                        while (i.hasNext() && VARIABLE_PART.matcher(i.peek()).matches()) {
                            variable.append(i.next());
                        }
                        yield new Variable(variable.toString());
                    }

                    throw new ParseException("Invalid character: " + c);
                }
            };

            if (token != null) tokens.add(token);
        }

        return tokens;
    }

    private static class StringStream {
        private final String input;
        private int position;

        public StringStream(String input) {
            this.input = input;
            this.position = 0;
        }

        public String next() {
            if (position >= input.length()) {
                throw new IndexOutOfBoundsException("No more characters to read");
            }
            return input.substring(position++, position);
        }

        public String peek() {
            return peek(0);
        }

        public String peek(int offset) {
            if (position >= input.length()) {
                throw new IndexOutOfBoundsException("No more characters to peek");
            }
            return input.substring(position + offset, position + offset + 1);
        }

        public boolean hasNext() {
            return position < input.length();
        }
    }
}
