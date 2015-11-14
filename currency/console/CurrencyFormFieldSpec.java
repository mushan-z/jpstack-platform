package wbs.platform.currency.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("currency-field")
@PrototypeComponent ("currencyFormFieldSpec")
@ConsoleModuleData
public
class CurrencyFormFieldSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean readOnly;

	@DataAttribute (
		name = "min")
	Long minimum =
		Long.MIN_VALUE;

	@DataAttribute (
		name = "max")
	Long maximum =
		Long.MAX_VALUE;

	@DataAttribute
	Integer size;

	@DataAttribute (
		name = "currency",
		required = true)
	String currencyPath;

	@DataAttribute
	Boolean blankIfZero;

}
