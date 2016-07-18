package wbs.services.ticket.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import wbs.console.context.ConsoleContextScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateRec;
import wbs.services.ticket.core.model.TicketTemplateRec;

@PrototypeComponent ("ticketPendingFormResponder")
public
class TicketPendingFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	TicketObjectHelper ticketHelper;

	@Inject
	UserPrivChecker privChecker;

	// state

	TicketRec ticket;
	TicketStateRec ticketState;

	List<TicketTemplateRec> templates;

	String summaryUrl;

	boolean manager;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			ConsoleContextScriptRef.javascript (
				"/js/jquery-1.11.2.js"),

			ConsoleContextScriptRef.javascript (
				"/js/TicketPending.js"));

	}

	// implementation

	@Override
	protected
	void prepare () {

		super.prepare ();

		ticket =
			ticketHelper.findRequired (
				requestContext.stuffInt (
					"ticketId"));

		ticketState =
			ticket.getTicketState ();

		summaryUrl =
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/ticket.pending",
					"/%u",
					ticket.getId (),
					"/ticket.pending.history"));

		ImmutableList.Builder<TicketTemplateRec> templatesBuilder =
			ImmutableList.<TicketTemplateRec>builder ();

		for (
			TicketTemplateRec template
				: ticket.getTicketManager ().getTicketTemplates ()
		) {

			if (template.getDeleted ())
				continue;

			templatesBuilder.add (
				template);

		}

		templates =
			templatesBuilder.build ();

	}

	@Override
	public
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<script language=\"JavaScript\">\n");

		printFormat (
			"top.show_inbox (true);\n",
			"top.frames ['main'].location = 'about:blank';\n",
			"window.setTimeout (function () { top.frames ['main'].location = '%j' }, 1);\n",
			summaryUrl);

		printFormat (
			"</script>\n");

		printFormat (
			"<style type=\"text/css\">\n",
			"  .template-chars.error {\n",
			"    background-color: darkred;\n",
			"    color: white;\n",
			"    padding-left: 10px;\n",
			"    padding-right: 10px;\n",
			"  }\n",
			"</style>\n");

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices (
			printWriter);

		printFormat (
			"<p",
			" class=\"links\"",
			">\n",

			"<a",
			" href=\"%h\">Queues</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),

			"<a",
			" href=\"%h\"",
			summaryUrl,
			" target=\"main\"",
			">Summary</a>\n",

			"<a",
			" href=\"javascript:top.show_inbox (false);\"",
			">Close</a>\n",

			"</p>\n");

		printFormat (
			"<h2>Ticket management</h2>\n");

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/ticket.pending",
					"/%u",
					ticket.getId (),
					"/ticket.pending.form")),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table",
			" id=\"templates\"",
			" class=\"list\"",
			" style=\"width: 100%%\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</td>\n",
			"<th>Name</th>\n",
			"<th>New State</th>\n",
			"<th>Time to queue</th>\n",
			"</tr>\n");

		List<TicketTemplateRec> templatesReversed
			= Lists.reverse(templates);

		for (
			TicketTemplateRec template
				: templatesReversed
		) {

			doTemplate (
				template);
		}

		printFormat (
			"</table>\n");

		addTicketNote();

		printFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			">\n");

		printFormat (
			"</form>\n");

	}

	void addTicketNote () {

		printFormat (
			"<p><h3>Add new note</h3>\n");

		printFormat (
			"<label for=\"note-text\">",
			"Note text",
			"</label>");

		printFormat (
			"<input",
			" id=\"note-text\"",
			" type=\"textarea\"",
			" name=\"note-text\"",
			">\n");

		printFormat (
			"</p>\n");

	}

	void doTemplate (
		TicketTemplateRec template) {

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"%h\"",
			template.getId (),
			">\n");

		/*
		if (template.getTicketState()
				.getState().equals (
					ticket.getTicketState().getState())) {

			printFormat (

				"<td><input",

				" id=\"radio-template-%h\"",
				template.getId (),

				" class=\"template-radio\"",

				" type=\"radio\"",

				" name=\"template\"",

				" value=\"%h\"",
				template.getId (),

				"checked",

				"></td>\n");

		}
		else {

			printFormat (

				"<td><input",

				" id=\"radio-template-%h\"",
				template.getId (),

				" class=\"template-radio\"",

				" type=\"radio\"",

				" name=\"template\"",

				" value=\"%h\"",
				template.getId (),

				"></td>\n");

		}
		*/

		printFormat (
			"<td>%s</td>\n",
			Html.nonBreakingWhitespace (Html.encode (template.getName ())));

		/*
		printFormat (
			"<td>%h</td>\n",
			template.getTicketState ()
				.getState ()
					.toString ());
		*/

		printFormat (

			"<td><input",

			" id=\"timestamp-%h\"",
			template.getId (),

			" type=\"textarea\"",

			/*
			" name=\"timestamp-%h\"",
			template.getTicketState ().getState ().toString (),
			*/

			" value=\"%d\"",
			template.getTicketState ().getMinimum (),

			"></td>\n");

		printFormat (
			"</tr>\n");

	}

}

