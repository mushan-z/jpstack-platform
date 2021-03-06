package wbs.platform.object.create;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
public
class ObjectCreatePartFactory <RecordType extends Record <RecordType>>
	implements PagePartFactory {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ObjectCreatePart <RecordType, ?>>
		objectCreatePartProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <RecordType> consoleHelper;

	@Getter @Setter
	ConsoleFormType <RecordType> formType;

	@Getter @Setter
	String localFile;

	// implementation

	@Override
	public
	PagePart buildPagePart (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"buildPagePart");

		) {

			return objectCreatePartProvider.provide (
				transaction,
				objectCreatePart ->
					objectCreatePart

				.consoleHelper (
					consoleHelper)

				.formType (
					formType)

				.localFile (
					localFile)

			);

		}

	}

}
