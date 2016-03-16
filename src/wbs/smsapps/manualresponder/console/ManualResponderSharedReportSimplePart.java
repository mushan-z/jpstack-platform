package wbs.smsapps.manualresponder.console;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.UrlParams;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderSharedReportSimplePart")
public
class ManualResponderSharedReportSimplePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject @Named
	ConsoleModule manualResponderSharedReportConsoleModule;

	@Inject
	PrivChecker privChecker;

	@Inject
	TimeFormatter timeFormatter;

	// state

	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;

	SearchForm searchForm;

	List<ManualResponderReportRec> reports;
	String outputTypeParam;

	// implementation

	@Override
	public
	void prepare () {

		searchFormFieldSet =
			manualResponderSharedReportConsoleModule.formFieldSets ().get (
				"simpleReportSearch");

		resultsFormFieldSet =
			manualResponderSharedReportConsoleModule.formFieldSets ().get (
				"simpleReportResults");

		// get search form

		LocalDate today =
			LocalDate.now ();

		Interval todayInterval =
			today.toInterval ();

		searchForm =
			new SearchForm ()

			.start (
				todayInterval.getStart ().toInstant ())

			.end (
				todayInterval.getEnd ().toInstant ());

		formFieldLogic.update (
			requestContext,
			searchFormFieldSet,
			searchForm,
			ImmutableMap.of (),
			"report");

		// perform search

		reports =
			manualResponderReportHelper.findByProcessedTime (
				new Interval (
					searchForm.start (),
					searchForm.end ()))

			.stream ()

			.filter (report ->
				privChecker.canRecursive (
					report.getManualResponder (),
					"supervisor")
				|| privChecker.canRecursive (
					report.getProcessedByUser (),
					"manage"))

			.collect (
				Collectors.toList ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// search form

		printFormat (
			"<form method=\"get\">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			searchFormFieldSet,
			Optional.absent (),
			searchForm,
			ImmutableMap.of (),
			FormType.search,
			"report");

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",
			"<td><input",
			" type=\"submit\"",
			" value=\"search\"",
			"></td>\n",
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		UrlParams urlParams =
			new UrlParams ()

			.set (
				"start",
				timeFormatter.instantToTimestampString  (
					timeFormatter.defaultTimezone (),
					searchForm.start ()))

			.set (
				"end",
				timeFormatter.instantToTimestampString  (
					timeFormatter.defaultTimezone (),
					searchForm.end ()));


		printFormat (
			"<p><a",
			" href=\"%h\"",
			urlParams.toUrl (
				requestContext.resolveLocalUrl (
					"/manualResponderSharedReport.simpleCsv")),
			">Download CSV File</a></p>\n");

		printFormat (
			"<table class=\"list\">\n");

		formFieldLogic.outputTableHeadings (
			formatWriter,
			resultsFormFieldSet);

		for (
			ManualResponderReportRec report
				: reports
		) {

			printFormat (
				"<tr>\n");

			formFieldLogic.outputTableCellsList (
				formatWriter,
				resultsFormFieldSet,
				report,
				ImmutableMap.of (),
				true);

			printFormat (
				"</tr>\n");

		}


	 	printFormat (
			"</table>\n");

		printFormat (
			"<p><a",
			" href=\"%h\"",
			urlParams.toUrl (
				requestContext.resolveLocalUrl (
					"/manualResponderSharedReport.simpleCsv")),
			">Download CSV File</a></p>\n");

	}

	// search form

	@Accessors (fluent = true)
	@Data
	public static
	class SearchForm {

		Instant start;
		Instant end;

	}

}
