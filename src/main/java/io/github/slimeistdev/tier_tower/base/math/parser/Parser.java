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
import io.github.slimeistdev.tier_tower.base.math.ast.CallNode;
import io.github.slimeistdev.tier_tower.base.math.ast.LiteralNode;
import io.github.slimeistdev.tier_tower.base.math.ast.Node;
import io.github.slimeistdev.tier_tower.base.math.ast.OpNode;
import io.github.slimeistdev.tier_tower.base.math.ast.VariableNode;
import io.github.slimeistdev.tier_tower.base.math.lexer.Lexer;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.Comma;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.End;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.Literal;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.Operator;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.ParenClose;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.ParenOpen;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.Token;
import io.github.slimeistdev.tier_tower.base.math.lexer.tokens.Variable;
import io.github.slimeistdev.tier_tower.base.math.operations.BiOp;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static Node parse(String expression) throws ParseException {
        List<Token> tokens = Lexer.lex(expression);
        TokenStream tokenStream = new TokenStream(tokens);

        Node parsed = parse(tokenStream, 0);

        if (tokenStream.hasNext()) {
            throw new ParseException("Parse skipped tokens, next: " + tokenStream.next());
        }

        return parsed;
    }

    private static Node parseCall(TokenStream tokens, Variable functionName) throws ParseException {
        if (tokens.peek() instanceof ParenClose) {
            tokens.next();
            return new CallNode(functionName.name(), new Node[0]);
        }

        List<Node> args = new ArrayList<>();
        while (true) {
            Node arg = parse(tokens, 0);
            args.add(arg);

            Token next = tokens.next();
            if (next instanceof ParenClose) {
                break;
            } else if (!(next instanceof Comma)) {
                throw new ParseException("Expected ',' or ')', got: " + next);
            }
        }

        return new CallNode(functionName.name(), args.toArray(new Node[0]));
    }

    // Recursive Pratt parser https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html
    @SuppressWarnings("InfiniteRecursion") // silly IntelliJ, can't figure out that tokens.next() will eventually exhaust the tokens
    static Node parse(TokenStream tokens, int minBP) throws ParseException {
        // minBP exists so that when parsing recursively, we know when the outer invocation wants to bind on the left

        Token token = tokens.next();
        Node lhs;
        if (token instanceof Literal literal) {
            lhs = new LiteralNode(literal.value());
        } else if (token instanceof Variable variable) {
            if (tokens.peek() instanceof ParenOpen) {
                tokens.next();
                lhs = parseCall(tokens, variable);
            } else {
                lhs = new VariableNode(variable.name());
            }
        } else if (token instanceof ParenOpen) {
            lhs = parse(tokens, 0);
            if (!tokens.hasNext() || !(tokens.next() instanceof ParenClose)) {
                throw new ParseException("Missing closing parenthesis");
            }
        } else {
            throw new ParseException("Illegal token: " + token);
        }

        // bind on the right until the binding power is less than the minimum binding power
        while (true) {
            if (!tokens.hasNext()) {
                break;
            }

            Token nextToken = tokens.peek();
            if (nextToken instanceof ParenClose || nextToken instanceof Comma) {
                break;
            }
            if (!(nextToken instanceof Operator op)) {
                throw new ParseException("Illegal token: " + nextToken);
            }

            int leftBP = op.leftBindingPower();
            int rightBP = op.rightBindingPower();

            if (leftBP < minBP) {
                break;
            }

            tokens.next(); // actually consume the operator token
            Node rhs = parse(tokens, rightBP);

            lhs = new OpNode(
                lhs,
                BiOp.of(op.symbol()),
                rhs
            );
        }

        return lhs;
    }

    private static class TokenStream {
        private final List<Token> tokens;
        private int position;

        public TokenStream(List<Token> tokens) {
            this.tokens = tokens;
            this.position = 0;
        }

        public boolean hasNext() {
            return position < tokens.size();
        }

        public Token next() throws ParseException {
            if (position >= tokens.size()) {
                throw new ParseException("No more tokens available");
            }
            return tokens.get(position++);
        }

        @SuppressWarnings("RedundantThrows")
        public Token peek() throws ParseException {
            if (position >= tokens.size()) {
                return End.INSTANCE;
            }
            return tokens.get(position);
        }
    }
}
