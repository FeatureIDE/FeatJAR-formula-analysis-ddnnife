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
package de.featjar.analysis.ddnnife;

import de.featjar.analysis.ddnnife.solver.DdnnifeWrapper;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.analysis.bool.ABooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanClauseList;
import java.time.Duration;
import java.util.List;

/**
 * Base class for analyses using a {@link SharpSatSolver}.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Sebastian Krieter
 */
public abstract class DdnnifeAnalysis<T> extends AComputation<T> {
    public static final Dependency<BooleanClauseList> BOOLEAN_CLAUSE_LIST =
            Dependency.newDependency(BooleanClauseList.class);
    public static final Dependency<ABooleanAssignment> ASSUMED_ASSIGNMENT =
            Dependency.newDependency(ABooleanAssignment.class);
    public static final Dependency<Duration> SAT_TIMEOUT = Dependency.newDependency(Duration.class);

    public DdnnifeAnalysis(IComputation<BooleanClauseList> booleanClauseList, Object... computations) {
        super(
                booleanClauseList,
                Computations.of(new BooleanAssignment()),
                Computations.of(Duration.ZERO),
                computations);
    }

    protected DdnnifeAnalysis(DdnnifeAnalysis<T> other) {
        super(other);
    }

    public DdnnifeWrapper initializeSolver(List<Object> dependencyList) {
        BooleanClauseList clauseList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        ABooleanAssignment assumedAssignment = ASSUMED_ASSIGNMENT.get(dependencyList);
        Duration timeout = SAT_TIMEOUT.get(dependencyList);
        FeatJAR.log().debug("initializing SAT4J");
        FeatJAR.log().debug("clauses %s", clauseList);
        FeatJAR.log().debug("assuming %s", assumedAssignment);

        DdnnifeWrapper solver = new DdnnifeWrapper(clauseList);
        solver.setAssumptions(assumedAssignment);
        solver.setTimeout(timeout);
        return solver;
    }
}
