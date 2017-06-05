package wbs.framework.fixtures;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

@SingletonComponent ("threadManager")
public
class FixturesThreadManager
	implements ComponentFactory <ThreadManager> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized components

	@UninitializedDependency
	Provider <ThreadManagerImplementation> threadManagerImplemetationProvider;

	// implementation

	@Override
	public
	ThreadManager makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return threadManagerImplemetationProvider.get ();

		}

	}

}