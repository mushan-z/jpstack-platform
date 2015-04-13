package wbs.platform.object.summary;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.forms.FormFieldLogic;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.ticket.console.FieldsProvider;

@Accessors (fluent = true)
@PrototypeComponent ("objectSummaryFieldsPart")
public
class ObjectSummaryFieldsPart
	extends AbstractPagePart {

	// dependencies
	
	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	FormFieldLogic formFieldLogic;	
	
	@Inject
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	FormFieldSet formFieldSet;
	
	@Getter @Setter
	FieldsProvider formFieldsProvider;

	// state

	Record<?> object;
	Record<?> parent;

	// implementation

	@Override
	public
	void prepare () {

		object =
			(Record<?>)
			consoleHelper.lookupObject (
				requestContext.contextStuff ());
		
		if (formFieldsProvider != null) {		
			prepareParent();
			prepareFieldSet();
		}

	}
	
	void prepareParent () {
		
		ConsoleHelper<?> parentHelper =
			objectManager.getConsoleObjectHelper (
				consoleHelper.parentClass ());
			
		if (parentHelper.root ()) {

			parent =
				rootHelper.find (0);

			return;

		}

		Integer parentId =
			requestContext.stuffInt (
				parentHelper.idKey ());

		if (parentId != null) {

			// use specific parent

			parent =
				parentHelper.find (
					parentId);

			return;

		}
		
	}
	
	void prepareFieldSet() {
		
		formFieldSet = formFieldsProvider.getFields(
				parent);
		
	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputTableRows (
			out,
			formFieldSet,
			object);

		printFormat (
			"</table>\n");

	}

}
