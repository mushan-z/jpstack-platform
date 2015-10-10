package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ObsoleteDateField;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueSupervisorItemsPart")
public
class QueueSupervisorItemsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	QueueItemConsoleHelper queueItemHelper;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleHelper userHelper;

	// state

	Set<QueueItemRec> queueItems;

	UserRec user;

	// implementation

	@Override
	public
	void prepare () {

		ObsoleteDateField dateField =
			ObsoleteDateField.parse (
				requestContext.parameter ("date"));

		int hour =
			Integer.parseInt (
				requestContext.parameter ("hour"));

		int userId =
			Integer.parseInt (
				requestContext.parameter ("userId"));

		user =
			userHelper.find (userId);

		DateTime startTime =
			dateField.date
				.toDateTimeAtStartOfDay ()
				.plusHours (hour);

		queueItems =
			new TreeSet<QueueItemRec> (
				QueueItemRec.processedTimeComparator);

		queueItems.addAll (
			queueItemHelper.findByProcessedTime (
				user,
				new Interval (
					startTime,
					startTime.plusHours (1))));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"%s\n",
			objectManager.tdForObjectMiniLink (
				user),
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Object</th>\n",
			"<th>Queue</th>\n",
			"<th>Item</th>\n",
			"<th>Created</th>\n",
			"<th>Pending</th>\n",
			"<th>Processed</th>\n",
			"</tr>\n");

		for (
			QueueItemRec queueItem
				: queueItems
		) {

			QueueRec queue =
				queueItem.getQueueSubject () != null
					? queueItem.getQueueSubject ().getQueue ()
					: queueItem.getQueue ();

			Record<?> parent =
				objectManager.getParent (
					queue);

			printFormat (
				"<tr>\n",

				"%s\n",
				objectManager.tdForObjectLink (
					parent),

				"%s\n",
				objectManager.tdForObjectMiniLink (
					queue,
					parent),

				"%s\n",
				objectManager.tdForObjectMiniLink (
					queueItem,
					queue),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						queueItem.getCreatedTime ())),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						queueItem.getPendingTime ())),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						queueItem.getProcessedTime ())),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
