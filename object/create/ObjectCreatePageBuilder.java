package wbs.platform.object.create;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.CodeFormFieldSpec;
import wbs.console.forms.DescriptionFormFieldSpec;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.NameFormFieldSpec;
import wbs.console.forms.ParentFormFieldSpec;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;
import wbs.services.ticket.core.console.FieldsProvider;

@PrototypeComponent ("objectCreatePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectCreatePageBuilder {

	// dependences

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ObjectCreateAction> objectCreateAction;

	@Inject
	Provider<ObjectCreatePart> objectCreatePart;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectCreatePageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String typeCode;
	String tabName;
	String localFile;
	String responderName;
	String targetContextTypeName;
	String targetResponderName;
	FieldsProvider fieldsProvider;
	FormFieldSet formFieldSet;
	String createTimeFieldName;
	String createUserFieldName;
	String createPrivDelegate;
	String createPrivCode;
	String privKey;

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildTab (
				resolvedExtensionPoint);

			buildFile (
				resolvedExtensionPoint);

		}

		buildResponder ();

	}

	void buildTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (

			"end",

			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					"Create")

				.localFile (
					localFile)

				/*.privKeys (
				 * 	Collections.singletonList (privKey))*/,

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Action createAction =
			new Action () {

			@Override
			public
			Responder handle () {

				return objectCreateAction.get ()

					.consoleHelper (
						consoleHelper)

					.typeCode (
						typeCode)

					.responderName (
						responderName)

					.targetContextTypeName (
						targetContextTypeName)

					.targetResponderName (
						targetResponderName)

					.createPrivDelegate (
						createPrivDelegate)

					.createPrivCode (
						createPrivCode)

					.formFieldSet (
						formFieldSet)

					.formFieldsProvider (
						fieldsProvider)

					.createTimeFieldName (
						createTimeFieldName)

					.createUserFieldName (
						createUserFieldName)

					.handle ();

			}

		};

		consoleModule.addContextFile (

			localFile,

			consoleFile.get ()

				.getResponderName (
					responderName)

				.postAction (
					createAction)

				/*.privKeys (
					Collections.singletonList (privKey)*/,

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectCreatePart.get ()

					.consoleHelper (
						consoleHelper)

					.formFieldSet (
						formFieldSet)

					.formFieldsProvider (
						fieldsProvider)

					.parentPrivCode (
						createPrivCode)

					.localFile (
						localFile);

			}

		};

		consoleModule.addResponder (

			responderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						consoleHelper.friendlyName () + " create"))

				.pagePartFactory (
					partFactory));

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		typeCode =
			spec.typeCode ();

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.create",
					container.pathPrefix ()));

		localFile =
			ifNull (
				spec.localFile (),
				stringFormat (
					"%s.create",
					container.pathPrefix ()));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%sCreateResponder",
					container.newBeanNamePrefix ()));

		targetContextTypeName =
			ifNull (
				spec.targetContextTypeName (),
				consoleHelper.objectName () + "+");

		targetResponderName =
			ifNull (
				spec.targetResponderName (),
				stringFormat (
					"%sSettingsResponder",
					consoleHelper.objectName ()));

		createPrivDelegate =
			spec.createPrivDelegate ();

		createPrivCode =
			ifNull (
				spec.createPrivCode (),
				stringFormat (
					"%s.create",
					consoleHelper.objectTypeCode ()));

		formFieldSet =
			spec.fieldsName () != null
				? consoleModule.formFieldSets ().get (
					spec.fieldsName ())
				: defaultFields ();

		// if a provider name is provided

		if (spec.fieldsProviderName () != null) {

			fieldsProvider =
					applicationContext.getBean (
							spec.fieldsProviderName (),
							FieldsProvider.class);
		}

		else {

			fieldsProvider =
				null;

		}

		createTimeFieldName =
			spec.createTimeFieldName ();

		createUserFieldName =
			spec.createUserFieldName ();

		privKey =
			spec.privKey ();

	}

	FormFieldSet defaultFields () {

		// parent

		List<Object> formFieldSpecs =
			new ArrayList<Object> ();

		if (consoleHelper.canGetParent ()) {

			formFieldSpecs.add (
				new ParentFormFieldSpec ()

				.createPrivDelegate (
					createPrivDelegate)

				.createPrivCode (
					createPrivCode));

		}

		if (consoleHelper.codeExists ()
				&& ! consoleHelper.nameExists ()) {

			formFieldSpecs.add (
				new CodeFormFieldSpec ());

		}

		if (consoleHelper.nameExists ()) {

			formFieldSpecs.add (
				new NameFormFieldSpec ());

		}

		if (consoleHelper.descriptionExists ()) {

			formFieldSpecs.add (
				new DescriptionFormFieldSpec ());

		}

		// build

		String fieldSetName =
			stringFormat (
				"%s.create",
				consoleHelper.objectName ());

		return consoleModuleBuilder.buildFormFieldSet (
			consoleHelper,
			fieldSetName,
			formFieldSpecs);

	}

}
