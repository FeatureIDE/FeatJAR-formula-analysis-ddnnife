/*
 * Copyright (C) 2022 Sebastian Krieter
 *
 * This file is part of formula-analysis-sharpsat.
 *
 * formula-analysis-sharpsat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sharpsat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sharpsat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-sharpsat> for further information.
 */
package de.featjar.analysis.ddnnife;

import de.featjar.analysis.AbstractAnalysis;
import de.featjar.analysis.ddnnife.solver.DdnnifeWrapper;
import de.featjar.analysis.solver.SharpSatSolver;
import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.FormulaProvider;

/**
 * Base class for analyses using a {@link SharpSatSolver}.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Sebastian Krieter
 */
public abstract class DdnnifeAnalysis<T> extends AbstractAnalysis<T, DdnnifeWrapper, Formula> {

    public DdnnifeAnalysis() {
        super();
        solverInputProvider = FormulaProvider.empty();
    }

    @Override
    protected DdnnifeWrapper createSolver(Formula input) {
        return new DdnnifeWrapper(input);
    }

    @Override
    protected void prepareSolver(DdnnifeWrapper solver) {
        super.prepareSolver(solver);
    }
}
