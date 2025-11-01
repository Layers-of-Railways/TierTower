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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.slimeistdev.tier_tower.base.math.EvaluationContext;
import io.github.slimeistdev.tier_tower.base.math.EvaluationException;
import io.github.slimeistdev.tier_tower.base.math.ParseException;
import io.github.slimeistdev.tier_tower.base.math.parser.Parser;

import java.util.function.Consumer;

public interface Node {
    double evaluate(EvaluationContext context) throws EvaluationException;
    String repr();
    default String reprS() {
        return repr();
    }
    default void visitSelfAndChildren(Consumer<Node> consumer) {
        consumer.accept(this);
    }

    Codec<Node> CODEC = Codec.STRING.comapFlatMap(
        s -> {
            try {
                return DataResult.success(Parser.parse(s));
            } catch (ParseException e) {
                return DataResult.error(() -> "Failed to parse expression: " + s + " - " + e.getMessage());
            }
        },
        Node::repr
    ); // TODO: allow checking for allowed variables
}
