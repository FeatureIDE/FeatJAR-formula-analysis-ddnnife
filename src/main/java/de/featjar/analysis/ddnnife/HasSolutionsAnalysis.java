/*
 * Copyright (C) 2023 Sebastian Krieter
 *
 * This file is part of formula-analysis-ddnnife.
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
 * See <https://github.com/FeatJAR/formula-analysis-sharpsat> for further information.
 */
package de.featjar.analysis.ddnnife;

import de.featjar.analysis.ddnnife.solver.DdnnifeWrapper;
import de.featjar.analysis.solver.SatSolver;
import de.featjar.analysis.solver.SatSolver.SatResult;
import de.featjar.util.data.Identifier;
import de.featjar.util.job.InternalMonitor;

/**
 * Counts the number of valid solutions to a formula.
 *
 * @author Sebastian Krieter
 */
public class HasSolutionsAnalysis extends DdnnifeAnalysis<SatSolver.SatResult> {

    public static final Identifier<SatResult> identifier = new Identifier<>();

    @Override
    public Identifier<SatSolver.SatResult> getIdentifier() {
        return identifier;
    }

    @Override
    protected SatSolver.SatResult analyze(DdnnifeWrapper solver, InternalMonitor monitor) throws Exception {
        return solver.hasSolution();
    }
}
