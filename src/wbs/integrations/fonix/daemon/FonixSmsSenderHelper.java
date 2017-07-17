package wbs.integrations.fonix.daemon;

import static wbs.sms.gsm.GsmUtils.gsmStringIsNotValid;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanOne;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.integrations.fonix.foreignapi.FonixMessageSendRequest;
import wbs.integrations.fonix.foreignapi.FonixMessageSendResponse;
import wbs.integrations.fonix.model.FonixRouteOutObjectHelper;
import wbs.integrations.fonix.model.FonixRouteOutRec;

import wbs.platform.scaffold.model.RootObjectHelper;

import wbs.sms.gsm.GsmUtils;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper;
import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("fonixSmsSenderHelper")
public
class FonixSmsSenderHelper
	implements SmsSenderHelper <GenericHttpSender <
		FonixMessageSendRequest,
		FonixMessageSendResponse
	>> {

	// singleton dependencies

	@SingletonDependency
	FonixRouteOutObjectHelper fonixRouteOutHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RootObjectHelper rootHelper;

	@SingletonDependency
	SmsMessageLogic smsMessageLogic;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("fonixMessageSender")
	ComponentProvider <GenericHttpSender <
		FonixMessageSendRequest,
		FonixMessageSendResponse
	>> fonixMessageSenderProvider;

	// details

	@Override
	public
	String senderCode () {
		return "fonix";
	}

	// implementation

	@Override
	public
	SetupRequestResult <GenericHttpSender <
		FonixMessageSendRequest,
		FonixMessageSendResponse
	>> setupRequest (
			@NonNull Transaction parentTransaction,
			@NonNull OutboxRec smsOutbox) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setupRequest");

		) {

			// get stuff

			MessageRec smsMessage =
				smsOutbox.getMessage ();

			RouteRec smsRoute =
				smsOutbox.getRoute ();

			// lookup route out

			Optional <FonixRouteOutRec> fonixRouteOutOptional =
				fonixRouteOutHelper.find (
					transaction,
					smsRoute.getId ());

			if (
				optionalIsNotPresent (
					fonixRouteOutOptional)
			) {

				return new SetupRequestResult <GenericHttpSender <
					FonixMessageSendRequest,
					FonixMessageSendResponse
				>> ()

					.status (
						SetupRequestStatus.configError)

					.statusMessage (
						stringFormat (
							"Fonix outbound route not found for %s",
							smsRoute.getCode ()))

				;

			}

			FonixRouteOutRec fonixRouteOut =
				fonixRouteOutOptional.get ();

			// validate message text

			if (
				gsmStringIsNotValid (
					smsMessage.getText ().getText ())
			) {

				return new SetupRequestResult <GenericHttpSender <
					FonixMessageSendRequest,
					FonixMessageSendResponse
				>> ()

					.status (
						SetupRequestStatus.validationError)

					.statusMessage (
						"The message text contains non-GSM characters")

				;

			}

			long gsmLength =
				GsmUtils.gsmStringLength (
					smsMessage.getText ().getText ());

			long gsmParts =
				GsmUtils.gsmCountMessageParts (
					gsmLength);

			if (
				moreThanOne (
					gsmParts)
			) {

				return new SetupRequestResult <GenericHttpSender <
					FonixMessageSendRequest,
					FonixMessageSendResponse
				>> ()

					.status (
						SetupRequestStatus.validationError)

					.statusMessage (
						stringFormat (
							"Message has length %s ",
							integerToDecimalString (
								gsmLength),
							"and so would be split into %s parts ",
							integerToDecimalString (
								gsmParts),
							"but the maximum is one"))

				;

			}

			// pick a handler

			if (
				stringEqualSafe (
					smsMessage.getMessageType ().getCode (),
					"sms")
			) {

				// nothing to do

			} else {

				return new SetupRequestResult <GenericHttpSender <
					FonixMessageSendRequest,
					FonixMessageSendResponse
				>> ()

					.status (
						SetupRequestStatus.unknownError)

					.statusMessage (
						stringFormat (
							"Don't know what to do with a %s",
							smsMessage.getMessageType ().getCode ()))

				;

			}

			// create request

			FonixMessageSendRequest fonixRequest =
				new FonixMessageSendRequest ()

				.url (
					fonixRouteOut.getUrl ())

				.apiKey (
					fonixRouteOut.getApiKey ())

				.id (
					smsMessage.getId ())

				.originator (
					smsMessage.getNumFrom ())

				.numbers (
					ImmutableList.of (
						smsMessage.getNumTo ()))

				.body (
					smsMessage.getText ().getText ())

				.dummy (
					false);

			// create sender

			GenericHttpSender <
				FonixMessageSendRequest,
				FonixMessageSendResponse
			>
				fonixSender =
					fonixMessageSenderProvider.provide (
						transaction,
						nestedFonixSender ->
							nestedFonixSender

				.request (
					fonixRequest)

			);

			// encode request

			fonixSender.encode (
				transaction);

			// return

			return new SetupRequestResult <GenericHttpSender <
				FonixMessageSendRequest,
				FonixMessageSendResponse
			>> ()

				.status (
					SetupRequestStatus.success)

				.requestTrace (
					fonixSender.requestTrace ())

				.state (
					fonixSender)

			;

		}

	}

	@Override
	public
	PerformSendResult performSend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull GenericHttpSender <
				FonixMessageSendRequest,
				FonixMessageSendResponse
			> fonixSender) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"performSend");

		) {

			PerformSendResult result =
				new PerformSendResult ();

			// encode

			try {

				fonixSender.send (
					taskLogger);

				fonixSender.receive (
					taskLogger);

				result.responseTrace (
					fonixSender.responseTrace ());

				fonixSender.decode (
					taskLogger);

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

	}

	@Override
	public
	ProcessResponseResult processSend (
			@NonNull Transaction parentTransaction,
			@NonNull GenericHttpSender <
				FonixMessageSendRequest,
				FonixMessageSendResponse
			> fonixSender) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processSend");

		) {

			// check for generic error
	
			if (
				optionalIsPresent (
					fonixSender.errorMessage ())
			) {
	
				return new ProcessResponseResult ()
	
					.status (
						ProcessResponseStatus.remoteError)
	
					.statusMessage (
						fonixSender.errorMessage ().get ())
	
					.failureType (
						FailureType.temporary);
	
			}
	
			// check for fonix error
	
			FonixMessageSendResponse fonixResponse =
				fonixSender.response ();
	
			if (
				isNotNull (
					fonixResponse.failure ())
			) {
	
				return handleGeneralError (
					fonixResponse);
	
			}
	
			// success response
	
			FonixMessageSendResponse.Success fonixSuccess =
				fonixResponse.success ();
	
			return new ProcessResponseResult ()
	
				.status (
					ProcessResponseStatus.success)
	
				.otherIds (
					ImmutableList.of (
						fonixSuccess.txguid ()));

		}

	}

	private
	ProcessResponseResult handleGeneralError (
			@NonNull FonixMessageSendResponse fonixResponse) {

		FonixMessageSendResponse.Failure fonixFailure =
			fonixResponse.failure ();

		if (
			isNotNull (
				fonixFailure.parameter ())
		) {

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.remoteError)

				.statusMessage (
					stringFormat (
						"Parameter error: %s: %s",
						fonixFailure.parameter (),
						fonixFailure.failcode ()))

				.failureType (
					FailureType.temporary);

		} else {

			return new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.remoteError)

				.statusMessage (
					"Unknown error")

				.failureType (
					FailureType.temporary);

		}

	}

}