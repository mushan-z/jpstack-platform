package wbs.integrations.clockworksms.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.object.ObjectManager;
import wbs.integrations.clockworksms.foreignapi.ClockworkSmsMessageRequest;
import wbs.integrations.clockworksms.foreignapi.ClockworkSmsMessageResponse;
import wbs.integrations.clockworksms.foreignapi.ClockworkSmsMessageSender;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutObjectHelper;
import wbs.integrations.clockworksms.model.ClockworkSmsRouteOutRec;
import wbs.sms.gsm.Gsm;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper;
import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("clockworkSmsSenderHelper")
public
class ClockworkSmsSenderHelper
	implements SmsSenderHelper<ClockworkSmsMessageSender> {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	ClockworkSmsRouteOutObjectHelper clockworkSmsRouteOutHelper;

	@Inject
	WbsConfig wbsConfig;

	// prototype dependencies

	@Inject
	Provider<ClockworkSmsMessageSender> clockworkSmsMessageSenderProvider;

	// details

	@Override
	public
	String senderCode () {
		return "clockwork_sms";
	}

	// implementation

	@Override
	public
	SetupRequestResult<ClockworkSmsMessageSender> setupRequest (
			@NonNull OutboxRec smsOutbox) {

		// get stuff

		MessageRec smsMessage =
			smsOutbox.getMessage ();

		RouteRec smsRoute =
			smsOutbox.getRoute ();

		// lookup route out

		Optional<ClockworkSmsRouteOutRec> clockworkSmsRouteOutOptional =
			clockworkSmsRouteOutHelper.find (
				smsRoute.getId ());

		if (
			isNotPresent (
				clockworkSmsRouteOutOptional)
		) {

			return new SetupRequestResult<ClockworkSmsMessageSender> ()

				.status (
					SetupRequestStatus.configError)

				.statusMessage (
					stringFormat (
						"Clockwork SMS outbound route not found for %s",
						smsRoute.getCode ()));

		}

		ClockworkSmsRouteOutRec clockworkSmsRouteOut =
			clockworkSmsRouteOutOptional.get ();

		// validate message text

		if (
			! Gsm.isGsm (
				smsMessage.getText ().getText ())
		) {

			return new SetupRequestResult<ClockworkSmsMessageSender> ()

				.status (
					SetupRequestStatus.validationError)

				.statusMessage (
					"The message text contains non-GSM characters");

		}

		long gsmLength =
			Gsm.length (
				smsMessage.getText ().getText ());

		long gsmParts =
			Gsm.parts (
				gsmLength);

		if (
			lessThan (
				clockworkSmsRouteOut.getMaxParts (),
				gsmParts)
		) {

			return new SetupRequestResult<ClockworkSmsMessageSender> ()

				.status (
					SetupRequestStatus.validationError)

				.statusMessage (
					stringFormat (
						"Message has length %s ",
						gsmLength,
						"and so would be split into %s parts ",
						gsmParts,
						"but the maximum configured for this route is %s",
						clockworkSmsRouteOut.getMaxParts ()));

		}

		// pick a handler

		if (
			equal (
				smsMessage.getMessageType ().getCode (),
				"sms")
		) {

			// nothing to do

		} else {

			return new SetupRequestResult<ClockworkSmsMessageSender> ()

				.status (
					SetupRequestStatus.unknownError)

				.statusMessage (
					stringFormat (
						"Don't know what to do with a %s",
						smsMessage.getMessageType ().getCode ()));

		}

		// create request

		ClockworkSmsMessageRequest clockworkRequest =
			new ClockworkSmsMessageRequest ()

			.key (
				clockworkSmsRouteOut.getKey ())

			.sms (
				ImmutableList.of (
					new ClockworkSmsMessageRequest.Sms ()

				.to (
					smsMessage.getNumTo ())

				.from (
					smsMessage.getNumFrom ())

				.content (
					smsMessage.getText ().getText ())

				.msgType (
					"TEXT")

				.concat (
					clockworkSmsRouteOut.getMaxParts ())

				.clientId (
					Integer.toString (
						smsMessage.getId ()))

				.dlrType (
					4l)

				.dlrUrl (
					stringFormat (
						"%s/clockworkSms/route/%s/report",
						wbsConfig.apiUrl (),
						smsRoute.getId ()))

				.uniqueId (
					1l)

				.invalidCharAction (
					1l)

				.truncate (
					0l)

			));

		// create sender

		ClockworkSmsMessageSender clockworkSender =
			clockworkSmsMessageSenderProvider.get ()

			.url (
				clockworkSmsRouteOut.getUrl ())

			.request (
				clockworkRequest);

		// encode request

		clockworkSender.encode ();

		// return

		return new SetupRequestResult<ClockworkSmsMessageSender> ()

			.status (
				SetupRequestStatus.success)

			.requestTrace (
				clockworkSender.requestTrace ())

			.state (
				clockworkSender);

	}

	@Override
	public
	PerformSendResult performSend (
			@NonNull ClockworkSmsMessageSender clockworkSender) {

		PerformSendResult result =
			new PerformSendResult ();

		// encode

		try {

			clockworkSender.send ();

			clockworkSender.receive ();

			result.responseTrace (
				clockworkSender.responseTrace ());

			clockworkSender.decode ();

			return result

				.status (
					PerformSendStatus.success);

		} catch (Exception exception) {

			return result

				.status (
					PerformSendStatus.communicationError)

				.exception (
					exception);

		}

	}

	@Override
	public
	ProcessResponseResult processSend (
			@NonNull ClockworkSmsMessageSender clockworkSender) {

		ClockworkSmsMessageResponse clockworkResponse =
			clockworkSender.clockworkResponse ();

		// check for general error

		if (
			isNotNull (
				clockworkResponse.errNo ())
		) {
			throw new RuntimeException ();
		}

		// check for individual error

		ClockworkSmsMessageResponse.SmsResp clockworkSmsResponse =
			clockworkResponse.smsResp ().get (0);

		if (
			isNotNull (
				clockworkSmsResponse.errNo ())
		) {

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.remoteError)

				.statusMessage (
					clockworkSmsResponse.errDesc ())

				.failureType (
					FailureType.temporary);

		}

		// success response

		return new ProcessResponseResult ()

			.status (
				ProcessResponseStatus.success)

			.otherIds (
				ImmutableList.of (
					clockworkSmsResponse.messageId ()));

	}

}
