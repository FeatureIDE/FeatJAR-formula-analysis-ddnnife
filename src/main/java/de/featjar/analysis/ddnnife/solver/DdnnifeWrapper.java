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
package de.featjar.analysis.ddnnife.solver;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.bin.ddnnife.D4Binary;
import de.featjar.bin.ddnnife.DdnnifeBinary;
import de.featjar.formula.analysis.ISolver;
import de.featjar.formula.analysis.bool.ABooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanAssignment;
import de.featjar.formula.analysis.bool.BooleanClauseList;
import de.featjar.formula.analysis.bool.BooleanSolution;
import de.featjar.formula.io.dimacs.CnfDimacsFormat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DdnnifeWrapper implements ISolver, AutoCloseable {

    protected Duration timeout = Duration.ZERO;

    private Process process;

    private BufferedReader prcIn;
    private BufferedWriter prcOut;

    private Path ddnifeFile;

    private ABooleanAssignment assumptions;

    public DdnnifeWrapper(BooleanClauseList formula) {
        int features = formula.getVariableCount();
        try {
            Path d4File = Files.createTempFile("d4Input", ".dimacs");

            ddnifeFile = Files.createTempFile("ddnnifeInput", ".nnf");
            d4File.toFile().deleteOnExit();
            ddnifeFile.toFile().deleteOnExit();

            IO.save(formula, d4File, new CnfDimacsFormat());

            D4Binary extension = FeatJAR.extension(D4Binary.class);
            ProcessBuilder processBuilder = new ProcessBuilder(
                    extension.getExecutablePath().toString(),
                    "-i",
                    d4File.toString(),
                    "-m",
                    "ddnnf-compiler",
                    "--dump-ddnnf",
                    ddnifeFile.toString());
            FeatJAR.log().debug(() -> String.join(" ", processBuilder.command()));
            Process start = processBuilder.start();
            start.waitFor();
            Files.deleteIfExists(d4File);

            process = startProcess(ddnifeFile, features);
            prcIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
            prcOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            if (prcIn.ready()) {
                FeatJAR.log().error(prcIn.lines().reduce("", (s1, s2) -> s1 + s2 + "\n"));
                close();
            }
        } catch (Exception e) {
            FeatJAR.log().error(e);
            try {
                close();
            } catch (Exception e1) {
                FeatJAR.log().error(e);
            }
        }
    }

    private Process startProcess(Path ddnifeFile, int features) {
        try {
            if (features > -1) {
                DdnnifeBinary extension = FeatJAR.extension(DdnnifeBinary.class);
                return new ProcessBuilder(
                                extension.getExecutablePath().toString(),
                                ddnifeFile.toString(),
                                "-o",
                                Integer.toString(features),
                                "--stream")
                        .start();
            } else {
                return new ProcessBuilder(new DdnnifeBinary().getExecutableName(), ddnifeFile.toString(), "--stream")
                        .start();
            }
        } catch (IOException e) {
            FeatJAR.log().error(e);
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

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
        }
    }

    public Result<Boolean> hasSolution() {
        StringBuilder sb = new StringBuilder("sat");
        writeAssumptions(sb);
        return compute(sb.toString()).map("true"::equalsIgnoreCase);
    }

    public Result<BooleanSolution> getSolution() {
        StringBuilder sb = new StringBuilder("enum l 1");
        writeAssumptions(sb);
        return compute(sb.toString())
                .map(s -> new BooleanSolution(Arrays.stream(s.split("\\w+"))
                        .mapToInt(Integer::parseInt)
                        .toArray()));
    }

    public Result<BigInteger> countSolutions() {
        StringBuilder sb = new StringBuilder("count");
        writeAssumptions(sb);
        return compute(sb.toString()).map(BigInteger::new);
    }

    public Result<BooleanAssignment> core() {
        StringBuilder sb = new StringBuilder("core");
        writeAssumptions(sb);
        return compute(sb.toString())
                .map(s -> new BooleanAssignment(Arrays.stream(s.split("\\w+"))
                        .mapToInt(Integer::parseInt)
                        .toArray()));
    }

    private void writeAssumptions(StringBuilder sb) {
        if (assumptions != null && !assumptions.isEmpty()) {
            sb.append(" a");
            for (int assumption : assumptions.get()) {
                sb.append(assumption);
            }
        }
    }

    public ABooleanAssignment getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(ABooleanAssignment assumptions) {
        this.assumptions = assumptions;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        Objects.requireNonNull(timeout);
        FeatJAR.log().debug("setting timeout to " + timeout);
        this.timeout = timeout;
    }

    @Override
    public boolean isTimeoutOccurred() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() throws Exception {
        try {
            if (process != null && process.isAlive()) {
                prcOut.write("exit\n");
                prcOut.flush();
                process.waitFor(1000, TimeUnit.MILLISECONDS);
            }
        } catch (IOException | InterruptedException e) {
            FeatJAR.log().error(e);
        } finally {
            closeStream(prcIn);
            closeStream(prcOut);
            try {
                Files.deleteIfExists(ddnifeFile);
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }
}
