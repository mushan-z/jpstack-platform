package wbs.platform.object.summary;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.console.module.ConsoleModuleSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("fields")
@PrototypeComponent ("objectSummaryFieldsSpec")
public
class ObjectSummaryFieldsSpec
	implements ConsoleModuleData {

	// tree attributes

	@DataAncestor
	ConsoleModuleSpec consoleModule;

	@DataParent
	ObjectSummaryPageSpec objectSummaryPageSpec;

	// attributes

	@DataAttribute (
		name = "fields")
	String fieldsName;

}
