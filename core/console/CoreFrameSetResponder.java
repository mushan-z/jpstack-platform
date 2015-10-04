package wbs.platform.core.console;

import java.io.IOException;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsolePrintResponder;

@PrototypeComponent ("coreFrameSetResponder")
public
class CoreFrameSetResponder
	extends ConsolePrintResponder {

	// dependencies

	@Inject
	WbsConfig wbsConfig;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	void render () {

		goDocType ();

		goHtml ();

	}

	@Override
	public
	void setHtmlHeaders ()
		throws IOException {

		super.setHtmlHeaders ();

		requestContext.setHeader (
			"Content-Type",
			"text/html; charset=utf-8");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.instantToHttpTimestampString (
				Instant.now ()));

	}

	public
	void goDocType () {

		printFormat (
			"<!DOCTYPE html>\n");

	}

	public
	void goHtml () {

		printFormat (
			"<html>\n");

		goHtmlStuff ();

		printFormat (
			"</html>\n");

	}

	public
	void goHtmlStuff () {

		goHead ();

		goFrameset ();

	}

	public
	void goHead () {

		printFormat (
			"<head>\n");

		goHeadStuff ();

		printFormat (
			"</head>\n");

	}

	public
	void goHeadStuff () {

		goTitle ();

		goScripts ();

		printFormat (
			"<link rel=\"shortcut icon\" href=\"%h\">\n",
			requestContext.resolveApplicationUrl (
				"/favicon.ico"));

		printFormat (
			"<link rel=\"icon\" href=\"%h\">\n",
			requestContext.resolveApplicationUrl (
				"/favicon.ico"));

	}

	public
	void goTitle () {

		printFormat (
			"<title>%h</title>\n",
			wbsConfig.consoleTitle ());

	}

	public
	void goScripts () {

		printFormat (
			"<script type=\"text/javascript\">\n",
			"  function show_inbox (show) {\n",
			"    document.getElementById ('right_frameset').rows = show? '2*,1*' : '*,0';\n",
			"  }\n",
			"</script>\n");

	}

	public
	void goFrameset () {

		printFormat (

			"<frameset cols=\"1*,4*\">\n",

			"<frameset rows=\"2*,1*\">\n",

			"<frame name=\"sidebar\" src=\"%h\">\n",
			requestContext.resolveApplicationUrl (
				"/sidebar"),

			"<frame name=\"status\" src=\"%h\">\n",
			requestContext.resolveApplicationUrl (
				"/status"),

			"</frameset>\n",

			"<frameset rows=\"1*,0\" id=\"right_frameset\">\n",

			"<frame name=\"main\" src=\"%h\">\n",
			requestContext.resolveApplicationUrl (
				"/home"),

			"<frame name=\"inbox\" src=\"%h\">\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),

			"</frameset>\n",

			"</frameset>\n");

	}

}
