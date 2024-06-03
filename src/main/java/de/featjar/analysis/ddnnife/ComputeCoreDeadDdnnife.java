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
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import java.util.List;

/**
 * Finds core and dead features.
 *
 * @author Sebastian Krieter
 */
public class ComputeCoreDeadDdnnife extends DdnnifeAnalysis<BooleanAssignment> {

    public ComputeCoreDeadDdnnife(IComputation<DdnnifeWrapper> ddnnifeWrapper) {
        super(ddnnifeWrapper);
    }

    protected ComputeCoreDeadDdnnife(ComputeCoreDeadDdnnife other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignment> compute(List<Object> dependencyList, Progress progress) {
        return setup(dependencyList).core();
    }
}
