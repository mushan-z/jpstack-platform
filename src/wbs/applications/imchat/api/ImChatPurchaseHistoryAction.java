package wbs.applications.imchat.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.applications.imchat.model.ImChatCustomerObjectHelper;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatPurchaseObjectHelper;
import wbs.applications.imchat.model.ImChatPurchaseRec;
import wbs.applications.imchat.model.ImChatSessionObjectHelper;
import wbs.applications.imchat.model.ImChatSessionRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Action;
import wbs.framework.web.JsonResponder;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.platform.console.misc.TimeFormatter;

import com.google.common.collect.Lists;

@PrototypeComponent ("imChatPurchaseHistoryAction")
public
class ImChatPurchaseHistoryAction
	implements Action {

	// dependencies

	@Inject
	Database database;

	@Inject
	ImChatApiLogic imChatApiLogic;

	@Inject
	ImChatCustomerObjectHelper imChatCustomerHelper;

	@Inject
	ImChatPurchaseObjectHelper imChatPurchaseHelper;

	@Inject
	ImChatSessionObjectHelper imChatSessionHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	// prototype dependencies

	@Inject
	Provider<JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	@SneakyThrows (IOException.class)
	public
	Responder handle () {

		DataFromJson dataFromJson =
			new DataFromJson ();

		// decode request

		JSONObject jsonValue =
			(JSONObject)
			JSONValue.parse (
				requestContext.reader ());

		ImChatPurchaseHistoryRequest purchaseHistoryRequest =
			dataFromJson.fromJson (
					ImChatPurchaseHistoryRequest.class,
				jsonValue);

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// lookup session and customer

		ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
						purchaseHistoryRequest.sessionSecret ());

		if (
			session == null
			|| ! session.getActive ()
		) {

			ImChatFailure failureResponse =
				new ImChatFailure ()

				.reason (
					"session-invalid")

				.message (
					"The session secret is invalid or the session is no " +
					"longer active");

			return jsonResponderProvider.get ()
				.value (failureResponse);

		}

		ImChatCustomerRec customer =
				session.getImChatCustomer();


		// retrieve purchases

		List<ImChatPurchaseRec> purchases =
			new ArrayList<ImChatPurchaseRec> (
				customer.getImChatPurchases ());

		Lists.reverse (
			purchases);

		// create response

		ImChatPurchaseHistorySuccess purchaseHistoryResponse =
			new ImChatPurchaseHistorySuccess ()

			.customer (
				imChatApiLogic.customerData (
					customer));

		for (
			ImChatPurchaseRec purchase
				: purchases
		) {

		purchaseHistoryResponse.purchases.add (
			new ImChatPurchaseHistoryData ()

			.price (
				purchase.getPrice ())

			.value (
				purchase.getValue ())

			.timestamp (
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					purchase.getCreatedTime ()))

		);

	}

	return jsonResponderProvider.get ()
		.value (purchaseHistoryResponse);

}

}