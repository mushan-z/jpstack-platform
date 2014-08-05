package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.part.AbstractPagePart;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchResultsPart")
public
class ObjectSearchResultsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	Integer itemsPerPage;

	// state

	Record<?> currentObject;
	List<Record<?>> objects;
	Integer totalObjects;
	Integer pageNumber;
	Integer pageCount;

	// implementation

	@Override
	public
	void prepare () {

		// current object

		Integer currentObjectId =
			(Integer)
			requestContext.stuff (
				consoleHelper.objectName () + "Id");

		if (currentObjectId != null) {

			currentObject =
				consoleHelper.find (
					currentObjectId);

		}

		// get search results for page

		@SuppressWarnings ("unchecked")
		List<Integer> allObjectIds =
			(List<Integer>)
			requestContext.session (
				sessionKey + "Results");

		totalObjects =
			allObjectIds.size ();

		pageNumber =
			Integer.parseInt (
				requestContext.parameter (
					"page",
					"0"));

		List<Integer> pageObjectIds =
			allObjectIds.subList (
				pageNumber * itemsPerPage,
				Math.min (
					(pageNumber + 1) * itemsPerPage,
					allObjectIds.size ()));

		pageCount =
			(allObjectIds.size () - 1) / itemsPerPage + 1;

		// load objects

		objects =
			new ArrayList<Record<?>> ();

		for (Integer objectId
				: pageObjectIds) {

			objects.add (
				consoleHelper.find (
					objectId));

		}

	}

	@Override
	public
	void goBodyStuff () {

		goNewSearch ();
		goTotalObjects ();
		goPageNumbers ();
		goSearchResults ();
		goPageNumbers ();

	}

	void goNewSearch () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"new-search\"",
			" value=\"new search\"",
			"></p>\n");

		printFormat (
			"</form>");

	}

	void goTotalObjects () {

		printFormat (
			"<p>Search returned %h items</p>\n",
			totalObjects);

	}

	void goPageNumbers () {

		if (pageCount == 1)
			return;

		printFormat (
			"<p",
			" class=\"links\"",
			">Select page\n");

		for (
			int page = 0;
			page < pageCount;
			page ++
		) {

			printFormat (
				"<a",
				" class=\"%h\"",
				page == pageNumber
					? "selected"
					: "",
				" href=\"%h\"",
				stringFormat (
					"?page=%h",
					page),
				">%s</a>\n",
				page + 1);

		}

		printFormat (
			"</p>\n");

	}

	void goSearchResults () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n");

		formFieldLogic.outputTableHeadings (
			out,
			formFieldSet);

		printFormat (
			"</tr>\n");

		for (Record<?> object
				: objects) {

			if (! consoleHelper.canView (object))
				continue;

			printFormat (
				"%s",
				Html.magicTr (
					requestContext.resolveContextUrl (
						consoleHelper.getDefaultContextPath (object)),
					object == currentObject));

			formFieldLogic.outputTableCells (
				out,
				formFieldSet,
				object,
				false);

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}