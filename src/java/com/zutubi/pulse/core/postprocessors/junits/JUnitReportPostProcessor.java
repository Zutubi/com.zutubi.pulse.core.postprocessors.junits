package com.zutubi.pulse.core.postprocessors.junits;

import com.zutubi.pulse.core.postprocessors.api.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

/**
 * Post-processor for junit (and compatible) XML reports.
 */
public class JUnitReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_SUITES  = "testsuites";
    private static final String ELEMENT_SUITE   = "testsuite";
    private static final String ELEMENT_CASE    = "testcase";
    private static final String ELEMENT_ERROR   = "error";
    private static final String ELEMENT_FAILURE = "failure";
    private static final String ELEMENT_SKIPPED = "skipped";

    private static final String ATTRIBUTE_CLASS   = "classname";
    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME    = "name";
    private static final String ATTRIBUTE_PACKAGE = "package";
    private static final String ATTRIBUTE_TIME    = "time";

    public JUnitReportPostProcessor(JUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    @Override
    public JUnitReportPostProcessorConfiguration getConfig()
    {
        return (JUnitReportPostProcessorConfiguration) super.getConfig();
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        while (nextSiblingTag(reader, ELEMENT_SUITE, ELEMENT_SUITES))
        {
            if (isElement(ELEMENT_SUITE, reader))
            {
                processSuite(reader, tests);
            }
            else if (isElement(ELEMENT_SUITES, reader))
            {
                processSuites(reader, tests);
            }
        }
    }

    protected void processSuites(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_SUITES, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_SUITE))
        {
            processSuite(reader, tests);
        }

        expectEndTag(ELEMENT_SUITES, reader);
    }

    private void processSuite(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_SUITE, reader);
        Map<String, String> attributes = getAttributes(reader);

        String name = getTestSuiteName(attributes);
        if (name.length() == 0)
        {
            nextElement(reader);
            return;
        }

        long duration = getDuration(attributes);
        TestSuiteResult suite = new TestSuiteResult(name, duration);
        tests.addSuite(suite);

        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_SUITE, ELEMENT_CASE))
        {
            if (isElement(ELEMENT_SUITE, reader))
            {
                processSuite(reader, suite);
            }
            else if (isElement(ELEMENT_CASE, reader))
            {
                processCase(reader, suite);
            }
        }

        expectEndTag(ELEMENT_SUITE, reader);
        nextTagOrEnd(reader);
    }

    protected String getTestSuiteName(Map<String, String> attributes)
    {
        String name = "";
        String attr = attributes.get(ATTRIBUTE_PACKAGE);
        if (attr != null)
        {
            name += attr + '.';
        }

        attr = attributes.get(ATTRIBUTE_NAME);
        if (attr != null)
        {
            name += attr;
        }
        return name;
    }

    private void processCase(XMLStreamReader reader, TestSuiteResult suite) throws XMLStreamException
    {
        expectStartTag(ELEMENT_CASE, reader);

        Map<String, String> attributes = getAttributes(reader);
        String name = getTestCaseName(attributes);
        if (name == null)
        {
            nextElement(reader);
            return;
        }

        String className = attributes.get(ATTRIBUTE_CLASS);
        if (className != null && !suite.getName().equals(className))
        {
            name = className + "." + name;
        }

        long duration = getDuration(attributes);
        TestCaseResult caseResult = new TestCaseResult(name, duration, getTestCaseImmediateStatus(attributes));
        suite.addCase(caseResult);
        nextTagOrEnd(reader);

        if (nextSiblingTag(reader, ELEMENT_ERROR, ELEMENT_FAILURE, ELEMENT_SKIPPED))
        {
            String tagName = reader.getLocalName();
            if (tagName.equals(ELEMENT_ERROR))
            {
                caseResult.setStatus(TestStatus.ERROR);
                caseResult.setMessage(getMessage(reader));
                nextTagOrEnd(reader);
            }
            else if (tagName.equals(ELEMENT_FAILURE))
            {
                caseResult.setStatus(TestStatus.FAILURE);
                caseResult.setMessage(getMessage(reader));
                nextTagOrEnd(reader);
            }
            else if (tagName.equals(ELEMENT_SKIPPED))
            {
                caseResult.setStatus(TestStatus.SKIPPED);
                nextElement(reader);
            }
        }

        // skip to the end.
        while (reader.isStartElement())
        {
            nextElement(reader);
        }

        expectEndTag(ELEMENT_CASE, reader);
        nextTagOrEnd(reader);
    }

    protected String getTestCaseName(Map<String, String> attributes)
    {
        return attributes.get(ATTRIBUTE_NAME);
    }

    protected TestStatus getTestCaseImmediateStatus(Map<String, String> attributes)
    {
        return TestStatus.PASS;
    }

    private String getMessage(XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        String message = attributes.get(ATTRIBUTE_MESSAGE);

        String elementText = reader.getElementText();
        if (elementText != null && elementText.length() > 0)
        {
            message = elementText.trim();
        }

        return (message != null && message.length() == 0) ? null : message;
    }

    private long getDuration(Map<String, String> attributes)
    {
        long duration = TestResult.DURATION_UNKNOWN;
        String attr = attributes.get(ATTRIBUTE_TIME);
        if (attr != null)
        {
            try
            {
                double time = Double.parseDouble(attr);
                duration = (long) (time * 1000);
            }
            catch (NumberFormatException e)
            {
                // No matter, leave time out
            }
        }
        return duration;
    }
}
