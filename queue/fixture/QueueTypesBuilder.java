package wbs.platform.queue.fixture;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.TransactionBuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.logging.LogContext;

import wbs.platform.queue.metamodel.QueueTypesSpec;

@PrototypeComponent ("queueTypesBuilder")
@ModelMetaBuilderHandler
public
class QueueTypesBuilder
	implements TransactionBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	QueueTypesSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull Transaction parentTransaction,
			@NonNull Builder <Transaction> builder) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"build");

		) {

			builder.descend (
				transaction,
				parent,
				spec.queueTypes (),
				model,
				MissingBuilderBehaviour.error);

		}

	}

}
