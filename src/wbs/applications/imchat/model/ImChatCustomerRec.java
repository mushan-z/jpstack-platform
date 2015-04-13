package wbs.applications.imchat.model;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id" )
@MajorEntity
public
class ImChatCustomerRec
	implements CommonRecord<ImChatCustomerRec> {

	// identity

	@GeneratedIdField
	Integer id;

	@ParentField
	ImChatRec imChat;

	@CodeField
	String code;

	// details

	@SimpleField
	String email;

	@SimpleField
	String password;

	@CollectionField (
			orderBy = "index")
		Set<ImChatPurchaseRec> imChatPurchases =
			new TreeSet<ImChatPurchaseRec> ();

	// statistics

	@SimpleField
	Integer numConversations = 0;

	@SimpleField
	Integer numPurchases = 0;

	// state

	@SimpleField
	Integer balance = 0;

	// dao methods

	public
	interface ImChatCustomerDaoMethods {

		ImChatCustomerRec findByEmail (
				ImChatRec imChat,
				String email);

		List<Integer> searchIds (
				ImChatCustomerSearch imChatCustomerSearch);

	}

	// object hooks

	public static
	class ImChatCustomerHooks
		extends AbstractObjectHooks<ImChatCustomerRec> {

		// dependencies

		@Inject
		ImChatCustomerDao imChatCustomerDao;

		// implementation

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			ImChatCustomerSearch imChatCustomerSearch =
				(ImChatCustomerSearch) search;

			return imChatCustomerDao.searchIds (
				imChatCustomerSearch);

		}

	}

	// compare to

	@Override
	public
	int compareTo (
			Record<ImChatCustomerRec> otherRecord) {

		ImChatCustomerRec other =
			(ImChatCustomerRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getImChat (),
				other.getImChat ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
