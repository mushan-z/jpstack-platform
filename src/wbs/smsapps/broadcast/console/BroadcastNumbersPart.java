package wbs.smsapps.broadcast.console;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.smsapps.broadcast.model.BroadcastRec;

@Accessors (fluent = true)
@PrototypeComponent ("broadcastNumbersPart")
public
class BroadcastNumbersPart
	extends AbstractPagePart {

	@Inject
	BroadcastConsoleHelper broadcastHelper;

	BroadcastRec broadcast;

	@Override
	public
	void prepare () {

		broadcast =
			broadcastHelper.find (
				requestContext.stuffInt (
					"broadcastId"));

	}

	@Override
	public
	void goBodyStuff () {

		goDetails ();

		goForm ();

	}

	void goDetails () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Total accepted</th>\n",
			"<td>%s</td>\n",
			broadcast.getNumAccepted (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Total rejected</th>\n",
			"<td>%s</td>\n",
			broadcast.getNumRejected (),
			"</tr>\n");

		printFormat (
			"</table>\n");

	}

	void goForm () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p>Numbers<br>\n",

			"<textarea",
			" name=\"numbers\"",
			" rows=\"8\"",
			" cols=\"60\"",
			">%h</textarea></p>\n",
			requestContext.parameter ("numbers"));

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"add\"",
			" value=\"add numbers\"",
			">\n",

			"<input",
			" type=\"submit\"",
			" name=\"remove\"",
			" value=\"remove numbers\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}