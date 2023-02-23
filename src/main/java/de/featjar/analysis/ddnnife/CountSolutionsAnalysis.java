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

import de.featjar.analysis.ddnnife.solver.DdnnifeWrapper;
import de.featjar.util.data.Identifier;
import de.featjar.util.job.InternalMonitor;
import java.math.BigInteger;

/**
 * Counts the number of valid solutions to a formula.
 *
 * @author Sebastian Krieter
 */
public class CountSolutionsAnalysis extends DdnnifeAnalysis<BigInteger> {

    public static final Identifier<BigInteger> identifier = new Identifier<>();

    @Override
    public Identifier<BigInteger> getIdentifier() {
        return identifier;
    }

    @Override
    protected BigInteger analyze(DdnnifeWrapper solver, InternalMonitor monitor) throws Exception {
        return solver.countSolutions();
    }
}
