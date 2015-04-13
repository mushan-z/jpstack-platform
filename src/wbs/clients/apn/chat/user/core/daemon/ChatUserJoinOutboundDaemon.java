package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.contact.logic.ChatMessageLogic;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.clients.apn.chat.contact.model.ChatUserInitiationReason;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;

import com.google.common.base.Optional;

@SingletonComponent ("chatUserJoinOutboundDaemon")
public
class ChatUserJoinOutboundDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatMessageLogic chatMessageLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserInitiationLogObjectHelper chatUserInitiationLogHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	// details

	@Override
	protected
	String getThreadName () {
		return "ChatUserJoinOut";
	}

	@Override
	protected
	int getDelayMs () {
		return 10 * 1000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "chat user join outbound daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for chat user join outbounds";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		// get a list of users who are past their outbound timestamp

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<ChatUserRec> chatUsers =
			chatUserHelper.findWantingJoinOutbound ();

		transaction.close ();

		// then do each one

		for (ChatUserRec chatUser
				: chatUsers) {

			try {

				doChatUserJoinOutbound (
					chatUser.getId ());

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					stringFormat (
						"chat user ",
						chatUser.getId ()),
					exception,
					Optional.<Integer>absent (),
					false);

			}

		}

	}

	void doChatUserJoinOutbound (
			int chatUserId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// find the user

		ChatUserRec user =
			chatUserHelper.find (
				chatUserId);

		// check and clear the outbound message flag

		if (user.getNextJoinOutbound() == null
				|| new Date ().getTime ()
					< user.getNextJoinOutbound ().getTime ()) {

			return;

		}

		user.setNextJoinOutbound (null);

		// find a monitor

		ChatUserRec monitor =
			chatMiscLogic.getOnlineMonitorForOutbound (
				user);

		if (monitor == null) {

			transaction.commit ();

			return;

		}

		// create or update the cmi

		ChatMonitorInboxRec chatMonitorInbox =
			chatMessageLogic.findOrCreateChatMonitorInbox (
				monitor,
				user,
				true);

		chatMonitorInbox
			.setOutbound (true);

		// create a log

		chatUserInitiationLogHelper.insert (
			new ChatUserInitiationLogRec ()

			.setChatUser (
				user)

			.setMonitorChatUser (
				monitor)

			.setReason (
				ChatUserInitiationReason.joinUser)

			.setTimestamp (
				new Date ())

		);

		transaction.commit ();

	}

}
