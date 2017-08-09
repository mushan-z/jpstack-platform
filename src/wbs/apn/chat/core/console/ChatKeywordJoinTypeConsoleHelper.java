package wbs.apn.chat.core.console;

import wbs.console.helper.enums.EnumConsoleHelper;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.apn.chat.keyword.model.ChatKeywordJoinType;

@SingletonComponent ("chatKeywordJoinTypeConsoleHelper")
public
class ChatKeywordJoinTypeConsoleHelper
	extends EnumConsoleHelper<ChatKeywordJoinType> {

	{

		enumClass (
			ChatKeywordJoinType.class);

		add (
			ChatKeywordJoinType.chatSimple,
			"chat, just join");

		add (
			ChatKeywordJoinType.chatSetInfo,
			"chat, set my info");

		add (
			ChatKeywordJoinType.chatNext,
			"chat, next user");

		add (
			ChatKeywordJoinType.chatLocation,
			"chat, set location");

		add (
			ChatKeywordJoinType.chatDob,
			"chat, set my date of birth");

		add (
			ChatKeywordJoinType.chatPics,
			"chat, next user photo(s)");

		add (
			ChatKeywordJoinType.dateSimple,
			"date, just join");

		add (
			ChatKeywordJoinType.dateSetInfo,
			"date, set my info");

		add (
			ChatKeywordJoinType.dateLocation,
			"date, set location");

		add (
			ChatKeywordJoinType.dateDob,
			"date, set my date of birth");

	}

}