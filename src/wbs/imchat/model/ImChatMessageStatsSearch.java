package wbs.imchat.model;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.TextualInterval;

@Accessors (fluent = true)
@Data
public
class ImChatMessageStatsSearch
	implements Serializable {

	Set <Long> imChatIds;
	Set <Long> senderUserIds;

	TextualInterval timestamp;

	Set <Long> filterImChatIds;
	Set <Long> filterSenderUserIds;

}