package org.jbehave.asciidoctor.reporter;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.model.OutcomesTable.Outcome;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;

public class AsciidoctorStoryReporter implements StoryReporter {

	private enum StoryResult {
		SUCCESS, FAIL, SUCCESS_WITH_WARNING;
	}

	private static final String SUCCESS_STORY = "rocket";
	private static final String SUCCESS_WITH_WARNING = "truck";
	private static final String FAILED_STORY = "ambulance";
	private static final String SUCCESS_STEP = "thumbs-up";
	private static final String FAIL_STEP = "thumbs-down";
	private static final String PENDING_STEP = "spinner";
	private static final String NOT_PERFORMED_STEP = "unlink";
	private static final String RESTARTED_STEP = "rotate-right";
	private static final String IGNORABLE_STEP = "volume-off";
	// both below icons should be adapted for font awesome 4 when Asciidoctor
	// support it.
	private static final String NOT_ALLOWED = "minus-sign";
	private static final String CANCELLED = "remove-circle";

	private static final int SECTION_KEY = 0;
	private static final String NEW_LINE = System.getProperty("line.separator");

	private PrintStream printer;
	private StringBuilder currentStoryContent = new StringBuilder();
	private StoryResult currentStoryResult = StoryResult.SUCCESS;

	private Keywords keywords;

	private int initialLevel = SECTION_KEY;

	public static final Format ASCIIDOC = new Format("ADOC") {

		@Override
		public StoryReporter createStoryReporter(
				FilePrintStreamFactory factory,
				StoryReporterBuilder storyReporterBuilder) {
			factory.useConfiguration(storyReporterBuilder
					.fileConfiguration("adoc"));
			return new AsciidoctorStoryReporter(factory.createPrintStream(),
					storyReporterBuilder.keywords(), SECTION_KEY);
		}
	};

	public static final Format ASCIIDOC(final int initialSectionLevel) {
		return new Format("ADOC") {

			@Override
			public StoryReporter createStoryReporter(
					FilePrintStreamFactory factory,
					StoryReporterBuilder storyReporterBuilder) {
				factory.useConfiguration(storyReporterBuilder
						.fileConfiguration("adoc"));
				return new AsciidoctorStoryReporter(
						factory.createPrintStream(),
						storyReporterBuilder.keywords(), initialSectionLevel);
			}
		};
	}

	public AsciidoctorStoryReporter(PrintStream output, Keywords keywords,
			int initialSectionLevel) {

		this.printer = output;
		this.keywords = keywords;
		this.initialLevel = initialSectionLevel;
	}

	@Override
	public void afterExamples() {
	}

	@Override
	public void afterScenario() {
		this.initialLevel--;
	}

	@Override
	public void afterStory(boolean givenStory) {

		this.initialLevel--;

		if (!givenStory) {
			this.printer.print(renderStoryResult());
			this.currentStoryContent = new StringBuilder();
			this.currentStoryResult = StoryResult.SUCCESS;
		} else {
			this.currentStoryContent.append("'''").append(NEW_LINE)
					.append(NEW_LINE);
		}

	}

	private String renderStoryResult() {
		return String.format(this.currentStoryContent.toString(),
				getStoryResult());
	}

	@Override
	public void beforeExamples(List<String> steps, ExamplesTable table) {

		StringBuilder output = new StringBuilder();

		output.append(".Examples").append(NEW_LINE).append("****")
				.append(NEW_LINE);
		output.append(renderSteps(steps));
		output.append(renderExamplesTable(table));
		output.append("****").append(NEW_LINE).append(NEW_LINE);

		this.currentStoryContent.append(output.toString());

	}

	private String renderExamplesTable(ExamplesTable examplesTable) {

		List<String> headers = examplesTable.getHeaders();
		StringBuilder tableInformation = new StringBuilder(
				"[options=\"header\"]" + NEW_LINE);
		tableInformation.append(".Examples").append(NEW_LINE);
		tableInformation.append("|===").append(NEW_LINE);

		for (String header : headers) {
			tableInformation.append("|").append(header);
		}
		tableInformation.append(NEW_LINE);

		tableInformation.append(renderContentTable(examplesTable, headers));

		tableInformation.append("|===").append(NEW_LINE).append(NEW_LINE);

		return tableInformation.toString();
	}

	private String renderContentTable(ExamplesTable examplesTable,
			List<String> headers) {

		StringBuilder tableInformation = new StringBuilder();

		for (int numberOfRow = 0; numberOfRow < examplesTable.getRowCount(); numberOfRow++) {

			Map<String, String> row = examplesTable.getRow(numberOfRow);

			for (String header : headers) {
				tableInformation.append("|").append(row.get(header))
						.append(NEW_LINE);
			}

			tableInformation.append(NEW_LINE);

		}
		return tableInformation.toString();
	}

	private String renderSteps(List<String> steps) {

		StringBuilder stepsInformation = new StringBuilder(".Steps" + NEW_LINE);
		stepsInformation.append("----").append(NEW_LINE);
		for (String step : steps) {
			stepsInformation.append(step).append(NEW_LINE);
		}

		stepsInformation.append("----").append(NEW_LINE);
		stepsInformation.append(NEW_LINE);

		return stepsInformation.toString();
	}

	@Override
	public void beforeScenario(String scenarioTitle) {

		this.initialLevel++;
		this.currentStoryContent.append(renderTitle(scenarioTitle,
				this.initialLevel));
	}

	@Override
	public void beforeStep(String step) {
		
	}

	@Override
	public void beforeStory(Story story, boolean givenStory) {

		this.initialLevel++;

		StringBuilder outputContent = new StringBuilder();

		outputContent.append("[[").append(story.getName()).append("]]")
				.append(NEW_LINE).append(getInitialSection(this.initialLevel))
				.append(" ").append(story.getName())
				.append(getSuffix(givenStory)).append(NEW_LINE)
				.append(NEW_LINE);

		if (!givenStory) {
			outputContent.append("%s").append(NEW_LINE).append(NEW_LINE);
		}

		Description description = story.getDescription();
		if (isDescriptionProvided(description)) {
			outputContent.append(renderDescription(description));
		}

		Meta meta = story.getMeta();
		if (isMetaProvided(meta)) {
			outputContent.append(renderMetaInformation(meta));
		}

		this.currentStoryContent.append(outputContent);

	}

	private String getSuffix(boolean givenStory) {
		return givenStory ? " [GivenStory]" : "";
	}

	@Override
	public void dryRun() {
	}

	@Override
	public void example(Map<String, String> tableRow) {
	}

	@Override
	public void failed(String step, Throwable cause) {

		this.currentStoryResult = StoryResult.FAIL;

		StringBuilder failedChunk = new StringBuilder(formatStep(step) + " ");
		failedChunk.append(getIcon(FAIL_STEP, "red"));
		failedChunk.append(NEW_LINE).append(NEW_LINE);
		failedChunk.append("[IMPORTANT]");
		failedChunk.append(NEW_LINE);
		failedChunk.append("====");
		failedChunk.append(cause.getCause().getMessage() + NEW_LINE);
		failedChunk.append("====");
		failedChunk.append(NEW_LINE + NEW_LINE);

		this.currentStoryContent.append(failedChunk.toString());

	}

	@Override
	public void failedOutcomes(String step, OutcomesTable table) {

		this.currentStoryResult = StoryResult.FAIL;

		StringBuilder output = new StringBuilder();
		output.append(formatStep(step)).append(" ").append(getIcon(FAIL_STEP, "red"))
				.append(NEW_LINE);

		List<String> outcomeFields = table.getOutcomeFields();

		output.append("[options=\"header\"]").append(NEW_LINE);
		output.append("|===").append(NEW_LINE);

		for (String outcomeField : outcomeFields) {
			output.append("|").append(outcomeField).append(NEW_LINE);
		}

		for (Outcome<?> outcome : table.getOutcomes()) {

			output.append("|").append(outcome.getDescription())
					.append(NEW_LINE);
			output.append("|").append(outcome.getValue()).append(NEW_LINE);
			output.append("|").append(outcome.getMatcher()).append(NEW_LINE);

			String icon = outcome.isVerified() ? getIcon(SUCCESS_STEP, "green")
					: getIcon(FAIL_STEP, "red");
			output.append("|").append(icon).append(NEW_LINE).append(NEW_LINE);

		}

		output.append("|===").append(NEW_LINE).append(NEW_LINE);

		this.currentStoryContent.append(output.toString());

	}

	@Override
	public void givenStories(GivenStories givenStories) {

		if (areGivenStoriesProvided(givenStories)) {
			this.currentStoryContent.append(renderGivenStories(givenStories));
		}

	}

	@Override
	public void givenStories(List<String> storyPaths) {
	}

	@Override
	public void ignorable(String step) {
		this.currentStoryContent.append(formatStep(step)).append(" ")
				.append(getIcon(IGNORABLE_STEP, "black")).append(NEW_LINE)
				.append(NEW_LINE);
	}

	@Override
	public void lifecyle(Lifecycle lifecycle) {

		if (!lifecycle.isEmpty()) {

			StringBuilder lifecycleOutput = new StringBuilder();
			
			List<String> beforeSteps = lifecycle.getBeforeSteps();

			if(beforeSteps.size() > 0) {
				
				lifecycleOutput.append(".Before Steps"
						+ NEW_LINE + "----" + NEW_LINE);
				
				for (String step : beforeSteps) {
					lifecycleOutput.append(formatStep(step)).append(NEW_LINE);
				}

				lifecycleOutput.append("----").append(NEW_LINE).append(NEW_LINE);
				
			}
				
			List<String> afterSteps = lifecycle.getAfterSteps();
			
			if(afterSteps.size() > 0) {
			
				lifecycleOutput.append(".After Steps"
						+ NEW_LINE + "----" + NEW_LINE);
				
				for (String step : afterSteps) {
					lifecycleOutput.append(formatStep(step)).append(NEW_LINE);
				}

				lifecycleOutput.append("----").append(NEW_LINE).append(NEW_LINE);
				
			}
			
			this.currentStoryContent.append(lifecycleOutput.toString());
		}
	}

	@Override
	public void narrative(Narrative narrative) {

		if (isNarrativeProvided(narrative)) {
			this.currentStoryContent.append(renderNarrative(narrative));
		}

	}

	@Override
	public void notPerformed(String step) {

		if (this.currentStoryResult != StoryResult.FAIL) {
			this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		}

		this.currentStoryContent.append(formatStep(step)).append(" ")
				.append(getIcon(NOT_PERFORMED_STEP, "black")).append(NEW_LINE).append(NEW_LINE);
	}

	@Override
	public void pending(String step) {

		if (this.currentStoryResult != StoryResult.FAIL) {
			this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		}

		this.currentStoryContent.append(formatStep(step)).append(" ").append(
				getIcon(PENDING_STEP, "black")).append(NEW_LINE).append(NEW_LINE);
	}

	@Override
	public void pendingMethods(List<String> methods) {
	}

	@Override
	public void restarted(String step, Throwable cause) {

		this.currentStoryResult = StoryResult.FAIL;

		StringBuilder restartedChunk = new StringBuilder(formatStep(step) + " " + getIcon(RESTARTED_STEP, "yellow"));
		restartedChunk.append(NEW_LINE).append(NEW_LINE);
		restartedChunk.append("[WARNING]").append(NEW_LINE).append("====")
				.append(cause.getCause().getMessage()).append(NEW_LINE).append("====");
		restartedChunk.append(NEW_LINE).append(NEW_LINE);
		this.currentStoryContent.append(restartedChunk.toString());
	}

	@Override
	public void scenarioMeta(Meta meta) {

		if (isMetaProvided(meta)) {
			renderMetaInformation(meta);
		}

	}

	@Override
	public void scenarioNotAllowed(Scenario scenario, String filter) {

		if (this.currentStoryResult != StoryResult.FAIL) {
			this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		}

		StringBuilder output = new StringBuilder();

		output.append("[WARNING]").append(NEW_LINE);
		output.append(".Scenario Not Allowed [").append(filter).append("]")
				.append(NEW_LINE);

		output.append("====").append(NEW_LINE);
		output.append(scenario.getTitle()).append(" ")
				.append(getIcon(NOT_ALLOWED, "orange"));
		output.append("====").append(NEW_LINE);

		this.currentStoryContent.append(output.toString());

	}

	@Override
	public void storyCancelled(Story story, StoryDuration storyDuration) {

		if (this.currentStoryResult != StoryResult.FAIL) {
			this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		}

		StringBuilder output = new StringBuilder();

		output.append("[WARNING]").append(NEW_LINE);
		output.append(".Scenario Cancelled with Timeout [")
				.append(storyDuration.getTimeoutInSecs()).append(" sec.]")
				.append(NEW_LINE);

		output.append("====").append(NEW_LINE);
		output.append(story.getName()).append(" ")
				.append(getIcon(CANCELLED, "orange"));
		output.append("====").append(NEW_LINE);

		this.currentStoryContent.append(output.toString());
	}

	@Override
	public void storyNotAllowed(Story story, String filter) {

		if (this.currentStoryResult != StoryResult.FAIL) {
			this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		}

		StringBuilder output = new StringBuilder();

		output.append("[WARNING]").append(NEW_LINE);
		output.append(".Story Not Allowed [").append(filter).append("]")
				.append(NEW_LINE);

		output.append("====").append(NEW_LINE);
		output.append(story.getName()).append(" ")
				.append(getIcon(NOT_ALLOWED, "orange"));
		output.append("====").append(NEW_LINE);

		this.currentStoryContent.append(output.toString());

	}
	
	@Override
	public void successful(String step) {
		this.currentStoryContent.append(formatStep(step)).append( " "
				).append(getIcon(SUCCESS_STEP, "green")).append(NEW_LINE).append(NEW_LINE);
	}

	private String getIcon(String iconName, String size, String flip,
			String role) {
		return "icon:" + iconName + "[size=" + size + ", flip=\"" + flip
				+ "\" role=\"" + role + "\"]";
	}

	private String getIcon(String iconName, String size, String role) {
		return "icon:" + iconName + "[size=" + size + ", role=\"" + role
				+ "\"]";
	}

	private String getIcon(String iconName, String role) {
		return "icon:" + iconName + "[role=\"" + role + "\"]";
	}

	private String getInitialSection(int initialLevel) {

		StringBuilder zeroLevel = new StringBuilder("=");

		for (int i = 0; i < initialLevel; i++) {
			zeroLevel.append("=");
		}

		return zeroLevel.toString();

	}

	private boolean isDescriptionProvided(Description description) {
		return description != null && !"".equals(description.asString().trim());
	}

	private String renderDescription(Description description) {

		StringBuilder descriptionInformation = new StringBuilder(".Description"
				+ NEW_LINE + "----" + NEW_LINE);

		descriptionInformation.append(description.asString()).append(NEW_LINE);

		descriptionInformation.append("----").append(NEW_LINE).append(NEW_LINE);

		return descriptionInformation.toString();

	}

	private String renderMetaInformation(Meta meta) {

		StringBuilder metaInformation = new StringBuilder(".Meta" + NEW_LINE
				+ "----" + NEW_LINE);

		Set<String> propertyNames = meta.getPropertyNames();

		for (String propertyName : propertyNames) {
			String propertyValue = meta.getProperty(propertyName);

			metaInformation.append("+").append(propertyName).append("+: ")
					.append(propertyValue).append(NEW_LINE);
		}

		metaInformation.append("----").append(NEW_LINE).append(NEW_LINE);

		return metaInformation.toString();

	}

	private boolean isMetaProvided(Meta meta) {
		return meta != null && !Meta.EMPTY.equals(meta);
	}

	private boolean isNarrativeProvided(Narrative narrative) {
		return narrative != null && !Narrative.EMPTY.equals(narrative);
	}

	private String renderNarrative(Narrative narrative) {

		StringBuilder narrativeInformation = new StringBuilder(".Narrative"
				+ NEW_LINE + "****" + NEW_LINE);

		String inOrderTo = narrative.inOrderTo();

		if (inOrderTo != null) {
			narrativeInformation.append("*In Order To* ").append(inOrderTo)
					.append(NEW_LINE).append(NEW_LINE);
		}

		String asA = narrative.asA();

		if (asA != null) {
			narrativeInformation.append("*As a* ").append(asA).append(NEW_LINE)
					.append(NEW_LINE);
		}

		String iWantTo = narrative.iWantTo();

		if (iWantTo != null) {
			narrativeInformation.append("*I Want To* ").append(iWantTo)
					.append(NEW_LINE).append(NEW_LINE);
		}

		narrativeInformation.append("****").append(NEW_LINE).append(NEW_LINE);

		return narrativeInformation.toString();

	}

	private boolean areGivenStoriesProvided(GivenStories givenStories) {
		return givenStories != null && givenStories.getStories().size() > 0;
	}
	
	private String formatStep(String step) {
		step = replaceParenthesis(step);
		return boldFirstWord(step);
	}
	
	private String replaceParenthesis(String content) {
		return content.replace('｟', '(').replace('｠', ')');
	}
	
	private String boldFirstWord(String content) {
		int firstWhiteSpace = content.indexOf(" ");
		return "*" + content.substring(0, firstWhiteSpace) + "*" + content.substring(firstWhiteSpace);
	}
	
	private String renderGivenStories(GivenStories givenStories) {

		StringBuilder givenStoriesInformation = new StringBuilder(
				".Given Stories" + NEW_LINE + "[NOTE]" + NEW_LINE + "===="
						+ NEW_LINE);

		List<GivenStory> stories = givenStories.getStories();

		for (GivenStory givenStory : stories) {
			String path = givenStory.getPath();
			givenStoriesInformation.append("<<")
					.append(getStoryIdentifier(path)).append(", ").append(path)
					.append(">>");

			if (givenStory.getAnchor() != null
					&& !"".equals(givenStory.getAnchor().trim())) {
				givenStoriesInformation.append("#").append(
						givenStory.getAnchor());
			}

			givenStoriesInformation.append(NEW_LINE);

		}

		givenStoriesInformation.append("====").append(NEW_LINE)
				.append(NEW_LINE);

		return givenStoriesInformation.toString();

	}

	private String getStoryIdentifier(String path) {
		int lastSlashNx = path.lastIndexOf("/");

		if (lastSlashNx > -1) {
			return path.substring(lastSlashNx + 1);
		} else {
			int lastSlashWin = path.lastIndexOf("\\");
			if (lastSlashWin > -1) {
				return path.substring(lastSlashWin + 1);
			} else {
				return path;
			}
		}
	}

	private String renderTitle(String title, int currentScenarioNumber) {
		if (title != null) {
			return getTitle(title, currentScenarioNumber);
		} else {
			return getTitle(Integer.toString(currentScenarioNumber),
					currentScenarioNumber);
		}
	}

	private String getTitle(String title, int currentScenarioNumber) {
		return getInitialSection(currentScenarioNumber) + " Scenario: " + title
				+ NEW_LINE + NEW_LINE;
	}

	private String getStoryResult() {
		switch (this.currentStoryResult) {
		case SUCCESS:
			return "[.lead]" + NEW_LINE + getIcon(SUCCESS_STORY, "2x", "green")
					+ " This story is ready to be shipped.";
		case SUCCESS_WITH_WARNING:
			return "[.lead]"
					+ NEW_LINE
					+ getIcon(SUCCESS_WITH_WARNING, "2x", "horizontal",
							"orange")
					+ " This story can be shipped with caution.";
		case FAIL:
			return "[.lead]" + NEW_LINE
					+ getIcon(FAILED_STORY, "2x", "horizontal", "red")
					+ " This story contains errors and should not be shipped.";
		default:
			return "";
		}
	}

}
