/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.util.List;

public class TestExecutorTest extends TestCase {

    private Model m;
    private TestCaseDescription tcd;

    @Override
    public void setUp() throws Exception {
        ToBreakRunner tbr = new ToBreakRunner("dig/setStateToTestResult.dig");
        List<Circuit.TestCase> tl = tbr.getCircuit().getTestCases();
        assertEquals(1, tl.size());
        tcd = tl.get(0).getTestCaseDescription();
        m = tbr.getModel();
    }

    public void testSetStateToTestResult0() throws Exception {
        new TestExecutor(tcd, m).executeTo(0);
        assertEquals(0, m.getOutput("S").getValue());
        assertEquals(0, m.getOutput("O").getValue());
    }

    public void testSetStateToTestResult12() throws Exception {
        new TestExecutor(tcd, m).executeTo(12);
        assertEquals(6, m.getOutput("S").getValue());
        assertEquals(0, m.getOutput("O").getValue());
    }

    public void testSetStateToTestResultCarry() throws Exception {
        new TestExecutor(tcd, m).executeTo(256 * 2 - 1);
        assertEquals(0, m.getOutput("S").getValue());
        assertEquals(1, m.getOutput("O").getValue());
    }

    public void testSetStateToTestResultError() throws Exception {
        new TestExecutor(tcd, m).executeTo(256 * 256 * 2 - 1);
        assertEquals(255, m.getOutput("S").getValue());
        assertEquals(0, m.getOutput("O").getValue());
    }

    /**
     * createColumnInfo() must report the real declared bit width (8 for S, an 8-bit output),
     * not a width guessed from the values seen so far. Row 0 alone already has S==0, which a
     * value-based guess (as previously used as a fallback in the waveform viewer) would
     * misinterpret as a single-bit signal.
     */
    public void testColumnInfoUsesDeclaredBitWidthNotObservedValue() throws Exception {
        TestExecutor executor = new TestExecutor(tcd, m);
        executor.executeTo(0);
        assertEquals(0, m.getOutput("S").getValue());

        List<String> names = executor.getNames();
        ValueTable.ColumnInfo[] info = executor.createColumnInfo();

        assertEquals(8, info[names.indexOf("A")].getBits());
        assertEquals(8, info[names.indexOf("B")].getBits());
        assertEquals(1, info[names.indexOf("C")].getBits());
        assertEquals(8, info[names.indexOf("S")].getBits());
        assertEquals(1, info[names.indexOf("O")].getBits());
    }

}