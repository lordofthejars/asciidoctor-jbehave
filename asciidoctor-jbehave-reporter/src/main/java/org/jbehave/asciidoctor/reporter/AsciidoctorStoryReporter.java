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
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;

public class AsciidoctorStoryReporter implements StoryReporter {

	private static final String SUCCESS_STORY = "rocket";
	private static final String SUCCESS_STEP = "thumbs-up";
	private static final String FAIL_STEP = "thumbs-down";
	private static final String PENDING_STEP = "spinner";
	private static final String NOT_PERFORMED_STEP = "unlink";
	private static final String RESTARTED_STEP = "rotate-right";
	private static final String IGNORABLE_STEP = "volume-off";
	//both below icons should be adapted for font awesome 4 when Asciidoctor support it.
	private static final String NOT_ALLOWED = "minus-sign";
	private static final String CANCELLED = "remove-circle";
	
	private static final int SECTION_KEY = 0;
	private static final String NEW_LINE = System.getProperty("line.separator");

	private PrintStream output;
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

	public AsciidoctorStoryReporter(PrintStream output, Keywords keywords,
			int initialSectionLevel) {
		this.output = output;
		this.keywords = keywords;
		this.initialLevel = initialSectionLevel;
	}

	@Override
	public void afterExamples() {
		System.out.println("After Examples");
	}

	@Override
	public void afterScenario() {
		this.initialLevel--;
		System.out.println("After Scenario");
	}

	@Override
	public void afterStory(boolean givenStory) {
		this.initialLevel--;
	}

	@Override
	public void beforeExamples(List<String> steps, ExamplesTable table) {
		System.out.println("Before Examples " + steps + " Examples Table "
				+ table);
	}

	@Override
	public void beforeScenario(String scenarioTitle) {
		this.initialLevel++;
		this.output.print(renderTitle(scenarioTitle, this.initialLevel));
	}

	@Override
	public void beforeStep(String step) {
		System.out.println("Before Step " + step);
	}

	@Override
	public void beforeStory(Story story, boolean givenStory) {

		this.initialLevel++;
		
		StringBuilder outputContent = new StringBuilder();

		outputContent.append("[[").append(story.getName()).append("]]")
				.append(NEW_LINE).append(getInitialSection(this.initialLevel))
				.append(" ").append(story.getName()).append(NEW_LINE)
				.append(NEW_LINE);

		Description description = story.getDescription();
		if (isDescriptionProvided(description)) {
			outputContent.append(renderDescription(description));
		}

		Meta meta = story.getMeta();
		if (isMetaProvided(meta)) {
			outputContent.append(renderMetaInformation(meta));
		}

		this.output.print(outputContent);

	}

	@Override
	public void dryRun() {
	}

	@Override
	public void example(Map<String, String> tableRow) {
		System.out.println("Example " + tableRow);
	}

	@Override
	public void failed(String step, Throwable cause) {
		String failedChunk = step + " " + getIcon(FAIL_STEP, "red");
		failedChunk += NEW_LINE + NEW_LINE;
		failedChunk += "[IMPORTANT]" + NEW_LINE + "====" + cause.getCause().getMessage() + NEW_LINE + "===="; 
		failedChunk += NEW_LINE + NEW_LINE;
		this.output.print(failedChunk);
		
	}

	@Override
	public void failedOutcomes(String step, OutcomesTable table) {
		System.out.println("Failed Outcomes " + step + " Outcomes Table "
				+ table);
	}

	@Override
	public void givenStories(GivenStories givenStories) {

		if (areGivenStoriesProvided(givenStories)) {
			this.output.append(renderGivenStories(givenStories));
		}

	}

	@Override
	public void givenStories(List<String> storyPaths) {
	}

	@Override
	public void ignorable(String step) {
		this.output.print(step + " " + getIcon(IGNORABLE_STEP, "black") + NEW_LINE + NEW_LINE);
	}

	@Override
	public void lifecyle(Lifecycle lifecycle) {
		System.out.println("Lifecycle " + lifecycle);
	}

	@Override
	public void narrative(Narrative narrative) {

		if (isNarrativeProvided(narrative)) {
			this.output.print(renderNarrative(narrative));
		}

	}

	@Override
	public void notPerformed(String step) {
		this.output.print(step + " " + getIcon(NOT_PERFORMED_STEP, "black") + NEW_LINE + NEW_LINE);
	}

	@Override
	public void pending(String step) {
		this.output.print(step + " " + getIcon(PENDING_STEP, "black") + NEW_LINE + NEW_LINE);
	}

	@Override
	public void pendingMethods(List<String> methods) {
		System.out.println("Pending Methods " + methods);
	}

	@Override
	public void restarted(String step, Throwable cause) {
		String restartedChunk = step + " " + getIcon(RESTARTED_STEP, "yellow");
		restartedChunk += NEW_LINE + NEW_LINE;
		restartedChunk += "[WARNING]" + NEW_LINE + "====" + cause.getCause().getMessage() + NEW_LINE + "===="; 
		restartedChunk += NEW_LINE + NEW_LINE;
		this.output.print(restartedChunk);
	}

	@Override
	public void scenarioMeta(Meta meta) {

		if (isMetaProvided(meta)) {
			renderMetaInformation(meta);
		}

	}

	@Override
	public void scenarioNotAllowed(Scenario scenario, String filter) {
		System.out.println("Scenario Not Allowed " + scenario + "Filter "
				+ filter);
	}

	@Override
	public void storyCancelled(Story story, StoryDuration storyDuration) {
		System.out.println("Story Cancelled " + story + " durantion "
				+ storyDuration);
	}

	@Override
	public void storyNotAllowed(Story story, String filter) {
		System.out.println("Story Not Allowed " + story + " Filter " + filter);
	}

	@Override
	public void successful(String step) {
		this.output.print(step + " " + getIcon(SUCCESS_STEP, "green") + NEW_LINE + NEW_LINE);
	}

	private String getIcon(String iconName, String role) {
		return "icon:"+iconName+"[role=\""+role+"\"]";
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
		return getInitialSection(currentScenarioNumber) + " Scenario: "
				+ title + NEW_LINE + NEW_LINE;
	}

}
