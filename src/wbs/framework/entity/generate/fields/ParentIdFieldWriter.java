package wbs.framework.entity.generate.fields;

import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.identities.ParentIdFieldSpec;

@PrototypeComponent ("parentIdFieldWriter")
@ModelWriter
public
class ParentIdFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	ParentIdFieldSpec spec;

	@BuilderTarget
	ModelFieldWriterTarget target;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		// write field

		new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s.model.%s",
				context.modelMeta ().plugin ().packageName (),
				context.recordClassName ())

			.typeClass (
				Long.class)

			.propertyName (
				"parentId")

			.writeBlock (
				target.imports (),
				target.formatWriter ());

	}

}