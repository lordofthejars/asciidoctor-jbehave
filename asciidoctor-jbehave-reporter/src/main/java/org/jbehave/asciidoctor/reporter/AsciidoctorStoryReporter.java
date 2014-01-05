package org.jbehave.asciidoctor.reporter;

import java.io.PrintStream;
import java.util.Iterator;
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
				return new AsciidoctorStoryReporter(factory.createPrintStream(),
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
			this.currentStoryContent.append("'''").append(NEW_LINE).append(NEW_LINE);
		}

	}

	private String renderStoryResult() {
		return String.format(this.currentStoryContent.toString(), getStoryResult());
	}

	@Override
	public void beforeExamples(List<String> steps, ExamplesTable table) {
		
		StringBuilder output = new StringBuilder();
		
		output.append(".Examples").append(NEW_LINE).append("****").append(NEW_LINE);
		output.append(renderSteps(steps));
		output.append(renderExamplesTable(table));
		output.append("****").append(NEW_LINE).append(NEW_LINE);
		
		this.currentStoryContent.append(output.toString());
		
	}

	private String renderExamplesTable(ExamplesTable examplesTable) {

        List<String> headers = examplesTable.getHeaders();
        String tableInformation = "[options=\"header\"]" + NEW_LINE;
        tableInformation += ".Examples" + NEW_LINE;
        tableInformation += "|===" + NEW_LINE;

        for (String header : headers) {
            tableInformation += "|" + header;
        }
        tableInformation += NEW_LINE;

        tableInformation += renderContentTable(examplesTable, headers);

        tableInformation += "|===" + NEW_LINE + NEW_LINE;

        return tableInformation;
    }

    private String renderContentTable(ExamplesTable examplesTable,
            List<String> headers) {

        String tableInformation = "";

        for (int numberOfRow = 0; numberOfRow < examplesTable.getRowCount(); numberOfRow++) {

            Map<String, String> row = examplesTable.getRow(numberOfRow);

            for (String header : headers) {
                tableInformation += "|" + row.get(header) + NEW_LINE;
            }

            tableInformation += NEW_LINE;

        }
        return tableInformation;
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
			outputContent.append("%s").append(NEW_LINE)
					.append(NEW_LINE);
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

		StringBuilder failedChunk = new StringBuilder(step + " ");
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
        output.append(step).append(" ").append(getIcon(FAIL_STEP, "red")).append(NEW_LINE);
		
        List<String> outcomeFields = table.getOutcomeFields();

        output.append("[options=\"header\"]").append(NEW_LINE);
        output.append("|===").append(NEW_LINE);
        
        for (String outcomeField : outcomeFields) {
			output.append("|").append(outcomeField).append(NEW_LINE);
		}
        
        for (Outcome<?> outcome : table.getOutcomes()) {
        	
        	output.append("|").append(outcome.getDescription()).append(NEW_LINE);
        	output.append("|").append(outcome.getValue()).append(NEW_LINE);
        	output.append("|").append(outcome.getMatcher()).append(NEW_LINE);
        	
        	String icon = outcome.isVerified() ? getIcon(SUCCESS_STEP, "green") : getIcon(FAIL_STEP, "red");
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
		
		this.currentStoryContent.append(step + " "
				+ getIcon(IGNORABLE_STEP, "black") + NEW_LINE + NEW_LINE);
	}

	@Override
	public void lifecyle(Lifecycle lifecycle) {
	}

	@Override
	public void narrative(Narrative narrative) {

		if (isNarrativeProvided(narrative)) {
			this.currentStoryContent.append(renderNarrative(narrative));
		}

	}

	@Override
	public void notPerformed(String step) {
		
		this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;

		this.currentStoryContent.append(step + " "
				+ getIcon(NOT_PERFORMED_STEP, "black") + NEW_LINE + NEW_LINE);
	}

	@Override
	public void pending(String step) {
		
		this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;

		this.currentStoryContent.append(step + " "
				+ getIcon(PENDING_STEP, "black") + NEW_LINE + NEW_LINE);
	}

	@Override
	public void pendingMethods(List<String> methods) {
	}

	@Override
	public void restarted(String step, Throwable cause) {
		
		this.currentStoryResult = StoryResult.FAIL;

		String restartedChunk = step + " " + getIcon(RESTARTED_STEP, "yellow");
		restartedChunk += NEW_LINE + NEW_LINE;
		restartedChunk += "[WARNING]" + NEW_LINE + "===="
				+ cause.getCause().getMessage() + NEW_LINE + "====";
		restartedChunk += NEW_LINE + NEW_LINE;
		this.currentStoryContent.append(restartedChunk);
	}

	@Override
	public void scenarioMeta(Meta meta) {

		if (isMetaProvided(meta)) {
			renderMetaInformation(meta);
		}

	}

	@Override
	public void scenarioNotAllowed(Scenario scenario, String filter) {
		
		this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		
		StringBuilder output = new StringBuilder();
		
		output.append("[WARNING]").append(NEW_LINE);
		output.append(".Scenario Not Allowed [").append(filter).append("]").append(NEW_LINE);
		
		output.append("====").append(NEW_LINE);
		output.append(scenario.getTitle()).append(" ").append(getIcon(NOT_ALLOWED, "orange"));
		output.append("====").append(NEW_LINE);
		
		this.currentStoryContent.append(output.toString());
		
	}

	@Override
	public void storyCancelled(Story story, StoryDuration storyDuration) {
		
		this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		
		StringBuilder output = new StringBuilder();
		
		output.append("[WARNING]").append(NEW_LINE);
		output.append(".Scenario Cancelled with Timeout [").append(storyDuration.getTimeoutInSecs()).append(" sec.]").append(NEW_LINE);
		
		output.append("====").append(NEW_LINE);
		output.append(story.getName()).append(" ").append(getIcon(CANCELLED, "orange"));
		output.append("====").append(NEW_LINE);
		
		this.currentStoryContent.append(output.toString());
	}

	@Override
	public void storyNotAllowed(Story story, String filter) {

		this.currentStoryResult = StoryResult.SUCCESS_WITH_WARNING;
		
		StringBuilder output = new StringBuilder();
		
		output.append("[WARNING]").append(NEW_LINE);
		output.append(".Story Not Allowed [").append(filter).append("]").append(NEW_LINE);
		
		output.append("====").append(NEW_LINE);
		output.append(story.getName()).append(" ").append(getIcon(NOT_ALLOWED, "orange"));
		output.append("====").append(NEW_LINE);
		
		this.currentStoryContent.append(output.toString());
	
	}

	@Override
	public void successful(String step) {
		this.currentStoryContent.append(step + " "
				+ getIcon(SUCCESS_STEP, "green") + NEW_LINE + NEW_LINE);
	}

	private String getIcon(String iconName, String size, String flip, String role) {
		return "icon:" + iconName + "[size="+size+", flip=\"" + flip + "\" role=\"" + role + "\"]";
	}
	
	private String getIcon(String iconName, String size, String role) {
		return "icon:" + iconName + "[size="+size+", role=\"" + role + "\"]";
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

		String descriptionInformation = ".Description" + NEW_LINE + "----"
				+ NEW_LINE;

		descriptionInformation += description.asString() + NEW_LINE;

		descriptionInformation += "----" + NEW_LINE + NEW_LINE;

		return descriptionInformation;

	}

	private String renderMetaInformation(Meta meta) {
		
		String metaInformation = ".Meta" + NEW_LINE + "----" + NEW_LINE;

		Set<String> propertyNames = meta.getPropertyNames();

		for (String propertyName : propertyNames) {
			String propertyValue = meta.getProperty(propertyName);

			metaInformation += "+" + propertyName + "+: " + propertyValue
					+ NEW_LINE;
		}

		metaInformation += "----" + NEW_LINE + NEW_LINE;

		return metaInformation;

	}

	private boolean isMetaProvided(Meta meta) {
		return meta != null && !Meta.EMPTY.equals(meta);
	}

	private boolean isNarrativeProvided(Narrative narrative) {
		return narrative != null && !Narrative.EMPTY.equals(narrative);
	}

	private String renderNarrative(Narrative narrative) {

		String narrativeInformation = ".Narrative" + NEW_LINE + "****"
				+ NEW_LINE;

		String inOrderTo = narrative.inOrderTo();

		if (inOrderTo != null) {
			narrativeInformation += "*In Order To* " + inOrderTo + NEW_LINE
					+ NEW_LINE;
		}

		String asA = narrative.asA();

		if (asA != null) {
			narrativeInformation += "*As a* " + asA + NEW_LINE + NEW_LINE;
		}

		String iWantTo = narrative.iWantTo();

		if (iWantTo != null) {
			narrativeInformation += "*I Want To* " + iWantTo + NEW_LINE
					+ NEW_LINE;
		}

		narrativeInformation += "****" + NEW_LINE + NEW_LINE;

		return narrativeInformation;

	}

	private boolean areGivenStoriesProvided(GivenStories givenStories) {
		return givenStories != null && givenStories.getStories().size() > 0;
	}

	private String renderGivenStories(GivenStories givenStories) {

		String givenStoriesInformation = ".Given Stories" + NEW_LINE + "[NOTE]"
				+ NEW_LINE + "====" + NEW_LINE;

		List<GivenStory> stories = givenStories.getStories();

		for (GivenStory givenStory : stories) {
			String path = givenStory.getPath();
			givenStoriesInformation += "<<" + getStoryIdentifier(path) + ", "
					+ path + ">>";

			if (givenStory.getAnchor() != null
					&& !"".equals(givenStory.getAnchor().trim())) {
				givenStoriesInformation += "#" + givenStory.getAnchor();
			}

			givenStoriesInformation += NEW_LINE;

		}

		givenStoriesInformation += "====" + NEW_LINE + NEW_LINE;

		return givenStoriesInformation;

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
			return "[.lead]"+NEW_LINE+getIcon(SUCCESS_STORY, "2x", "green") + " This story is ready to be shipped.";
		case SUCCESS_WITH_WARNING:
			return "[.lead]"+NEW_LINE+getIcon(SUCCESS_WITH_WARNING, "2x", "horizontal", "orange") + " This story can be shipped with caution.";
		case FAIL:
			return "[.lead]"+NEW_LINE+getIcon(FAILED_STORY, "2x", "horizontal", "red") + " This story contains errors and should not be shipped.";
		default:
			return "";
		}
	}

}
