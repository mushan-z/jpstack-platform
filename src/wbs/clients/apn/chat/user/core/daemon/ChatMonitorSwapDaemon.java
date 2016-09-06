package wbs.clients.apn.chat.user.core.daemon;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.joinWithCommaAndSpace;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;

import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.utils.RandomLogic;
import wbs.platform.daemon.SleepingDaemonService;

@Log4j
@SingletonComponent ("chatMonitorSwapDaemon")
public
class ChatMonitorSwapDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	RandomLogic randomLogic;

	// details

	@Override
	protected
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			10);

	}

	@Override
	protected
	String generalErrorSource () {
		return "chat monitor swap daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error swapping monitors";
	}

	@Override
	protected
	String getThreadName () {
		return "ChatMonitorSwap";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		// get list of chats

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ChatMonitorSwapDaemon.runOnce ()",
				this);

		List<ChatRec> chats =
			chatHelper.findAll ();

		transaction.close ();

		// then call doMonitorSwap for any whose time has come

		for (
			ChatRec chat
				: chats
		) {

			if (

				isNull (
					chat.getLastMonitorSwap ())

				|| earlierThan (
					chat.getLastMonitorSwap ().plus (
						chat.getTimeMonitorSwap () * 1000),
					transaction.now ())

			) {

				doMonitorSwap (
					chat.getId (),
					randomLogic.sample (
						Gender.values ()),
					randomLogic.sample (
						Orient.values ()));

			}

		}

	}

	void doMonitorSwap (
			@NonNull Long chatId,
			@NonNull Gender gender,
			@NonNull Orient orient) {

		try {

			doMonitorSwapReal (
				chatId,
				gender,
				orient);

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"daemon",
				stringFormat (
					"chat %s %s %s",
					chatId,
					gender.toString (),
					orient.toString ()),
				exception,
				Optional.absent (),
				GenericExceptionResolution.tryAgainLater);

		}

	}

	void doMonitorSwapReal (
			@NonNull Long chatId,
			@NonNull Gender gender,
			@NonNull Orient orient) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s)",
					"ChatMonitorSwapDaemon",
					"doMonitorSwapReal",
					joinWithCommaAndSpace (
						stringFormat (
							"chatId = %s",
							chatId),
						stringFormat (
							"gender = %s",
							gender),
						stringFormat (
							"orient = %s",
							orient))),
				this);

		ChatRec chat =
			chatHelper.findRequired (
				chatId);

		chat

			.setLastMonitorSwap (
				transaction.now ());

		// fetch all appropriate monitors

		List<ChatUserRec> allMonitors =
			chatUserHelper.find (
				chat,
				ChatUserType.monitor,
				orient,
				gender);

		// now sort into online and offline ones

		List<ChatUserRec> onlineMonitors =
			new ArrayList<ChatUserRec> ();

		List<ChatUserRec> offlineMonitors =
			new ArrayList<ChatUserRec> ();

		for (
			ChatUserRec monitor
				: allMonitors
		) {

			if (monitor.getOnline ()) {

				onlineMonitors.add (
					monitor);

			} else {

				offlineMonitors.add (
					monitor);

			}

		}

		if (onlineMonitors.size () == 0
				|| offlineMonitors.size () == 0)
			return;

		// pick a random monitor to take offline

		ChatUserRec monitor =
			randomLogic.sample (
				onlineMonitors);

		monitor

			.setOnline (
				false);

		String tookOff =
			monitor.getCode ();

		// pick a random monitor to bring online

		monitor =
			randomLogic.sample (
				offlineMonitors);

		monitor

			.setOnline (
				true);

		String putOn =
			monitor.getCode ();

		log.info (
			stringFormat (
				"Swapping %s %s monitor %s for %s",
				orient, gender,
				tookOff,
				putOn));

		transaction.commit ();

	}

}
