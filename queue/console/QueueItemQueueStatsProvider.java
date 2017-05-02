package wbs.platform.queue.console;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.reporting.StatsDataSet;
import wbs.console.reporting.StatsDatum;
import wbs.console.reporting.StatsGranularity;
import wbs.console.reporting.StatsPeriod;
import wbs.console.reporting.StatsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;

@SingletonComponent ("queueItemQueueStatsProvider")
public
class QueueItemQueueStatsProvider
	implements StatsProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	QueueItemObjectHelper queueItemHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <QueueStatsFilter> queueStatsFilterProvider;

	// implementation

	@Override
	public
	StatsDataSet getStats (
			@NonNull Transaction parentTransaction,
			@NonNull StatsPeriod statsPeriod,
			@NonNull Map <String, Object> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getStats");

		) {

			if (statsPeriod.granularity () != StatsGranularity.hour)
				throw new IllegalArgumentException ();

			// setup data structures

			Map <Long, long[]> numProcessedPerQueue =
				new TreeMap<> ();

			Map <Long, long[]> numCreatedPerQueue =
				new TreeMap<> ();

			Map <Long, long[]> numPreferredPerQueue =
				new TreeMap<> ();

			Map <Long, long[]> numNotPreferredPerQueue =
				new TreeMap<> ();

			Set <Object> queueIdObjects =
				new HashSet<> ();

			// retrieve queue items

			QueueStatsFilter queueStatsFilter =
				queueStatsFilterProvider.get ();

			queueStatsFilter.conditions (
				transaction,
				conditions);

			List <QueueItemRec> createdQueueItems =
				queueStatsFilter.filterQueueItems (
					transaction,
					queueItemHelper.findByCreatedTime (
						transaction,
						statsPeriod.toInterval ()));

			List <QueueItemRec> processedQueueItems =
				queueStatsFilter.filterQueueItems (
					transaction,
					queueItemHelper.findByProcessedTime (
						transaction,
						statsPeriod.toInterval ()));

			// aggregate created items

			for (
				QueueItemRec queueItem
					: createdQueueItems
			) {

				QueueSubjectRec queueSubject =
					queueItem.getQueueSubject ();

				// TODO fix data!
				QueueRec queue =
					queueSubject != null
						? queueSubject.getQueue ()
						: queueItem.getQueue ();

				Record <?> parent =
					objectManager.getParentRequired (
						transaction,
						queue);

				if (
					! privChecker.canRecursive (
						transaction,
						parent,
						"supervisor")
				) {
					continue;
				}

				int hour =
					statsPeriod.assign (
						queueItem.getCreatedTime ());

				if (! queueIdObjects.contains (
						queue.getId ())) {

					queueIdObjects.add (
						queue.getId ());

					numCreatedPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

					numProcessedPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

					numPreferredPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

					numNotPreferredPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

				}

				long[] numCreatedForQueue =
					numCreatedPerQueue.get (
						queue.getId ());

				numCreatedForQueue [hour] ++;

			}

			// aggregate processed items

			for (
				QueueItemRec queueItem
					: processedQueueItems
			) {

				QueueSubjectRec queueSubject =
					queueItem.getQueueSubject ();

				// TODO fix data!
				QueueRec queue =
					queueSubject != null
						? queueSubject.getQueue ()
						: queueItem.getQueue ();

				Record <?> parent =
					objectManager.getParentRequired (
						transaction,
						queue);

				if (
					! privChecker.canRecursive (
						transaction,
						parent,
						"supervisor")
				) {
					continue;
				}

				int hour =
					statsPeriod.assign (
						queueItem.getProcessedTime ());

				if (! queueIdObjects.contains (
						queue.getId ())) {

					queueIdObjects.add (
						queue.getId ());

					numCreatedPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

					numProcessedPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

					numPreferredPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

					numNotPreferredPerQueue.put (
						queue.getId (),
						new long [
							toJavaIntegerRequired (
								statsPeriod.size ())]);

				}

				long[] numProcessedForQueue =
					numProcessedPerQueue.get (
						queue.getId ());

				numProcessedForQueue [hour] ++;

				if (queueItem.getProcessedByPreferredUser () != null) {

					if (queueItem.getProcessedByPreferredUser ()) {

						long[] numPreferredForQueue =
							numPreferredPerQueue.get (
								queue.getId ());

						numPreferredForQueue [hour] ++;

					} else {

						long[] numNotPreferredForQueue =
							numNotPreferredPerQueue.get (
								queue.getId ());

						numNotPreferredForQueue [hour] ++;

					}

				}

			}

			// create return value

			StatsDataSet statsDataSet =
				new StatsDataSet ();

			statsDataSet.indexValues ().put (
				"queueId",
				queueIdObjects);

			for (
				int hour = 0;
				hour < statsPeriod.size ();
				hour ++
			) {

				for (
					Object queueIdObject
						: queueIdObjects
				) {

					Long queueId =
						(Long)
						queueIdObject;

					statsDataSet.data ().add (
						new StatsDatum ()

						.startTime (
							statsPeriod.step (hour))

						.addIndex (
							"queueId",
							queueId)

						.addValue (
							"numCreated",
							numCreatedPerQueue.get (queueId) [hour])

						.addValue (
							"numProcessed",
							numProcessedPerQueue.get (queueId) [hour])

						.addValue (
							"numPreferred",
							numPreferredPerQueue.get (queueId) [hour])

						.addValue (
							"numNotPreferred",
							numNotPreferredPerQueue.get (queueId) [hour]));

				}

			}

			return statsDataSet;

		}

	}

}
