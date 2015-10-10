package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Data;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueUsersPart")
public
class QueueUsersPart
	extends AbstractPagePart {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@Inject
	UserObjectHelper userHelper;

	UserRec myUser;
	List<UserData> userDatas;
	Instant now;

	@Override
	public
	void prepare () {

		now =
			Instant.now ();

		myUser =
			userHelper.find (
				requestContext.userId ());

		Map<Integer,UserData> temp =
			new HashMap<Integer,UserData> ();

		List<QueueItemClaimRec> queueItemClaims =
			queueItemClaimHelper.findClaimed ();

		for (QueueItemClaimRec queueItemClaim
				: queueItemClaims) {

			QueueItemRec queueItem =
				queueItemClaim.getQueueItem ();

			QueueRec queue =
				queueItem.getQueue ();

			Record<?> parent =
				objectManager.getParent (queue);

			if (! privChecker.can (
					parent,
					"manage"))
				continue;

			Instant createdTime =
				dateToInstant (queueItem.getCreatedTime ());

			UserData line =
				temp.get (queueItemClaim.getUser ().getId ());

			if (line == null) {

				temp.put (
					queueItemClaim.getUser ().getId (),
					line = new UserData ()
						.user (queueItemClaim.getUser ())
						.oldest (createdTime)
						.count (0));

			}

			if (createdTime.isBefore (line.oldest))
				line.oldest = createdTime;

			line.count ++;

		}

		userDatas =
			new ArrayList<UserData> (
				temp.values ());

		Collections.sort (userDatas);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",
			"<th>Items</th>\n",
			"<th>Oldest</th>\n",
			"<th>Reclaim</th>\n",
			"<th>Unclaim</th>\n",
			"</tr>\n");

		for (UserData userData
				: userDatas) {

			printFormat (
				"<tr>\n");

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					userData.user));

			printFormat (
				"<td align=\"right\">%s</td>\n",
				userData.count);

			printFormat (
				"<td>%s</td>\n",
				requestContext.prettyDateDiff (userData.oldest, now));

			if (userData.user == myUser) {

				printFormat (
					"<td></td>\n");

			} else {

				printFormat (
					"<td>",

					"<form method=\"post\" action=\"%h\">",
					 requestContext.resolveLocalUrl ("/queue.users"),

					"<input type=\"hidden\" name=\"userId\" value=\"%h\">",
					userData.user.getId (),

					"<input type=\"submit\" name=\"reclaim\" value=\"reclaim\">",

					"</form></td>\n");

			}

			printFormat (
				"<td>",

				"<form method=\"post\" action=\"%h\">",
				requestContext.resolveLocalUrl ("/queue.users"),

				"<input type=\"hidden\" name=\"userId\" value=\"%h\">",
				userData.user.getId (),

				"<input type=\"submit\" name=\"unclaim\" value=\"unclaim\">\n",

				"</form></td>\n",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

	@Accessors (fluent = true)
	@Data
	static
	class UserData
		implements Comparable<UserData> {

		UserRec user;
		Instant oldest;
		int count;

		@Override
		public
		int compareTo (
				UserData other) {

			return new CompareToBuilder ()

				.append (
					oldest,
					other.oldest)

				.append (
					user,
					other.user)

				.toComparison ();

		}

	}

}
