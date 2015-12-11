package wbs.framework.entity.build;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.classForNameRequired;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.ComponentFieldSpec;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;

@PrototypeComponent ("componentModelFieldBuilder")
@ModelBuilder
public
class ComponentModelFieldBuilder {

	// dependencies

	@Inject
	ModelBuilderManager modelBuilderManager;

	@Inject
	ModelMetaLoader modelMetaLoader;

	// builder

	@BuilderParent
	ModelFieldBuilderContext context;

	@BuilderSource
	ComponentFieldSpec spec;

	@BuilderTarget
	ModelFieldBuilderTarget target;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String fieldName =
			ifNull (
				spec.name (),
				spec.typeName ());

		String fieldTypeName =
			capitalise (
				spec.typeName ());

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				fieldTypeName);

		Class<?> fieldTypeClass =
			classForNameRequired (
				fullFieldTypeName);

		// create model field

		ModelField modelField =
			new ModelField ()

			.model (
				target.model ())

			.parentField (
				context.parentModelField ())

			.name (
				fieldName)

			.label (
				camelToSpaces (
					fieldName))

			.type (
				ModelFieldType.component)

			.parent (
				false)

			.identity (
				false)

			.valueType (
				fieldTypeClass)

			.nullable (
				false);

		// contained model field

		ModelFieldBuilderContext nextContext =
			new ModelFieldBuilderContext ()

			.modelMeta (
				context.modelMeta ())

			.recordClass (
				context.recordClass ())

			.parentModelField (
				modelField);

		ModelFieldBuilderTarget nextTarget =
			new ModelFieldBuilderTarget ()

			.model (
				target.model ())

			.fields (
				modelField.fields ())

			.fieldsByName (
				modelField.fieldsByName ());

		ModelMetaSpec componentMeta =
			modelMetaLoader.componentMetas ().get (
				spec.typeName ());

		if (
			isNull (
				componentMeta)
		) {

			throw new RuntimeException ();

		}

		modelBuilderManager.build (
			nextContext,
			componentMeta.fields (),
			nextTarget);

		// store field

		target.fields ().add (
			modelField);

		target.fieldsByName ().put (
			modelField.name (),
			modelField);

	}

}