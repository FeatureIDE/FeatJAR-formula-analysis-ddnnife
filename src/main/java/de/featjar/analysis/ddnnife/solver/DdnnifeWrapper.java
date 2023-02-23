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
package de.featjar.analysis.ddnnife.solver;

import de.featjar.analysis.solver.DynamicFormula;
import de.featjar.analysis.solver.SharpSatSolver;
import de.featjar.analysis.solver.SolutionSolver;
import de.featjar.bin.ddnnife.D4Binary;
import de.featjar.bin.ddnnife.DdnnifeBinary;
import de.featjar.clauses.LiteralList;
import de.featjar.clauses.LiteralList.Order;
import de.featjar.formula.ModelRepresentation;
import de.featjar.formula.io.dimacs.DIMACSFormat;
import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.FormulaProvider.CNF;
import de.featjar.formula.structure.atomic.Assignment;
import de.featjar.formula.structure.atomic.VariableAssignment;
import de.featjar.formula.structure.atomic.literal.VariableMap;
import de.featjar.util.data.Pair;
import de.featjar.util.data.Result;
import de.featjar.util.io.IO;
import de.featjar.util.logging.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DdnnifeWrapper implements SolutionSolver<LiteralList>, SharpSatSolver {

    private Process process;

    private BufferedReader prcIn;
    private BufferedWriter prcOut;

    private Path ddnifeFile;

    private final VariableAssignment assumptions;
    private final VariableMap variables;

    public DdnnifeWrapper(Formula formula) {
        variables = formula.getVariableMap().orElseGet(VariableMap::new);
        assumptions = new VariableAssignment(variables);

        int features = variables.getVariableCount();
        try {
            Path d4File = Files.createTempFile("d4Input", ".dimacs");

            ddnifeFile = Files.createTempFile("ddnnifeInput", ".nnf");
            d4File.toFile().deleteOnExit();
            ddnifeFile.toFile().deleteOnExit();
            IO.save(new ModelRepresentation(formula).get(CNF.fromFormula()), d4File, new DIMACSFormat());

            Process start = new ProcessBuilder(
                            new D4Binary().getPath().toString(),
                            "-i",
                            d4File.toString(),
                            "-m",
                            "ddnnf-compiler",
                            "--dump-ddnnf",
                            ddnifeFile.toString())
                    .start();
            start.waitFor();
            Files.deleteIfExists(d4File);

            process = startProcess(ddnifeFile, features);
            prcIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
            prcOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            if (prcIn.ready()) {
                Logger.logError(prcIn.lines().reduce("", (s1, s2) -> s1 + s2 + "\n"));
                endProcess();
            }
        } catch (IOException | InterruptedException e) {
            Logger.logError(e);
            endProcess();
        }
    }

    private Process startProcess(Path ddnifeFile, int features) {
        try {
            if (features > -1) {
                return new ProcessBuilder(
                                new DdnnifeBinary().getPath().toString(),
                                ddnifeFile.toString(),
                                "-o",
                                Integer.toString(features),
                                "--stream")
                        .start();
            } else {
                return new ProcessBuilder(new DdnnifeBinary().getPath().toString(), ddnifeFile.toString(), "--stream")
                        .start();
            }
        } catch (IOException e) {
            Logger.logError(e);
            return null;
        }
    }

    public Result<String> compute(String query) {
        if (process != null && process.isAlive()) {
            try {
                prcOut.write(query + "\n");
                prcOut.flush();
                return Result.of(prcIn.readLine());
            } catch (IOException e) {
                return Result.empty(e);
            }
        } else {
            return Result.empty(new Exception("Process was terminated!"));
        }
    }

    public void endProcess() {
        try {
            if (process != null && process.isAlive()) {
                prcOut.write("exit\n");
                prcOut.flush();
                process.waitFor(1000, TimeUnit.MILLISECONDS);
            }
        } catch (IOException | InterruptedException e) {
            Logger.logError(e);
        } finally {
            closeStream(prcIn);
            closeStream(prcOut);
            try {
                Files.deleteIfExists(ddnifeFile);
            } catch (IOException e) {
                Logger.logError(e);
            }
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Logger.logError(e);
            }
        }
    }

    @Override
    public Assignment getAssumptions() {
        return assumptions;
    }

    @Override
    public DynamicFormula<?> getDynamicFormula() {
        return new DynamicFormula<Object>() {

            @Override
            public List<Object> getConstraints() {
                return Collections.emptyList();
            }

            @Override
            public VariableMap getVariableMap() {
                return variables;
            }

            @Override
            public List<Object> push(Formula clause) {
                return Collections.emptyList();
            }

            @Override
            public Object peek() {
                return null;
            }

            @Override
            public Object pop() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public void remove(Object constraint) {}
        };
    }

    @Override
    public VariableMap getVariables() {
        return variables;
    }

    @Override
    public SatResult hasSolution() {
        StringBuilder sb = new StringBuilder("sat");
        List<Pair<Integer, Object>> all = assumptions.getAll();
        if (!all.isEmpty()) {
            sb.append(" a");
            for (Pair<Integer, Object> assumption : all) {
                sb.append((boolean) assumption.getValue() ? assumption.getKey() : -assumption.getKey());
            }
        }
        Result<String> compute = compute(sb.toString());
        if (compute.isPresent()) {
            if ("true".equalsIgnoreCase(compute.get())) {
                return SatResult.TRUE;
            } else {
                return SatResult.FALSE;
            }
        } else {
            return SatResult.TIMEOUT;
        }
    }

    @Override
    public LiteralList getSolution() {
        StringBuilder sb = new StringBuilder("enum l 1");
        List<Pair<Integer, Object>> all = assumptions.getAll();
        if (!all.isEmpty()) {
            sb.append(" a");
            for (Pair<Integer, Object> assumption : all) {
                sb.append((boolean) assumption.getValue() ? assumption.getKey() : -assumption.getKey());
            }
        }
        Result<String> compute = compute(sb.toString());
        if (compute.isPresent()) {
            int[] array = Arrays.stream(compute.get().split("\\w+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            return new LiteralList(array, Order.INDEX, false);
        } else {
            return null;
        }
    }

    @Override
    public BigInteger countSolutions() {
        StringBuilder sb = new StringBuilder("count");
        List<Pair<Integer, Object>> all = assumptions.getAll();
        if (!all.isEmpty()) {
            sb.append(" a");
            for (Pair<Integer, Object> assumption : all) {
                sb.append((boolean) assumption.getValue() ? assumption.getKey() : -assumption.getKey());
            }
        }
        Result<String> compute = compute(sb.toString());
        if (compute.isPresent()) {
            return new BigInteger(compute.get());
        } else {
            return null;
        }
    }

    public LiteralList core() {
        StringBuilder sb = new StringBuilder("core");
        List<Pair<Integer, Object>> all = assumptions.getAll();
        if (!all.isEmpty()) {
            sb.append(" a");
            for (Pair<Integer, Object> assumption : all) {
                sb.append((boolean) assumption.getValue() ? assumption.getKey() : -assumption.getKey());
            }
        }
        Result<String> compute = compute(sb.toString());
        if (compute.isPresent()) {
            int[] array = Arrays.stream(compute.get().split("\\w+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            return new LiteralList(array, Order.NATURAL);
        } else {
            return null;
        }
    }
}
