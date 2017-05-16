package wbs.services.ticket.core.console;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.HiddenComponent;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("ticketSummaryFieldsProvider")
@HiddenComponent
public
class TicketSummaryFieldsProvider
	implements ComponentFactory <TicketFieldsProvider> {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized components

	@UninitializedDependency
	Provider <TicketFieldsProvider> ticketFieldsProviderProvider;

	// implementation

	@Override
	public
	TicketFieldsProvider makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return ticketFieldsProviderProvider.get ()

				.mode (
					"summary")

			;

		}

	}

}
