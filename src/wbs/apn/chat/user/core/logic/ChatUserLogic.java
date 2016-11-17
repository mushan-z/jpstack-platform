package wbs.apn.chat.user.core.logic;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import wbs.apn.chat.bill.model.ChatUserCreditMode;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.apn.chat.affiliate.model.ChatAffiliateRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageRec;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;

public
interface ChatUserLogic {

	AffiliateRec getAffiliate (
			ChatUserRec chatUser);

	Long getAffiliateId (
			ChatUserRec chatUser);

	void logoff (
			ChatUserRec chatUser,
			Boolean automatic);

	boolean deleted (
			ChatUserRec chatUser);

	void scheduleAd (
			ChatUserRec chatUser);

	boolean compatible (
			Gender thisGender,
			Orient thisOrient,
			Optional <Long> thisCategoryId,
			Gender thatGender,
			Orient thatOrient,
			Optional <Long> thatCategoryId);

	boolean compatible (
			ChatUserRec thisUser,
			ChatUserRec thatUser);

	Collection <ChatUserRec> getNearestUsers (
			ChatUserRec thisUser,
			Collection <ChatUserRec> thoseUsers,
			Long numToFind);

	List<UserDistance> getUserDistances (
			ChatUserRec thisUser,
			Collection<ChatUserRec> otherUsers);

	@Accessors (fluent = true)
	@Data
	@EqualsAndHashCode
	public static
	class UserDistance
		implements Comparable<UserDistance> {

		ChatUserRec user;
		double miles;

		@Override
		public
		int compareTo (
				UserDistance other) {

			return new CompareToBuilder ()

				.append (
					miles,
					other.miles)

				.append (
					user,
					other.user)

				.toComparison ();

		}

	}

	void adultVerify (
			ChatUserRec chatUser);

	ChatUserRec createChatMonitor (
			ChatRec chat);

	void creditModeChange (
			ChatUserRec chatUser,
			ChatUserCreditMode newMode);

	ChatUserImageRec setImage (
			ChatUserRec chatUser,
			ChatUserImageType type,
			MediaRec smallMedia,
			MediaRec fullMedia,
			Optional<MessageRec> message,
			boolean append);

	ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			MediaRec fullMedia,
			Optional<MessageRec> message,
			boolean append);

	ChatUserImageRec setPhoto (
			ChatUserRec chatUser,
			byte[] data,
			Optional <String> filename,
			Optional <String> mimeType,
			Optional <MessageRec> message,
			Boolean append);

	Optional <ChatUserImageRec> setPhotoFromMessage (
			ChatUserRec chatUser,
			MessageRec message,
			Boolean append);

	void setVideo (
			ChatUserRec chatUser,
			MediaRec fullMedia,
			MessageRec message,
			Boolean append);

	void setVideo (
			ChatUserRec chatUser,
			byte[] data,
			Optional<String> filename,
			Optional<String> mimeType,
			MessageRec message,
			Boolean append);

	void setAudio (
			ChatUserRec chatUser,
			byte[] data,
			MessageRec message,
			Boolean append);

	void setImage (
			ChatUserRec chatUser,
			ChatUserImageType type,
			byte[] data,
			String filename,
			String mimeType,
			Optional<MessageRec> message,
			Boolean append);

	boolean setVideo (
			ChatUserRec chatUser,
			MessageRec message,
			Boolean append);

	boolean setPlace (
			ChatUserRec chatUser,
			String place,
			Optional<MessageRec> message,
			Optional<UserRec> user);

	boolean gotDob (
			ChatUserRec chatUser);

	boolean dobOk (
			ChatUserRec chatUser);

	void setScheme (
			ChatUserRec chatUser,
			ChatSchemeRec chatScheme);

	void setAffiliate (
			ChatUserRec chatUser,
			ChatAffiliateRec chatAffiliate,
			Optional <MessageRec> message);

	Optional <MediaRec> findPhoto (
			MessageRec message);

	boolean valid (
			ChatUserRec chatUser);

	ChatUserImageRec chatUserPendingImage (
			ChatUserRec chatUser,
			ChatUserImageType type);

	@Accessors (fluent = true)
	public
	enum PendingMode {

		none,
		name ("reject_name", null, null),
		info ("reject_info", null, null),
		image ("reject_image", "set_photo", "chatUserImageList"),
		video ("reject_image", "video_set", "chatUserVideoList"),
		audio ("reject_image", "audio_set", "chatUserAudioList");

		@Getter String rejectType;
		@Getter String commandCode;
		@Getter String listProperty;

		PendingMode () {

			this (
				null,
				null,
				null);

		}

		PendingMode (
				String rejectType) {

			this (
				rejectType,
				null,
				null);

		}

		PendingMode (
				String rejectType,
				String commandCode,
				String listProperty) {

			this.rejectType = rejectType;
			this.commandCode = commandCode;
			this.listProperty = listProperty;

		}

	}

	ChatUserImageType imageTypeForMode (
			PendingMode mode);

	String getBrandName (
			ChatUserRec chatUser);

	DateTimeZone getTimezone (
			ChatUserRec chatUser);

	List<ChatUserImageRec> getChatUserImageListByType (
			ChatUserRec chatUser,
			ChatUserImageType type);

	ChatUserImageRec getMainChatUserImageByType (
			ChatUserRec chatUser,
			ChatUserImageType type);

	void setMainChatUserImageByType (
			ChatUserRec chatUser,
			ChatUserImageType type,
			Optional<ChatUserImageRec> chatUserImage);

	String getPrettyName (
			ChatUserRec chatUser);

	boolean likes (
			ChatUserRec chatUser,
			Gender otherGender);

	long getAgeInYears (
			ChatUserRec chatUser,
			Instant now);

}