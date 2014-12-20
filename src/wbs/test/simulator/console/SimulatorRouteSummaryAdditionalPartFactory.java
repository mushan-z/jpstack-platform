package wbs.test.simulator.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.part.PagePart;
import wbs.sms.route.core.console.RouteSummaryAdditionalPartFactory;

@SingletonComponent ("simulatorRouteSummaryAdditionalPartFactory")
public
class SimulatorRouteSummaryAdditionalPartFactory
	implements RouteSummaryAdditionalPartFactory {

	@Inject
	Provider<SimulatorRouteSummaryAdditionalPart>
		simulatorRouteSummaryAdditionalPart;

	@Override
	public
	String[] getSenderCodes () {

		return new String [] {
			"simulator"
		};

	}

	@Override
	public
	PagePart getPagePart (
			String senderCode) {

		return simulatorRouteSummaryAdditionalPart.get ();

	}

}