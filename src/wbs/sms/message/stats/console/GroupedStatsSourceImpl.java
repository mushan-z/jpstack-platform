package wbs.sms.message.stats.console;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.framework.web.UrlParams;
import wbs.sms.message.stats.logic.MessageStatsLogic;
import wbs.sms.message.stats.model.MessageStatsData;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@PrototypeComponent ("groupedStatsSource")
public
class GroupedStatsSourceImpl
	implements GroupedStatsSource {

	// dependencies

	@Inject
	MessageStatsLogic messageStatsLogic;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	RouteConsoleHelper routeHelper;

	// properties

	@Getter @Setter
	SmsStatsCriteria groupCriteria;

	@Getter @Setter
	SmsStatsSource statsSource;

	@Getter @Setter
	Map<SmsStatsCriteria,Set<Integer>> critMap;

	@Getter @Setter
	Map<SmsStatsCriteria,Set<Integer>> filterMap;

	@Getter @Setter
	String url;

	@Getter @Setter
	UrlParams urlParams;

	// implementation

	@Override
	public
	Map<String,GroupStats> load (
			LocalDate start,
			LocalDate end) {

		Map<String,GroupStats> ret =
			new TreeMap<String,GroupStats> ();

		RouteRec route =
			statsSource.findRoute ();

		if (
			route == null
			&& critMap.containsKey (SmsStatsCriteria.route)
		) {

			Set<Integer> routeIds =
				critMap.get (SmsStatsCriteria.route);

			if (routeIds.size () == 1) {

				route =
					routeHelper.find (
						routeIds.iterator ().next ());

			}

		}

		for (
			MessageStatsRec messageStats
				: statsSource.findMessageStats (
					start,
					end,
					critMap,
					filterMap)
		) {

			String groupName =
				groupName (messageStats);

			if (groupCriteria == SmsStatsCriteria.route) {

				route =
					messageStats.getMessageStatsId ().getRoute ();

			}

			GroupStats groupStats =
				ret.get (groupName);

			if (groupStats == null) {

				groupStats =
					new GroupStats (
						route,
						groupUrl (messageStats));

				ret.put (
					groupName,
					groupStats);

			}

			Map<LocalDate,MessageStatsData> statsByDate =
				groupStats.getStatsByDate ();

			LocalDate date =
				messageStats.getMessageStatsId ().getDate ();

			MessageStatsData stats =
				statsByDate.get (date);

			if (stats == null) {

				stats =
					new MessageStatsData ();

				statsByDate.put (
					date,
					stats);

			}

			messageStatsLogic.addTo (
				stats,
				messageStats.getStats ());

		}

		return ret;

	}

	String groupName (
			MessageStatsRec mse) {

		if (groupCriteria == null)
			return "Total";

		switch (groupCriteria) {

			case route:

				return objectName (
					mse.getMessageStatsId ().getRoute ());

			case service:

				return objectName (
					mse.getMessageStatsId ().getService ());

			case affiliate:

				return objectName (
					mse.getMessageStatsId ().getAffiliate ());

			case batch:

				return mse.getMessageStatsId ().getBatch ().getId ().toString ();

			case network:

				return mse.getMessageStatsId ().getNetwork ().getDescription ();

		}

		throw new IllegalArgumentException ();

	}

	String groupUrl (
			MessageStatsRec mse) {

		if (groupCriteria == null)
			return null;

		UrlParams myUrlParams =
			new UrlParams (urlParams);

		myUrlParams.set (
			groupCriteria.toString (),
			groupId (mse));

		return myUrlParams.toUrl (url);

	}

	Integer groupId (
			MessageStatsRec mse) {

		if (groupCriteria == null)
			return null;

		switch (groupCriteria) {

			case route:

				return mse.getMessageStatsId ().getRoute ().getId ();

			case service:

				return mse.getMessageStatsId ().getService ().getId ();

			case affiliate:

				return mse.getMessageStatsId ().getAffiliate ().getId ();

			case batch:

				return mse.getMessageStatsId ().getBatch ().getId ();

			case network:

				return mse.getMessageStatsId ().getNetwork ().getId ();

		}

		throw new IllegalArgumentException ();

	}

	String objectName (
			Record<?> object) {

		return objectManager.objectPathMini (
			object);

	}

}
