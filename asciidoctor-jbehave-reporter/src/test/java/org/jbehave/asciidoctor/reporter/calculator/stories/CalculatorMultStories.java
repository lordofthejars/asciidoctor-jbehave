package org.jbehave.asciidoctor.reporter.calculator.stories;

import static org.jbehave.asciidoctor.reporter.AsciidoctorStoryReporter.ASCIIDOC;
import static org.jbehave.core.reporters.Format.HTML;

import org.jbehave.asciidoctor.reporter.calculator.steps.AddTwoNumbersSteps;
import org.jbehave.asciidoctor.reporter.calculator.steps.MultiplyTwoNumbersSteps;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

public class CalculatorMultStories extends JUnitStory {

	@Override
	public Configuration configuration() {
		return super.configuration().useStoryReporterBuilder(
				new StoryReporterBuilder().withDefaultFormats().withFormats(
						ASCIIDOC, HTML));
	}

	// Here we specify the steps classes
	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(),
				new MultiplyTwoNumbersSteps(), new AddTwoNumbersSteps());
	}
	
}
