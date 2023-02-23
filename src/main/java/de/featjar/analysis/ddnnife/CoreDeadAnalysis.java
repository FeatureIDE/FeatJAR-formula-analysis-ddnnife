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
import de.featjar.clauses.LiteralList;
import de.featjar.util.data.Identifier;
import de.featjar.util.job.InternalMonitor;

/**
 * Finds core and dead features.
 *
 * @author Sebastian Krieter
 */
public class CoreDeadAnalysis extends DdnnifeAnalysis<LiteralList> {

    public static final Identifier<LiteralList> identifier = new Identifier<>();

    @Override
    public Identifier<LiteralList> getIdentifier() {
        return identifier;
    }

    @Override
    protected LiteralList analyze(DdnnifeWrapper solver, InternalMonitor monitor) throws Exception {
        return solver.core();
    }
}
