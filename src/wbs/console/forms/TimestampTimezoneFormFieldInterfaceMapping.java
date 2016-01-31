package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.misc.TimeFormatter;
import wbs.framework.application.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("timestampTimezoneFormFieldInterfaceMapping")
public
class TimestampTimezoneFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,DateTime,String> {

	// dependencies

	@Inject
	TimeFormatter timeFormatter;

	// properties

	@Getter @Setter
	String name;

	// implementation

	@Override
	public
	Either<Optional<DateTime>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		if (

			isNotPresent (
				interfaceValue)

			|| isEmpty (
				optionalRequired (
					interfaceValue))

		) {

			return successResult (
				Optional.<DateTime>absent ());

		}

		try {

			return successResult (
				Optional.of (
					timeFormatter.timestampTimezoneToDateTime (
						optionalRequired (
							interfaceValue))));

		} catch (IllegalArgumentException exception) {

			return errorResult (
				stringFormat (
					"Please enter a valid timestamp with timezone for %s",
					name ()));

		}

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<DateTime> genericValue) {

		if (! genericValue.isPresent ()) {

			return successResult (
				Optional.<String>absent ());

		}

		return successResult (
			Optional.of (
				timeFormatter.dateTimeToTimestampTimezoneString (
					genericValue.get ())));

	}

}
