package wbs.apn.chat.help.console;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatHelpQueueConsolePlugin")
public
class ChatHelpQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatHelpLogPendingFormResponder")
	ComponentProvider <WebResponder> pendingFormResponderProvider;

	// details

	{
		queueTypeCode ("chat", "help");
	}

	@Override
	public
	WebResponder makeResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeResponder");

		) {

			ConsoleContext targetContext =
				consoleManager.context (
					"chatHelpLog.pending",
					true);

			consoleManager.changeContext (
				taskLogger,
				targetContext,
				"/" + queueItem.getRefObjectId ());

			return pendingFormResponderProvider.provide (
				taskLogger);

		}

	}

}