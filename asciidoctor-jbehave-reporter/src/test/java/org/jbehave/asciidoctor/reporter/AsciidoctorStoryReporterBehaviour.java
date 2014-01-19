package org.jbehave.asciidoctor.reporter;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jbehave.core.reporters.PrintStreamFactory;
import org.junit.Test;

public class AsciidoctorStoryReporterBehaviour {

	private static final String EXPECTED_WITH_FAILURES = "[[_path_to_story]]\n" + 
			"== /path/to/story\n" + 
			"\n" + 
			"[.lead]\n" + 
			"icon:ambulance[size=2x, flip=\"horizontal\" role=\"red\"] This story contains errors and should not be shipped.\n" + 
			"\n" + 
			".Description\n" + 
			"----\n" + 
			"An interesting story\n" + 
			"----\n" + 
			"\n" + 
			".Meta\n" + 
			"----\n" + 
			"+author+: Mauro\n" + 
			"+theme+: testing\n" + 
			"----\n" + 
			"\n" + 
			".Narrative\n" + 
			"****\n" + 
			"*In Order To* renovate my house\n" + 
			"\n" + 
			"*As a* customer\n" + 
			"\n" + 
			"*I Want To* get a loan\n" + 
			"\n" + 
			"****\n" + 
			"\n" + 
			"=== Scenario: I ask for a loan\n" + 
			"\n" + 
			"*Given* I have a balance of $50 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*!--* A comment icon:volume-off[role=\"black\"]\n" + 
			"\n" + 
			"*When* I request $20 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I ask Liz for a loan of $100 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I ask Liz for a loan of $(99) icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write special chars <>&\" icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write special chars in parameter (<>&\") icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write two parameters (,,,) and (&&&) icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"[WARNING]\n" + 
			".Story Cancelled with Timeout [1 sec.]\n" + 
			"====\n" + 
			"/path/to/story icon:remove-circle[role=\"orange\"]\n" + 
			"====\n" + 
			"\n" + 
			"*Then* I should have a balance of $30 icon:spinner[role=\"black\"]\n" + 
			"\n" + 
			"*Then* I should have $20 icon:unlink[role=\"black\"]\n" + 
			"\n" + 
			"*Then* I don't return loan icon:thumbs-down[role=\"red\"]\n" + 
			"[options=\"header\"]\n" + 
			"|===\n" + 
			"|Description|Value|Matcher|Verified\n" + 
			"|I don't return all\n" + 
			"|100.0\n" + 
			"|<50.0>\n" + 
			"|icon:thumbs-down[role=\"red\"]\n" + 
			"\n" + 
			"|A wrong date\n" + 
			"|Sat Jan 01 00:00:00 CET 2011\n" + 
			"|\"02/01/2011\"\n" + 
			"|icon:thumbs-down[role=\"red\"]\n" + 
			"\n" + 
			"|===\n" + 
			"\n" + 
			"=== Scenario: Parametrised Scenario\n" + 
			"\n" + 
			".Examples\n" + 
			"****\n" + 
			".Steps\n" + 
			"----\n" + 
			"Given money <money>\n" + 
			"Then I give it to <to>\n" + 
			"----\n" + 
			"\n" + 
			"[options=\"header\"]\n" + 
			".Examples\n" + 
			"|===\n" + 
			"|money|to\n" + 
			"|$30\n" + 
			"|Mauro\n" + 
			"\n" + 
			"|$50\n" + 
			"|Paul\n" + 
			"\n" + 
			"|===\n" + 
			"\n" + 
			"****\n" + 
			"\n" + 
			"*Given* money $30 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I give it to Mauro icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Given* money $50 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I give it to Paul icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I should have a balance of $30 icon:spinner[role=\"black\"]";
	
	private static final String EXPECTED_WITH_WARNING = "[[_path_to_story]]\n" + 
			"== /path/to/story\n" + 
			"\n" + 
			"[.lead]\n" + 
			"icon:truck[size=2x, flip=\"horizontal\" role=\"yellow\"] This story can be shipped with caution.\n" + 
			"\n" + 
			".Description\n" + 
			"----\n" + 
			"An interesting story\n" + 
			"----\n" + 
			"\n" + 
			".Meta\n" + 
			"----\n" + 
			"+author+: Mauro\n" + 
			"+theme+: testing\n" + 
			"----\n" + 
			"\n" + 
			".Narrative\n" + 
			"****\n" + 
			"*In Order To* renovate my house\n" + 
			"\n" + 
			"*As a* customer\n" + 
			"\n" + 
			"*I Want To* get a loan\n" + 
			"\n" + 
			"****\n" + 
			"\n" + 
			"=== Scenario: I ask for a loan\n" + 
			"\n" + 
			"*Given* I have a balance of $50 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*!--* A comment icon:volume-off[role=\"black\"]\n" + 
			"\n" + 
			"*When* I request $20 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I ask Liz for a loan of $100 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I ask Liz for a loan of $(99) icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write special chars <>&\" icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write special chars in parameter (<>&\") icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write two parameters (,,,) and (&&&) icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"[WARNING]\n" + 
			".Story Cancelled with Timeout [1 sec.]\n" + 
			"====\n" + 
			"/path/to/story icon:remove-circle[role=\"yellow\"]\n" + 
			"====\n" + 
			"\n" + 
			"*Then* I should have a balance of $30 icon:spinner[role=\"black\"]\n" + 
			"\n" + 
			"*Then* I should have $20 icon:unlink[role=\"black\"]\n" + 
			"\n" + 
			"=== Scenario: Parametrised Scenario\n" + 
			"\n" + 
			".Examples\n" + 
			"****\n" + 
			".Steps\n" + 
			"----\n" + 
			"Given money <money>\n" + 
			"Then I give it to <to>\n" + 
			"----\n" + 
			"\n" + 
			"[options=\"header\"]\n" + 
			".Examples\n" + 
			"|===\n" + 
			"|money|to\n" + 
			"|$30\n" + 
			"|Mauro\n" + 
			"\n" + 
			"|$50\n" + 
			"|Paul\n" + 
			"\n" + 
			"|===\n" + 
			"\n" + 
			"****\n" + 
			"\n" + 
			"*Given* money $30 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I give it to Mauro icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Given* money $50 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I give it to Paul icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I should have a balance of $30 icon:spinner[role=\"black\"]";
	
	private static final String EXPECTED_WITH_SUCCESS = "[[_path_to_story]]\n" + 
			"== /path/to/story\n" + 
			"\n" + 
			"[.lead]\n" + 
			"icon:rocket[size=2x, role=\"green\"] This story is ready to be shipped.\n" + 
			"\n" + 
			".Description\n" + 
			"----\n" + 
			"An interesting story\n" + 
			"----\n" + 
			"\n" + 
			".Meta\n" + 
			"----\n" + 
			"+author+: Mauro\n" + 
			"+theme+: testing\n" + 
			"----\n" + 
			"\n" + 
			".Narrative\n" + 
			"****\n" + 
			"*In Order To* renovate my house\n" + 
			"\n" + 
			"*As a* customer\n" + 
			"\n" + 
			"*I Want To* get a loan\n" + 
			"\n" + 
			"****\n" + 
			"\n" + 
			"=== Scenario: I ask for a loan\n" + 
			"\n" + 
			"*Given* I have a balance of $50 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I request $20 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I ask Liz for a loan of $100 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I ask Liz for a loan of $(99) icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write special chars <>&\" icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write special chars in parameter (<>&\") icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*When* I write two parameters (,,,) and (&&&) icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"=== Scenario: Parametrised Scenario\n" + 
			"\n" + 
			".Examples\n" + 
			"****\n" + 
			".Steps\n" + 
			"----\n" + 
			"Given money <money>\n" + 
			"Then I give it to <to>\n" + 
			"----\n" + 
			"\n" + 
			"[options=\"header\"]\n" + 
			".Examples\n" + 
			"|===\n" + 
			"|money|to\n" + 
			"|$30\n" + 
			"|Mauro\n" + 
			"\n" + 
			"|$50\n" + 
			"|Paul\n" + 
			"\n" + 
			"|===\n" + 
			"\n" + 
			"****\n" + 
			"\n" + 
			"*Given* money $30 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I give it to Mauro icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Given* money $50 icon:thumbs-up[role=\"green\"]\n" + 
			"\n" + 
			"*Then* I give it to Paul icon:thumbs-up[role=\"green\"]";
	
	@Test
	public void shouldReportEventsToAsciiDocOutputWithFailures() {
		
		final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
		
		AsciidoctorStoryReporter asciidoctorStoryReporter = new AsciidoctorStoryReporter(factory.createPrintStream(), null, 0);
		
		StoryNarrator.narrateAnInterestingStoryWithFailures(asciidoctorStoryReporter, false);
		
		assertThat(out.toString().trim(), is(EXPECTED_WITH_FAILURES));
		
	}
	
	@Test
	public void shouldReportEventsToAsciiDocOutputWithWarnings() {
		
		final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
		
		AsciidoctorStoryReporter asciidoctorStoryReporter = new AsciidoctorStoryReporter(factory.createPrintStream(), null, 0);
		
		StoryNarrator.narrateAnInterestingStoryWithWarnings(asciidoctorStoryReporter, false);
		
		assertThat(out.toString().trim(), is(EXPECTED_WITH_WARNING));
		
	}
	
	@Test
	public void shouldReportEventsToAsciiDocOutputWithSuccess() {
		
		final OutputStream out = new ByteArrayOutputStream();
        PrintStreamFactory factory = new PrintStreamFactory() {

            public PrintStream createPrintStream() {
                return new PrintStream(out);
            }
        };
		
		AsciidoctorStoryReporter asciidoctorStoryReporter = new AsciidoctorStoryReporter(factory.createPrintStream(), null, 0);
		
		StoryNarrator.narrateAnInterestingStoryWithSuccess(asciidoctorStoryReporter, false);
		
		assertThat(out.toString().trim(), is(EXPECTED_WITH_SUCCESS));
		
	}
	
}
