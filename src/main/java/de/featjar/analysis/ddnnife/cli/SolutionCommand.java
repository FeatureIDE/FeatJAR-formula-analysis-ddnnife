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
import de.featjar.analysis.ddnnife.computation.ComputeSolutionDdnnife;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.io.csv.BooleanSolutionListCSVFormat;
import java.util.List;
import java.util.Optional;

public class SolutionCommand extends ADdnnifeAnalysisCommand<BooleanSolution, BooleanAssignment> {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes a solution for a given formula using ddnnife");
    }

    @Override
    public IComputation<BooleanSolution> newAnalysis(OptionList optionParser, ComputeDdnnifeWrapper formula) {
        return formula.map(ComputeSolutionDdnnife::new);
    }

    @Override
    protected Object getOuputObject(BooleanSolution assignment) {
        return new BooleanAssignmentGroups(VariableMap.of(inputFormula), List.of(List.of(assignment)));
    }

    @Override
    protected IFormat<?> getOuputFormat() {
        return new BooleanSolutionListCSVFormat();
    }

    @Override
    public String serializeResult(BooleanSolution assignment) {
        return assignment.print();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("solution-ddnnife");
    }
}
