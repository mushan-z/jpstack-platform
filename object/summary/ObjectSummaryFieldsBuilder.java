package wbs.platform.object.summary;

import static wbs.utils.etc.Misc.todo;

import lombok.NonNull;

import wbs.console.module.ConsoleModuleBuilderComponent;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("objectSummaryFieldsBuilder")
public
class ObjectSummaryFieldsBuilder <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	@BuilderSource
	ObjectSummaryFieldsSpec objectSummaryFieldsSpec;

	@BuilderTarget
	ObjectSummaryPageBuilder <ObjectType, ParentType> objectSummaryPageBuilder;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			throw todo ();

			/*
			objectSummaryPageBuilder.addFieldsPart (
				taskLogger,
				objectSummaryFieldsSpec.formTypeName ());
			*/

		}

	}

}
