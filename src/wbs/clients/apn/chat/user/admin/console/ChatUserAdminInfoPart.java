package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.ifNull;

import java.util.Set;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.console.ChatConsoleLogic;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("chatUserAdminInfoPart")
public
class ChatUserAdminInfoPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleLogic chatConsoleLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/wbs.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void prepare () {

		int chatUserId =
			requestContext.stuffInt ("chatUserId");

		chatUser =
			chatUserHelper.find (
				chatUserId);
	}

	@Override
	public
	void goBodyStuff() {

		if (requestContext.canContext ("chat.userAdmin")) {

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					"/chatUser.admin.info"),
				">\n");

			printFormat (
					"<table class=\"details\" border=\"0\" cellspacing=\"1\">\n",

					"<tr> <th>Info</th> <td>\n",

					"<textarea id=\"info\" name=\"info\" rows=\"3\" cols=\"64\""
							+ " onkeyup=\"gsmCharCount (this, document.getElementById ('chars'), 0)\""
							+ " onfocus=\"gsmCharCount (this, document.getElementById ('chars'), 0)\">"
							+ "%h</textarea> </td> </tr>\n",
					ifNull (
						requestContext.getForm ("info"),
						chatUser.getInfoText () != null
							? chatUser.getInfoText ().getText ()
							: ""),

					"<tr> <th>Chars</th> <td><span id=\"chars\">&nbsp;</span></td> </tr>\n",

					"<tr> <th>Reason</th> <td>\n",
					"%s\n",
					chatConsoleLogic.selectForChatUserEditReason (
						"editReason",
						requestContext.getForm ("editReason")),

					"</td> </tr>\n");

				printFormat (
					"<tr>\n",
					"<th>Action</th>\n",

					"<td><input\n",
					" type=\"submit\"\n",
					" value=\"save changes\"",
					"></td>\n",

					"</tr>\n");

				printFormat (
					"</table>\n");

				printFormat (
					"</form>\n");

		}

		Set<ChatUserInfoRec> chatUserInfos =
			chatUser.getChatUserInfos ();

		if (chatUserInfos.size () == 0)
			return;

		printFormat (
			"<h3>History</h3>\n");

		printFormat (
			"<table",
			" class=\"list\"",
			" border=\"0\"",
			" cellspacing=\"1\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Timestamp</th>\n",
			"<th>Original</th>\n",
			"<th>Edited</th>\n",
			"<th>Status</th>\n",
			"<th>Moderator</th>\n",
			"</tr>\n");

		for (ChatUserInfoRec chatUserInfo
				: chatUserInfos) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					chatUserLogic.timezone (
						chatUser),
					dateToInstant (
						chatUserInfo.getCreationTime ())),

				"<td>%h</td>\n",
				chatUserInfo.getOriginalText () != null
					? chatUserInfo.getOriginalText ().getText ()
					: "-",

				"<td>%h</td>\n",
				chatUserInfo.getEditedText () != null
					? chatUserInfo.getEditedText ().getText ()
					: "-",

				"<td>%h</td>\n",
				chatConsoleLogic.textForChatUserInfoStatus (
					chatUserInfo.getStatus ()),

				chatUserInfo.getModerator () == null
					? "-"
					: objectManager.tdForObjectMiniLink (
						chatUserInfo.getModerator ()),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
