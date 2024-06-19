/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ddnnife.
 *
 * formula-analysis-ddnnife is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ddnnife is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ddnnife. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-ddnnife> for further information.
 */
package de.featjar.analysis.ddnnife.cli;

import de.featjar.analysis.ddnnife.computation.ComputeDdnnifeWrapper;
import de.featjar.analysis.ddnnife.computation.ComputeTWiseSampleDdnnife;
import de.featjar.base.cli.Option;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.List;
import java.util.Optional;

public class TWiseCommand extends ADdnnifeAnalysisCommand<List<BooleanSolution>, BooleanAssignment> {

    public static final Option<Integer> T_OPTION = new Option<>("t", Option.IntegerParser) //
            .setDescription("Value for parameter t") //
            .setDefaultValue(2);

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes a t-wise sample for a given formula using ddnnife");
    }

    @Override
    public IComputation<List<BooleanSolution>> newAnalysis(ComputeDdnnifeWrapper formula) {
        return formula.map(ComputeTWiseSampleDdnnife::new)
                .set(ComputeTWiseSampleDdnnife.T, optionParser.get(T_OPTION))
                .set(ComputeTWiseSampleDdnnife.RANDOM_SEED, optionParser.get(RANDOM_SEED_OPTION));
    }

    @Override
    public String serializeResult(List<BooleanSolution> assignments) {
        StringBuilder sb = new StringBuilder();
        for (BooleanSolution assignment : assignments) {
            sb.append(assignment.print());
            sb.append('\n');
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("t-wise-ddnnife");
    }
}
