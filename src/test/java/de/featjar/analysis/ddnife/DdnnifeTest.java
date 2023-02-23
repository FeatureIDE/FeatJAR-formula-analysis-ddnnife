package de.featjar.analysis.ddnife;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.analysis.ddnnife.CountSolutionsAnalysis;
import de.featjar.formula.ModelRepresentation;
import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.Formulas;
import de.featjar.formula.structure.atomic.literal.Literal;
import de.featjar.formula.structure.atomic.literal.VariableMap;
import de.featjar.formula.structure.compound.And;
import de.featjar.formula.structure.compound.Biimplies;
import de.featjar.formula.structure.compound.Implies;
import de.featjar.formula.structure.compound.Or;
import de.featjar.util.data.Result;
import de.featjar.util.extension.ExtensionLoader;
import de.featjar.util.logging.Logger;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DdnnifeTest {

    static {
        ExtensionLoader.load();
        Logger.setPrintStackTrace(true);
    }

    private static final Path modelDirectory = Paths.get("src/test/resources/testFeatureModels");

    private static final List<String> modelNames = Arrays.asList( //
            "basic", //
            "simple", //
            "car", //
            "gpl_medium_model", //
            "500-100");

    private static ModelRepresentation load(final Path modelFile) {
        return ModelRepresentation.load(modelFile) //
                .orElseThrow(p -> new IllegalArgumentException(
                        p.isEmpty() ? null : p.get(0).toException()));
    }

    @Test
    public void count() {
        final VariableMap variables = new VariableMap();
        final Literal a = variables.createLiteral("a");
        final Literal b = variables.createLiteral("b");
        final Literal c = variables.createLiteral("c");

        final Implies implies1 = new Implies(a, b);
        final Or or = new Or(implies1, c);
        final Biimplies equals = new Biimplies(a, b);
        final And and = new And(equals, c);
        final Implies formula = new Implies(or, and);

        final Formula cnfFormula = Formulas.toCNF(formula).get();
        final ModelRepresentation rep = new ModelRepresentation(cnfFormula);

        final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
        final Result<?> result = rep.getResult(analysis);
        result.orElse(Logger::logProblems);
        assertTrue(result.isPresent());
        assertEquals(BigInteger.valueOf(3), result.get());
    }

    @Test
    public void count2() {
        final ModelRepresentation rep = load(modelDirectory.resolve(modelNames.get(3) + ".xml"));

        final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
        final Result<?> result = rep.getResult(analysis);
        result.orElse(Logger::logProblems);
        assertTrue(result.isPresent());
        assertEquals(BigInteger.valueOf(960), result.get());
    }
}
