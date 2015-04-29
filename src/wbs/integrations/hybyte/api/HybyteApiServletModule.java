package wbs.integrations.hybyte.api;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.AbstractWebFile;
import wbs.framework.web.PathHandler;
import wbs.framework.web.RegexpPathHandler;
import wbs.framework.web.RequestContext;
import wbs.framework.web.ServletModule;
import wbs.framework.web.WebFile;
import wbs.integrations.hybyte.model.HybyteNetworkObjectHelper;
import wbs.integrations.hybyte.model.HybyteNetworkRec;
import wbs.integrations.hybyte.model.HybyteRouteObjectHelper;
import wbs.integrations.hybyte.model.HybyteRouteOutObjectHelper;
import wbs.integrations.hybyte.model.HybyteRouteOutRec;
import wbs.integrations.hybyte.model.HybyteRouteRec;
import wbs.platform.exception.logic.ExceptionLogLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.logic.InboxMultipartLogic;
import wbs.sms.message.report.logic.ReportLogic;
import wbs.sms.message.report.model.MessageReportCodeObjectHelper;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.message.report.model.MessageReportCodeType;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.format.logic.NumberFormatLogic;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@Log4j
@SingletonComponent ("hybyteApiServletModule")
public
class HybyteApiServletModule
	implements ServletModule {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogLogic exceptionLogic;

	@Inject
	HybyteNetworkObjectHelper hybyteNetworkHelper;

	@Inject
	HybyteRouteObjectHelper hybyteRouteHelper;

	@Inject
	HybyteRouteOutObjectHelper hybyteRouteOutHelper;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	InboxMultipartLogic inboxMultipartLogic;

	@Inject
	MessageReportCodeObjectHelper messageReportCodeHelper;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	ReportLogic reportLogic;

	@Inject
	RequestContext requestContext;

	@Inject
	TextObjectHelper textHelper;

	// in file

	WebFile inFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			Document xmlDocument = null;

			try {

				@Cleanup
				Transaction transaction =
					database.beginReadWrite (
						this);

				// get ids

				int routeId =
					requestContext.requestInt ("routeId");

				// process xml

				SAXBuilder builder =
					new SAXBuilder ();

				xmlDocument =
					builder.build (
						requestContext.inputStream ());

				InRequestResult inRequestResult =
					processInRequest (xmlDocument);

				// lookup route

				HybyteRouteRec hybyteRoute =
					hybyteRouteHelper.find (
						routeId);

				log.debug("Hybyte network: [" + inRequestResult.net + "]");

				// update network if provided

				HybyteNetworkRec hybyteNetwork =
					hybyteNetworkHelper.findByInText (
						inRequestResult.net);

				NetworkRec network =
					hybyteNetwork != null
						? hybyteNetwork.getNetwork ()
						: null;

				if (inRequestResult.multipart) {

					// insert multipart message

					inboxMultipartLogic.insertInboxMultipart (
						hybyteRoute.getRoute (),
						inRequestResult.multipartId,
						inRequestResult.multipartMaxSeg,
						inRequestResult.multipartSeg,
						numberFormatLogic.parse (
							hybyteRoute.getNumberFormat (),
							inRequestResult.to),
						numberFormatLogic.parse (
							hybyteRoute.getNumberFormat (),
							inRequestResult.from),
						null,
						network,
						inRequestResult.uuid,
						inRequestResult.message);

				} else {

					// insert message

					MessageRec message =
						inboxLogic.inboxInsert (
							Optional.of (inRequestResult.uuid),
							textHelper.findOrCreate (
								inRequestResult.message),
							numberFormatLogic.parse (
								hybyteRoute.getNumberFormat (),
								inRequestResult.from),
							numberFormatLogic.parse (
								hybyteRoute.getNumberFormat (),
								inRequestResult.to),
							hybyteRoute.getRoute (),
							Optional.of (network),
							Optional.<Instant>absent (),
							Collections.<MediaRec>emptyList (),
							Optional.<String>absent (),
							Optional.<String>absent ());

					// output a message

					log.info (
						"Saved " + inRequestResult.uuid + " as " + message.getId ());

				}

				transaction.commit ();

			} catch (Exception exception) {

				StringBuilder stringBuilder =
					new StringBuilder ();

				stringBuilder.append (
					exceptionLogic.throwableDump (
						exception));

				if (xmlDocument != null) {

					stringBuilder.append (
						"\n\nXML DOCUMENT\n\n");

					XMLOutputter xmlOutputter =
						new XMLOutputter (
							Format.getPrettyFormat ());

					stringBuilder.append (
						xmlOutputter.outputString (xmlDocument));

				}

				exceptionLogic.logSimple (
					"webapi",
					requestContext.requestUri (),
					exceptionLogic.throwableSummary (
						exception),
					stringBuilder.toString (),
					Optional.<Integer>absent (),
					false);

			}

		}

	};

	private static
	class InRequestResult {

		String from, to, time, net, uuid, message;

		boolean multipart = false;

		int multipartId, multipartSeg, multipartMaxSeg;

	}

	static
	InRequestResult processInRequest (
			Document doc) {

		try {

			InRequestResult ret = new InRequestResult();

			Element deliveryElem = doc.getRootElement().getChild("deliveries")
					.getChild("delivery");

			if (deliveryElem == null)
				throw new RuntimeException("No delivery element");

			ret.from = deliveryElem.getAttributeValue("from");
			ret.to = deliveryElem.getAttributeValue("to");
			ret.time = deliveryElem.getAttributeValue("time");
			ret.net = deliveryElem.getAttributeValue("net");
			ret.uuid = deliveryElem.getAttributeValue("uuid");

			ret.message = deliveryElem.getText();

			String multipartId = deliveryElem.getAttributeValue("multipart_id");
			String multipartSeg = deliveryElem
					.getAttributeValue("multipart_seg");
			String multipartMaxSeg = deliveryElem
					.getAttributeValue("multipart_max_seg");

			if (multipartId != null || multipartSeg != null
					|| multipartMaxSeg != null) {
				if (multipartId == null || multipartSeg == null
						|| multipartMaxSeg == null)
					throw new RuntimeException(
							"Got some but not all multipart attributes");
				ret.multipart = true;
				ret.multipartId = Integer.parseInt(multipartId);
				ret.multipartSeg = Integer.parseInt(multipartSeg);
				ret.multipartMaxSeg = Integer.parseInt(multipartMaxSeg);
			}

			if (ret.from == null)
				throw new RuntimeException(
						"delivery from attribute not specified");
			if (ret.to == null)
				throw new RuntimeException(
						"delivery to attribute not specified");
			if (ret.time == null)
				throw new RuntimeException(
						"delivery time attribute not specified");
			if (ret.net == null)
				throw new RuntimeException(
						"delivery net attribute not specified");
			if (ret.uuid == null)
				throw new RuntimeException(
						"delivery uuid attribute not specified");

			return ret;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ============================================================ report file

	private
	WebFile reportFile =
		new AbstractWebFile () {

		@Override
		public
		void doPost ()
			throws
				ServletException,
				IOException {

			ReportRequestResult req =
				processReportRequest (
					requestContext.inputStream ());

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					this);

			// get route

			HybyteRouteOutRec hybyteRouteOut =
				hybyteRouteOutHelper.find (
					requestContext.requestInt ("routeId"));

			if (hybyteRouteOut == null) {

				throw new RuntimeException (
					stringFormat (
						"No such hybyte route out: %s",
						requestContext.requestInt("routeId")));

			}

			Integer statusCode = null;

			try {

				statusCode =
					Integer.parseInt (req.code);

			} catch (NumberFormatException exception) {

				statusCode = null;

			}

			Integer statusType = null;
			Integer reason = null;

			// update message report code

			MessageReportCodeRec messageReportCode =
				messageReportCodeHelper.findOrCreate (
					statusCode,
					statusType,
					reason,
					MessageReportCodeType.hybyte,
					req.success,
					false,
					req.description);

			// process delivery report

			//MessageRec message;

			try {

				//message =
					reportLogic.deliveryReport (
						hybyteRouteOut.getRoute (),
						req.otherId,
						req.success
							? MessageStatus.delivered
							: MessageStatus.undelivered,
						null,
						messageReportCode);

			} catch (NoSuchMessageException exception) {

				if (hybyteRouteOut.getFreeRoute() == null)
					throw new RuntimeException (exception);

				// try the free route

				try {

					//message =
						reportLogic.deliveryReport (
							hybyteRouteOut.getFreeRoute ().getRoute (),
							req.otherId,
							req.success
								? MessageStatus.delivered
								: MessageStatus.undelivered,
							null,
							messageReportCode);

				} catch (NoSuchMessageException noSuchMessageException) {

					// just throw the original exception

					throw new RuntimeException (
						noSuchMessageException);

				}

			}

			// update hybyte network

			/*
			if (req.network != null) {

				HybyteNetworkRec hybyteNetwork =
					hybyteNetworkHelper.findByInText (
						req.network);

				if (hybyteNetwork != null) {

					message.getNumber ().setNetwork (
						hybyteNetwork.getNetwork ());

				}

			}
			*/

			transaction.commit ();

			sendReportResponse (
				requestContext.outputStream ());

		}

	};

	static
	class ReportRequestResult {

		Boolean success;
		String otherId;
		String code;
		String network;
		String description;

	}

	static
	ReportRequestResult processReportRequest (
			InputStream in) {

		try {

			ReportRequestResult ret =
				new ReportRequestResult ();

			SAXBuilder builder =
				new SAXBuilder ();

			Document document =
				builder.build (in);

			Element receiptElem =
				document.getRootElement ();

			if (! equal (
					receiptElem.getName (),
					"airbytereceipt")) {

				throw new RuntimeException (
					"Don't know what to do with a <" + receiptElem.getName () + ">");

			}

			ret.otherId =
				receiptElem.getChild ("uuid").getText ();

			ret.code =
				receiptElem.getChild ("reason").getText ();

			if (receiptElem.getChild ("network") != null) {

				ret.network =
					receiptElem.getChild ("network").getText ();

			}

			String status =
				receiptElem.getChild ("status").getText ();

			if (status.equalsIgnoreCase ("delivered")) {

				ret.success = true;

			} else if (status.equalsIgnoreCase ("failed")) {

				ret.success = false;

			} else {

				throw new RuntimeException (
					"Unknown status: " + status);

			}

			ret.description = status;

			return ret;

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

	static
	void sendReportResponse (
			OutputStream out)
		throws IOException {

		Namespace xsiNamespace =
			Namespace.getNamespace (
				"xsi",
				"http://www.w3.org/2001/XMLSchema-instance");

		Element airbyteElem =
			new Element ("Airbyte");

		airbyteElem.setAttribute (
			"noNamespaceSchemaLocation",
			"http://airbyte-dtds.airmessaging.net/airbyte.xsd",
			xsiNamespace);

		Document document =
			new Document (airbyteElem);

		Element airbyteReceiptElem =
			new Element ("AirbyteReceipt");

		airbyteElem.addContent (
			airbyteReceiptElem);

		Element ackElem =
			new Element ("ack");

		ackElem.setText (
			"OK");

		airbyteReceiptElem.addContent (
			ackElem);

		XMLOutputter xmlOutputter =
			new XMLOutputter (
				Format.getPrettyFormat ());

		xmlOutputter.output (
			document,
			out);

	}

	// ============================================================ entries

	final
	RegexpPathHandler.Entry routeEntry =
		new RegexpPathHandler.Entry (
			"/route/([0-9]+)/([^/]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return defaultFiles.get (
				matcher.group (2));

		}

	};

	final
	RegexpPathHandler.Entry inEntry =
		new RegexpPathHandler.Entry (
			"/in/([0-9]+)") {

		@Override
		protected
		WebFile handle (
				Matcher matcher) {

			requestContext.request (
				"routeId",
				Integer.parseInt (matcher.group (1)));

			return inFile;

		}

	};

	// ================================= path handler

	final
	PathHandler pathHandler =
		new RegexpPathHandler (
			routeEntry,
			inEntry);

	// ================================= files

	final
	Map<String,WebFile> defaultFiles =
		ImmutableMap.<String,WebFile>builder ()
			.put ("report", reportFile)
			.put ("in", inFile)
			.build ();

	// ================================= servlet module

	@Override
	public
	Map<String,PathHandler> paths () {

		return ImmutableMap.<String,PathHandler>builder ()
			.put ("/hybyte", pathHandler)
			.build ();

	}

	@Override
	public
	Map<String,WebFile> files () {
		return null;
	}

}
