package org.jbehave.asciidoctor.reporter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;

public class AsciidoctorStoryReporter implements StoryReporter {

	private PrintStream output;
	private Keywords keywords;
	
	public static final Format ASCIIDOC = new Format("ADOC") {
		
		@Override
		public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
				StoryReporterBuilder storyReporterBuilder) {
			factory.useConfiguration(storyReporterBuilder.fileConfiguration("adoc"));
			return new AsciidoctorStoryReporter(factory.createPrintStream(), storyReporterBuilder.keywords());
		}
	};
	
	public AsciidoctorStoryReporter(PrintStream output, Keywords keywords) {
		this.output = output;
		this.keywords = keywords;
	}
	
	@Override
	public void afterExamples() {
		System.out.println("After Examples");
	}

	@Override
	public void afterScenario() {
		System.out.println("After Scenario");
	}

	@Override
	public void afterStory(boolean givenStory) {
		System.out.println("After Story "+givenStory);
	}

	@Override
	public void beforeExamples(List<String> steps, ExamplesTable table) {
		System.out.println("Before Examples "+ steps + " Examples Table " + table);
	}

	@Override
	public void beforeScenario(String scenarioTitle) {
		System.out.println("Before Scenario " + scenarioTitle);
	}

	@Override
	public void beforeStep(String step) {
		System.out.println("Before Step " + step);
	}

	@Override
	public void beforeStory(Story story, boolean givenStory) {
		System.out.println("Before Story " + story + " isGivenStory " + givenStory);
	}

	@Override
	public void dryRun() {
		System.out.println("DryRun");
	}

	@Override
	public void example(Map<String, String> tableRow) {
		System.out.println("Example " + tableRow);
	}

	@Override
	public void failed(String step, Throwable cause) {
		System.out.println("Failed "+ step + " " + cause.getCause());
	}

	@Override
	public void failedOutcomes(String step, OutcomesTable table) {
		System.out.println("Failed Outcomes " + step + " Outcomes Table "+ table);
	}

	@Override
	public void givenStories(GivenStories givenStories) {
		System.out.println("Given Stories " + givenStories);
	}

	@Override
	public void givenStories(List<String> storyPaths) {
		System.out.println("Given Stories SP" + storyPaths);
	}

	@Override
	public void ignorable(String step) {
		System.out.println("Ignorable " + step);
	}

	@Override
	public void lifecyle(Lifecycle lifecycle) {
		System.out.println("Lifecycle " + lifecycle);
	}

	@Override
	public void narrative(Narrative narrative) {
		System.out.println("Narrative " + narrative);
	}

	@Override
	public void notPerformed(String step) {
		System.out.println("Not Performed " + step);
	}

	@Override
	public void pending(String step) {
		System.out.println("Pending " + step);
	}

	@Override
	public void pendingMethods(List<String> methods) {
		System.out.println("Pending Methods " + methods);
	}

	@Override
	public void restarted(String step, Throwable cause) {
		System.out.println("Restarted "+ step + " cause " + cause);
	}

	@Override
	public void scenarioMeta(Meta meta) {
		System.out.println("Meta " + meta);
	}

	@Override
	public void scenarioNotAllowed(Scenario scenario, String filter) {
		System.out.println("Scenario Not Allowed " + scenario + "Filter " + filter);
	}

	@Override
	public void storyCancelled(Story story, StoryDuration storyDuration) {
		System.out.println("Story Cancelled " + story + " durantion " + storyDuration);
	}

	@Override
	public void storyNotAllowed(Story story, String filter) {
		System.out.println("Story Not Allowed " + story + " Filter " + filter);
	}

	@Override
	public void successful(String step) {
		System.out.println("Successful " + step);
	}

}
