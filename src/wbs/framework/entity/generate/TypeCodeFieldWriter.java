package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.ifNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.meta.PropertyWriter;
import wbs.framework.entity.meta.TypeCodeFieldSpec;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("typeCodeFieldWriter")
@ModelWriter
public
class TypeCodeFieldWriter {

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	TypeCodeFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		javaWriter.writeFormat (
			"\t@TypeCodeField\n");

		// write field

		PropertyWriter propertyWriter =
			new PropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"String")

			.propertyNameFormat (
				"%s",
				ifNull (
					spec.name (), "type"));

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
