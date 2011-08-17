package com.zutubi.pulse.core.postprocessors.junits;

import com.zutubi.pulse.core.postprocessors.api.XMLTestReportPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for instances of {@link com.zutubi.pulse.core.postprocessors.junits.JUnitReportPostProcessor}.
 */
@SymbolicName("zutubi.junitsReportPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnFailure", "suite", "resolveConflicts", "expectedFailureFile", "suiteElement", "caseElement", "errorElement", "failureElement", "skippedElement", "classAttribute", "messageAttribute", "nameAttribute", "packageAttribute", "timeAttribute"})
public class JUnitReportPostProcessorConfiguration extends XMLTestReportPostProcessorConfigurationSupport
{
    public JUnitReportPostProcessorConfiguration()
    {
        this(JUnitReportPostProcessor.class);
    }

    public JUnitReportPostProcessorConfiguration(Class<? extends JUnitReportPostProcessor> postProcessorType)
    {
        this(postProcessorType, "JUnit");
    }

    public JUnitReportPostProcessorConfiguration(Class<? extends JUnitReportPostProcessor> postProcessorType, String reportType)
    {
        super(postProcessorType, reportType);
    }
}
