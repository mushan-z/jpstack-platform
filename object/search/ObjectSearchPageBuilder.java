package wbs.platform.object.search;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;

@PrototypeComponent ("objectSearchPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSearchPageBuilder <
	ObjectType extends Record <ObjectType>,
	SearchType extends Serializable,
	ResultType extends IdObject
> implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	@PrototypeDependency
	Provider <ObjectSearchGetAction> objectSearchGetAction;

	@PrototypeDependency
	Provider <ObjectSearchPart <ObjectType, SearchType>> objectSearchPart;

	@PrototypeDependency
	Provider <
		ObjectSearchPostAction <
			ObjectType,
			SearchType,
			ResultType
		>
	> objectSearchPostAction;

	@PrototypeDependency
	Provider <ObjectSearchResultsPart <ObjectType, ResultType>>
		objectSearchResultsPartProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectSearchPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	Class <SearchType> searchClass;
	Class <ResultType> resultClass;

	FormFieldSet <SearchType> searchFormFieldSet;

	Map <String, ObjectSearchResultsMode <ResultType>> resultsModes;

	String name;
	String sessionKey;
	String privKey;
	String parentIdKey;
	String parentIdName;
	String tabName;
	String tabLabel;
	String fileName;
	String searchResponderName;
	String searchResultsResponderName;
	Long itemsPerPage;

	Provider <Action> searchGetActionProvider;
	Provider <Action> searchPostActionProvider;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildConsoleModule");

		) {

			setDefaults ();

			buildGetAction ();
			buildPostAction ();

			buildSearchResponder ();
			buildResultsResponder ();

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTab (
					resolvedExtensionPoint);

				buildContextFile (
					resolvedExtensionPoint);

			}

		}

	}

	void buildContextTab (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			"end",

			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					tabLabel)

				.localFile (
					fileName)

				.privKeys (
					privKey != null
						? Collections.singletonList (privKey)
						: Collections.<String>emptyList ()),

			extensionPoint.contextTypeNames ());

	}

	void buildGetAction () {

		searchGetActionProvider =
			() -> objectSearchGetAction.get ()

			.searchResponderName (
				searchResponderName)

			.searchResultsResponderName (
				searchResultsResponderName)

			.sessionKey (
				sessionKey);

	}

	void buildPostAction () {

		searchPostActionProvider =
			() -> objectSearchPostAction.get ()

			.consoleHelper (
				consoleHelper)

			.searchClass (
				searchClass)

			.searchDaoMethodName (
				spec.searchDaoMethodName ())

			.resultsDaoMethodName (
				spec.resultsDaoMethodName ())

			.sessionKey (
				sessionKey)

			.parentIdKey (
				parentIdKey)

			.parentIdName (
				parentIdName)

			.searchFormFieldSet (
				searchFormFieldSet)

			.resultsModes (
				resultsModes)

			.searchResponderName (
				searchResponderName)

			.fileName (
				fileName);

	}

	void buildContextFile (
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getActionProvider (
					searchGetActionProvider)

				.postActionProvider (
					searchPostActionProvider)

				.privKeys (
					privKey != null
						? Collections.singletonList (privKey)
						: Collections.<String>emptyList ()),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildSearchResponder () {

		PagePartFactory searchPartFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildPagePart");

			) {

				return objectSearchPart.get ()

					.consoleHelper (
						consoleHelper)

					.searchClass (
						searchClass)

					.sessionKey (
						sessionKey)

					.fields (
						searchFormFieldSet)

					.fileName (
						fileName);

			}

		};

		consoleModule.addResponder (

			searchResponderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						stringFormat (
							"%s search",
							consoleHelper.friendlyName ())))

				.pagePartFactory (
					searchPartFactory)

		);

	}

	void buildResultsResponder () {

		PagePartFactory searchResultsPartFactory =
			parentTransaction -> {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"buildPagePart");

			) {

				return objectSearchResultsPartProvider.get ()

					.consoleHelper (
						consoleHelper)

					.sessionKey (
						sessionKey)

					.resultsModes (
						resultsModes)

					.resultsClass (
						resultClass)

					.resultsDaoMethodName (
						spec.resultsDaoMethodName ())

					.itemsPerPage (
						itemsPerPage)

					.targetContextTypeName (
						consoleHelper.objectName () + ":combo");

			}

		};

		consoleModule.addResponder (

			searchResultsResponderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						stringFormat (
							"%s search results",
							consoleHelper.friendlyName ())))

				.pagePartFactory (
					searchResultsPartFactory));

	}

	void setDefaults () {

		if (
			isNotNull (
				spec.objectTypeName ())
		) {

			consoleHelper =
				genericCastUnchecked (
					objectManager.findConsoleHelperRequired (
						spec.objectTypeName ()));

		} else {

			consoleHelper =
				container.consoleHelper ();

		}

		String searchClassName =
			ifNull (
				spec.searchClassName (),
				stringFormat (
					"%s.%sSearch",
					consoleHelper
						.objectClass ()
						.getPackage ()
						.getName (),
					capitalise (
						consoleHelper.objectName ())));

		Optional <Class <?>> searchClassOptional =
			classForName (
				searchClassName);

		if (
			optionalIsNotPresent (
				searchClassOptional)
		) {

			throw new RuntimeException (
				stringFormat (
					"Search class not found: %s",
					searchClassName));

		}

		searchClass =
			genericCastUnchecked (
				searchClassOptional.get ());

		resultClass =
			genericCastUnchecked (
				ifNotNullThenElse (
					spec.resultsClassName (),
					() -> classForNameRequired (
						spec.resultsClassName ()),
					() -> consoleHelper.objectClass ()));

		searchFormFieldSet =
			consoleModule.formFieldSetRequired (
				spec.searchFieldsName (),
				searchClass);

		boolean haveResultsColumnsFieldsName =
			isNotNull (
				spec.resultsFieldsName ());

		boolean haveResultsRowsFieldName =
			isNotNull (
				spec.resultsRowsFieldsName ());

		boolean haveResultsFieldsNames = (
			haveResultsColumnsFieldsName
			|| haveResultsRowsFieldName
		);

		boolean haveResultsModes =
			collectionIsNotEmpty (
				spec.resultsModes ());

		if (haveResultsFieldsNames && haveResultsModes) {
			throw new RuntimeException ();
		}

		if (haveResultsFieldsNames) {

			resultsModes =
				ImmutableMap.<String, ObjectSearchResultsMode <ResultType>> of (
					"normal",
					new ObjectSearchResultsMode <ResultType> ()

				.name (
					"normal")

				.columns (
					formFieldSet (
						optionalFromNullable (
							spec.resultsFieldsName ())))

				.rows (
					formFieldSet (
						optionalFromNullable (
							spec.resultsRowsFieldsName ())))

			);

		} else {

			resultsModes =
				mapWithDerivedKey (
					iterableMap (
						resultsModeSpec ->
							new ObjectSearchResultsMode <ResultType> ()

						.name (
							resultsModeSpec.name ())

						.columns (
							formFieldSet (
								optionalFromNullable (
									ifNull (
										resultsModeSpec.columnsFieldsName (),
										stringFormat (
											"results-%s-columns",
											resultsModeSpec.name ())))))

						.rows (
							formFieldSet (
								optionalFromNullable (
									ifNull (
										resultsModeSpec.rowsFieldsName (),
										stringFormat (
											"results-%s-rows",
											resultsModeSpec.name ()))))),

					spec.resultsModes ()),
				ObjectSearchResultsMode::name);

		}

		name =
			ifNull (
				spec.name (),
				"search");

		privKey =
			spec.privKey ();

		parentIdKey =
			spec.parentIdKey ();

		parentIdName =
			spec.parentIdName ();

		sessionKey =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				name);

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		tabLabel =
			ifNull (
				spec.tabLabel (),
				"Search");

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		searchResponderName =
			ifNull (
				spec.searchResponderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (
						name)));

		searchResultsResponderName =
			ifNull (
				spec.searchResultsResponderName (),
				stringFormat (
					"%s%sResultsResponder",
					container.newBeanNamePrefix (),
					capitalise (
						name)));

		itemsPerPage =
			100l;

	}

	private
	Optional <FormFieldSet <ResultType>> formFieldSet (
			@NonNull Optional <String> formFieldSetNameOptional) {

		return optionalMapOptional (
			formFieldSetNameOptional,
			formFieldSetName ->
				consoleModule.formFieldSet (
					formFieldSetName,
					resultClass));

	}

}
