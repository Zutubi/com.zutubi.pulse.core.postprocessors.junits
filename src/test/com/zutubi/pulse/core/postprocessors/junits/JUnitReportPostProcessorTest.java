package com.zutubi.pulse.core.postprocessors.junits;

import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;

import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.util.List;

/**
 * An example test suite for a Pulse test report post-processor plugin.
 * Illustrates a few alternative ways to verify test suites returned by the
 * post-processor.
 * 
 * All cases use the base class support for running a processor against an
 * input file.  Files are found on the classpath in the same package as this
 * class, named:
 * 
 *   <class name>.<test case name>.xml
 *   
 * For example:
 * 
 *   JUnitReportPostProcessorTest.testSimple.xml
 */
public class JUnitReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private JUnitReportPostProcessor pp = new JUnitReportPostProcessor(new JUnitReportPostProcessorConfiguration());

    public void testSimple() throws Exception
    {
        // In this test case, we manually walk through the returned test suite to verify it.
        // This is straightforward and flexible, but a little tedious.  This approach  is
        // only recommended when the flexibility is required.
        TestSuiteResult tests = runProcessorAndGetTests(pp);

        assertEquals(2, tests.getSuites().size());
        checkWarning(tests.getSuites().get(0), "com.zutubi.pulse.junit.EmptyTest", 91, "No tests found");

        TestSuiteResult suite = tests.getSuites().get(1);
        assertEquals("com.zutubi.pulse.junit.SimpleTest", suite.getName());
        assertEquals(90, suite.getDuration());

        List<TestCaseResult> children = suite.getCases();
        assertEquals(3, children.size());
        assertEquals(new TestCaseResult("testSimple", 0, PASS), children.get(0));
        assertEquals(new TestCaseResult("testAssertionFailure", 10, FAILURE,
                "junit.framework.AssertionFailedError: expected:<1> but was:<2>\n" +
                        "\tat com.zutubi.pulse.junit.SimpleTest.testAssertionFailure(Unknown Source)"),
                children.get(1));
        assertEquals(new TestCaseResult("testThrowException", 10, ERROR,
                "java.lang.RuntimeException: random message\n" +
                        "\tat com.zutubi.pulse.junit.SimpleTest.testThrowException(Unknown Source)"),
                children.get(2));
    }

    private void checkWarning(TestResult testResult, String name, long duration, String contents)
    {
        assertTrue(testResult instanceof TestSuiteResult);
        TestSuiteResult suite = (TestSuiteResult) testResult;
        assertEquals(name, suite.getName());
        assertEquals(duration, suite.getDuration());

        List<TestCaseResult> children = suite.getCases();
        assertEquals(1, children.size());
        TestCaseResult caseResult = children.get(0);
        assertEquals("warning", caseResult.getName());
        assertEquals(10, caseResult.getDuration());
        assertTrue(caseResult.getMessage().contains(contents));
    }

    public void testSingle() throws Exception
    {
        // This test case makes use of support to construct and compare test suites.
        // Where the suites differ, the assertion reports the first significant
        // difference.  This is the preferred method of testing as it is the easiest
        // both to read and write.
        assertEquals(
            buildSuite(null,
                buildSuite("com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest", 391,
                    new TestCaseResult("testSimple", (long) 291, PASS, null),
                    new TestCaseResult("testSkipped", (long) 0, SKIPPED, null),
                    new TestCaseResult("testFailure", (long) 10, FAILURE, "junit.framework.AssertionFailedError\n" +
                            "\tat\n" +
                            "        com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest.testFailure(JUnitReportPostProcessorTest.java:63)"),
                    new TestCaseResult("testError", (long) 0, ERROR, "java.lang.RuntimeException: whoops!\n" +
                            "\tat\n" +
                            "        com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorTest.testError(JUnitReportPostProcessorTest.java:68)")
                )
            ),
            runProcessorAndGetTests(pp)
        );
    }

    public void testNested() throws Exception
    {
        // Alternatively, you can mix manual verification with some support in the base.
        // Below, checkStatusCounts is used to quickly verify the summary of each suite.
        // Test suite support for finding cases is used to match up those of interest.
        TestSuiteResult tests = runProcessorAndGetTests(pp);
        TestSuiteResult suite = tests.findSuite("Outer");
        assertNotNull(suite);
        checkStatusCounts(suite, "Outer", 2, 0, 0, 0, 0);
        TestSuiteResult nested = suite.findSuite("Nested");
        checkStatusCounts(nested, "Nested", 2, 0, 0, 0, 0);
        assertEquals(new TestCaseResult("test1", -1, PASS, null), nested.findCase("test1"));
        assertEquals(new TestCaseResult("test2", -1, PASS, null), nested.findCase("test2"));
    }
}
