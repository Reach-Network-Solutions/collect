/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package app.nexusforms.android.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import app.nexusforms.analytics.Analytics;


import app.nexusforms.android.R;
import app.nexusforms.android.application.Collect;
import app.nexusforms.android.backgroundwork.FormSubmitManager;
import app.nexusforms.android.javarosawrapper.FormController;
import app.nexusforms.android.javarosawrapper.FormIndexUtils;
import app.nexusforms.android.nexus_view.QuestionsAdapter;
import app.nexusforms.android.permissions.PermissionsChecker;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.audio.AMRAppender;
import app.nexusforms.android.audio.AudioControllerView;
import app.nexusforms.android.audio.AudioHelper;
import app.nexusforms.android.audio.M4AAppender;
import app.nexusforms.android.dao.helpers.ContentResolverHelper;
import app.nexusforms.android.dao.helpers.InstancesDaoHelper;
import app.nexusforms.android.events.ReadPhoneStatePermissionRxEvent;
import app.nexusforms.android.events.RxEventBus;
import app.nexusforms.android.exception.JavaRosaException;
import app.nexusforms.android.external.ExternalAppsUtils;
import app.nexusforms.android.formentry.BackgroundAudioPermissionDialogFragment;
import app.nexusforms.android.formentry.BackgroundAudioViewModel;
import app.nexusforms.android.formentry.FormEndView;
import app.nexusforms.android.formentry.FormEntryMenuDelegate;
import app.nexusforms.android.formentry.FormEntryViewModel;
import app.nexusforms.android.formentry.FormIndexAnimationHandler;
import app.nexusforms.android.formentry.FormLoadingDialogFragment;
//import org.odk.collect.android.formentry.ODKView;

import app.nexusforms.android.formentry.QuitFormDialogFragment;
import app.nexusforms.android.formentry.RecordingHandler;
import app.nexusforms.android.formentry.RecordingWarningDialogFragment;
import app.nexusforms.android.formentry.audit.AuditEvent;
import app.nexusforms.android.formentry.audit.ChangesReasonPromptDialogFragment;
import app.nexusforms.android.formentry.audit.IdentifyUserPromptDialogFragment;
import app.nexusforms.android.formentry.audit.IdentityPromptViewModel;
import app.nexusforms.android.formentry.backgroundlocation.BackgroundLocationManager;
import app.nexusforms.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import app.nexusforms.android.formentry.loading.FormInstanceFileCreator;
import app.nexusforms.android.formentry.media.AudioHelperFactory;
import app.nexusforms.android.formentry.repeats.DeleteRepeatDialogFragment;
import app.nexusforms.android.formentry.saving.FormSaveViewModel;
import app.nexusforms.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import app.nexusforms.android.formentry.saving.SaveAnswerFileProgressDialogFragment;
import app.nexusforms.android.formentry.saving.SaveFormProgressDialogFragment;
import app.nexusforms.android.forms.Form;
import app.nexusforms.android.forms.FormDesignException;
import app.nexusforms.android.forms.FormsRepository;
import app.nexusforms.android.fragments.MediaLoadingFragment;
import app.nexusforms.android.fragments.dialogs.CustomDatePickerDialog;
import app.nexusforms.android.fragments.dialogs.CustomTimePickerDialog;
import app.nexusforms.android.fragments.dialogs.LocationProvidersDisabledDialog;
import app.nexusforms.android.fragments.dialogs.NumberPickerDialog;
import app.nexusforms.android.fragments.dialogs.ProgressDialogFragment;
import app.nexusforms.android.fragments.dialogs.RankingWidgetDialog;
import app.nexusforms.android.fragments.dialogs.SelectMinimalDialog;
import app.nexusforms.android.listeners.FormLoaderListener;
import app.nexusforms.android.listeners.PermissionListener;
import app.nexusforms.android.listeners.SavePointListener;
import app.nexusforms.android.listeners.WidgetValueChangedListener;
import app.nexusforms.android.logic.FormInfo;
import app.nexusforms.android.logic.HierarchyElement;
import app.nexusforms.android.logic.ImmutableDisplayableQuestion;
import app.nexusforms.android.logic.PropertyManager;

import app.nexusforms.android.provider.FormsProviderAPI.FormsColumns;
import app.nexusforms.android.provider.InstanceProviderAPI.InstanceColumns;
import app.nexusforms.android.storage.StoragePathProvider;
import app.nexusforms.android.storage.StorageSubdirectory;
import app.nexusforms.android.tasks.FormLoaderTask;
import app.nexusforms.android.tasks.SaveFormIndexTask;
import app.nexusforms.android.tasks.SavePointTask;
import app.nexusforms.android.utilities.ActivityAvailability;
import app.nexusforms.android.utilities.ApplicationConstants;
import app.nexusforms.android.utilities.DestroyableLifecyleOwner;
import app.nexusforms.android.utilities.DialogUtils;
import app.nexusforms.android.utilities.ExternalAppIntentProvider;
import app.nexusforms.android.utilities.FileUtils;
import app.nexusforms.android.utilities.FormEntryPromptUtils;
import app.nexusforms.android.utilities.MultiClickGuard;
import app.nexusforms.android.utilities.PlayServicesChecker;
import app.nexusforms.android.utilities.ScreenContext;
import app.nexusforms.android.utilities.SnackbarUtils;
import app.nexusforms.android.utilities.SoftKeyboardController;
import app.nexusforms.android.utilities.ToastUtils;
import app.nexusforms.android.widgets.QuestionWidget;
import app.nexusforms.android.widgets.RangePickerDecimalWidget;
import app.nexusforms.android.widgets.RangePickerIntegerWidget;
import app.nexusforms.android.widgets.StringWidget;
import app.nexusforms.android.widgets.WidgetFactory;
import app.nexusforms.android.widgets.interfaces.WidgetDataReceiver;
import app.nexusforms.android.widgets.utilities.ExternalAppRecordingRequester;
import app.nexusforms.android.widgets.utilities.FormControllerWaitingForDataRegistry;
import app.nexusforms.android.widgets.utilities.InternalRecordingRequester;
import app.nexusforms.android.widgets.utilities.RecordingRequesterProvider;
import app.nexusforms.android.widgets.utilities.ViewModelAudioPlayer;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;
import app.nexusforms.async.Scheduler;
import app.nexusforms.audioclips.AudioClipViewModel;
import app.nexusforms.audiorecorder.recording.AudioRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static app.nexusforms.android.utilities.ApplicationConstants.RequestCodes;
import static app.nexusforms.android.utilities.DialogUtils.getDialog;
import static app.nexusforms.android.utilities.DialogUtils.showIfNotShowing;
import static app.nexusforms.android.utilities.ToastUtils.showLongToast;
import static app.nexusforms.android.utilities.ToastUtils.showShortToast;

/**
 * FormEntryActivity is responsible for displaying questions, animating
 * transitions between questions, and allowing the user to enter data.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior
 * option)
 */

@SuppressWarnings("PMD.CouplingBetweenObjects")
public class FormEntryActivity extends CollectAbstractActivity implements
        FormLoaderListener,
        SavePointListener, NumberPickerDialog.NumberPickerListener,
        RankingWidgetDialog.RankingListener, SaveFormIndexTask.SaveFormIndexListener,
        WidgetValueChangedListener, ScreenContext, FormLoadingDialogFragment.FormLoadingDialogFragmentListener,
        QuitFormDialogFragment.Listener, DeleteRepeatDialogFragment.DeleteRepeatDialogCallback,
        SelectMinimalDialog.SelectMinimalDialogListener, CustomDatePickerDialog.DateChangeListener,
        CustomTimePickerDialog.TimeChangeListener{


    public static final String ANSWER_KEY = "value"; // this value can not be changed because it is also used by external apps

    public static final String KEY_INSTANCES = "instances";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    private static final String KEY_SAVE_NAME = "saveName";
    private static final String KEY_LOCATION_PERMISSIONS_GRANTED = "location_permissions_granted";

    private static final String TAG_MEDIA_LOADING_FRAGMENT = "media_loading_fragment";

    // Identifies the gp of the form used to launch form entry
    public static final String KEY_FORMPATH = "formpath";

    // Identifies whether this is a new form, or reloading a form after a screen
    // rotation (or similar)
    private static final String NEWFORM = "newform";
    // these are only processed if we shut down and are restoring after an
    // external intent fires

    public static final String KEY_INSTANCEPATH = "instancepath";
    public static final String KEY_XPATH = "xpath";
    public static final String KEY_XPATH_WAITING_FOR_DATA = "xpathwaiting";

    // Tracks whether we are autosaving
    public static final String KEY_AUTO_SAVED = "autosaved";

    public static final String EXTRA_TESTING_PATH = "testingPath";
    public static final String KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED = "readPhoneStatePermissionRequestNeeded";

    private boolean autoSaved;
    private boolean allowMovingBackwards;

    // Random ID
    private static final int DELETE_REPEAT = 654321;

    private String formPath;
    private String saveName;

    private Animation inAnimation;
    private Animation outAnimation;

    private FrameLayout questionHolder;
    private View currentView;

    private AlertDialog alertDialog;
    private String errorMessage;
    private boolean shownAlertDialogIsGroupRepeat;

    private FormLoaderTask formLoaderTask;

    private TextView nextButton;
    private TextView backButton;

    private Button submitButton;

    //private ODKView odkView;
    private final DestroyableLifecyleOwner odkViewLifecycleFox = new DestroyableLifecyleOwner();

    private String instancePath;
    private String startingXPath;
    private String waitingXPath;
    private boolean newForm = true;
    private boolean readPhoneStatePermissionRequestNeeded;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    MediaLoadingFragment mediaLoadingFragment;
    private FormEntryMenuDelegate menuDelegate;
    private FormIndexAnimationHandler formIndexAnimationHandler;
    private WaitingForDataRegistry waitingForDataRegistry;
    private InternalRecordingRequester internalRecordingRequester;
    private ExternalAppRecordingRequester externalAppRecordingRequester;

    private FormIndex screenIndex;

    private List<HierarchyElement> hierarchyElementsToDisplay;

    private WidgetFactory widgetFactory;

    ViewModelAudioPlayer viewModelAudioPlayer;

    private ArrayList<QuestionWidget> questionWidgetArrayList;

    private LinearLayout widgetsListLinearLayout;

    private LinearLayout.LayoutParams layoutParams;

    private AudioHelper audioHelper;

    private ScreenContext screenContext;

    private FormEndView endView;
    private List<FormEntryPrompt> questionsPrompt;





    private boolean showNavigationButtons;

    @Inject
    RxEventBus eventBus;

    @Inject
    Analytics analytics;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    FormsRepository formsRepository;

    @Inject
    PropertyManager propertyManager;

    @Inject
    FormSubmitManager formSubmitManager;

    @Inject
    Scheduler scheduler;

    @Inject
    AudioRecorder audioRecorder;

    @Inject
    FormSaveViewModel.FactoryFactory formSaveViewModelFactoryFactory;

    @Inject
    FormEntryViewModel.Factory formEntryViewModelFactory;

    @Inject
    SoftKeyboardController softKeyboardController;

    @Inject
    PermissionsChecker permissionsChecker;

    @Inject
    ActivityAvailability activityAvailability;

    @Inject
    ExternalAppIntentProvider externalAppIntentProvider;

    @Inject
    BackgroundAudioViewModel.Factory backgroundAudioViewModelFactory;

    @Inject
    public AudioHelperFactory audioHelperFactory;

    private final LocationProvidersReceiver locationProvidersReceiver = new LocationProvidersReceiver();


    /**
     * True if the Android location permission was granted last time it was checked. Allows for
     * detection of location permissions changes while the activity is in the background.
     */
    private boolean locationPermissionsPreviouslyGranted;

    private BackgroundLocationViewModel backgroundLocationViewModel;
    private IdentityPromptViewModel identityPromptViewModel;
    private FormSaveViewModel formSaveViewModel;
    private FormEntryViewModel formEntryViewModel;
    private BackgroundAudioViewModel backgroundAudioViewModel;

    private FormIndex repeatGroupPickerIndex;


    QuestionsAdapter questionsAdapter;

    RecyclerView recycler;

    private FormIndex currentIndex;


    private TreeReference contextGroupRef;

    List<FormEntryPrompt> readyProcessedQuestions;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect.getInstance().getComponent().inject(this);
        setContentView(R.layout.form_entry);

        setupViewModels();

        compositeDisposable.add(eventBus
                .register(ReadPhoneStatePermissionRxEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    readPhoneStatePermissionRequestNeeded = true;
                }));

        errorMessage = null;

        questionHolder = findViewById(R.id.questionholder);
        submitButton = findViewById(R.id.button_submit);
        submitButton.setOnClickListener(v -> {
            FormController controller = getFormController();
            if (controller != null) {
                displayFormEndDialog(controller, true);
            }
        });

        initToolbar();


        //showView(createView(event, true), AnimationType.RIGHT);

        menuDelegate = new FormEntryMenuDelegate(
                this,
                () -> getAnswers(),
                formIndexAnimationHandler,
                formSaveViewModel,
                formEntryViewModel,
                audioRecorder,
                backgroundLocationViewModel,
                backgroundAudioViewModel,
                settingsProvider
        );


        if (savedInstanceState == null) {
            mediaLoadingFragment = new MediaLoadingFragment();
            getSupportFragmentManager().beginTransaction().add(mediaLoadingFragment, TAG_MEDIA_LOADING_FRAGMENT).commit();
        } else {
            mediaLoadingFragment = (MediaLoadingFragment) getSupportFragmentManager().findFragmentByTag(TAG_MEDIA_LOADING_FRAGMENT);
        }

        setupFields(savedInstanceState);
        loadForm();
        //refreshView(true);

        questionWidgetArrayList = new ArrayList<>();


        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        //audioHelper = audioHelperFactory.create(this);

        screenContext = this;


    }


    private boolean isScreenEvent(FormController formController, FormIndex index) {
        // Beginning of form.
        if (index == null) {
            return true;
        }

        return formController.isDisplayableGroup(index);
    }

    private void jumpToHierarchyStartIndex() {
        FormController formController = Collect.getInstance().getFormController();
        FormIndex startIndex = formController.getFormIndex();

        // If we're not at the first level, we're inside a repeated group so we want to only
        // display everything enclosed within that group.
        contextGroupRef = null;

        // Save the index to the screen itself, before potentially moving into it.
        screenIndex = startIndex;

        // If we're currently at a displayable group, record the name of the node and step to the next
        // node to display.
        if (formController.isDisplayableGroup(startIndex)) {
            contextGroupRef = formController.getFormIndex().getReference();
            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
        } else {
            FormIndex potentialStartIndex = FormIndexUtils.getPreviousLevel(startIndex);
            // Step back until we hit a displayable group or the beginning.
            while (!isScreenEvent(formController, potentialStartIndex)) {
                potentialStartIndex = FormIndexUtils.getPreviousLevel(potentialStartIndex);
            }

            screenIndex = potentialStartIndex;

            // Check to see if the question is at the first level of the hierarchy.
            // If it is, display the root level from the beginning.
            // Otherwise we're at a displayable group.
            if (screenIndex == null) {
                screenIndex = FormIndex.createBeginningOfFormIndex();
            }

            formController.jumpToIndex(screenIndex);

            // Now test again. This should be true at this point or we're at the beginning.
            if (formController.isDisplayableGroup(formController.getFormIndex())) {
                contextGroupRef = formController.getFormIndex().getReference();
                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            } else {
                // Let contextGroupRef be null.
            }
        }
    }

    private boolean shouldShowRepeatGroupPicker() {
        return repeatGroupPickerIndex != null;
    }

    private void extractQuestionsForListing(boolean isGoingUp) {
        try {
            FormController formController = Collect.getInstance().getFormController();

            if(formController == null)return;

            Timber.d("REFRESHING QUESTIONS -- AT INDEX %s", formController.getFormIndex());

            setTitle(formController.getFormTitle());
            // Save the current index so we can return to the problematic question
            // in the event of an error.
            currentIndex = formController.getFormIndex();

            List<FormEntryPrompt> elementsToDisplay = new ArrayList<>();
            hierarchyElementsToDisplay = new ArrayList<>();

            jumpToHierarchyStartIndex();

            // Refresh the current event in case we did step forward.
            int event = formController.getEvent();

            // Ref to the parent group that's currently being displayed.
            //
            // Because of the guard conditions below, we will skip
            // everything until we exit this group.
            TreeReference visibleGroupRef = null;

            while (event != FormEntryController.EVENT_END_OF_FORM) {
                // get the ref to this element
                TreeReference currentRef = formController.getFormIndex().getReference();

                // retrieve the current group
                TreeReference curGroup = (visibleGroupRef == null) ? contextGroupRef : visibleGroupRef;

                if (curGroup != null && !curGroup.isParentOf(currentRef, false)) {
                    // We have left the current group
                    if (visibleGroupRef == null) {
                        // We are done.
                        break;
                    } else {
                        // exit the inner group
                        visibleGroupRef = null;
                    }
                }

                if (visibleGroupRef != null) {
                    // We're in a group within the one we want to list
                    // skip this question/group/repeat and move to the next index.
                    event = formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                    continue;
                }

                switch (event) {
                    case FormEntryController.EVENT_QUESTION: {
                        // Nothing but repeat group instances should show up in the picker.
                        if (shouldShowRepeatGroupPicker()) {
                            break;
                        }

                        FormEntryPrompt fp = formController.getQuestionPrompt();
                        elementsToDisplay.add(fp);

                        String label = fp.getShortText();
                        String answerDisplay = FormEntryPromptUtils.getAnswerText(fp, this, formController);
                        hierarchyElementsToDisplay.add(
                                new HierarchyElement(FormEntryPromptUtils.markQuestionIfIsRequired(label, fp.isRequired()), answerDisplay, null,
                                        HierarchyElement.Type.QUESTION, fp.getIndex()));
                        break;
                    }
                    case FormEntryController.EVENT_GROUP: {
                        if (!formController.isGroupRelevant()) {
                            break;
                        }
                        // Nothing but repeat group instances should show up in the picker.
                        if (shouldShowRepeatGroupPicker()) {
                            break;
                        }

                        FormIndex index = formController.getFormIndex();

                        // Only display groups with a specific appearance attribute.
                        if (!formController.isDisplayableGroup(index)) {
                            break;
                        }

                        // Don't render other groups' children.
                        if (contextGroupRef != null && !contextGroupRef.isParentOf(currentRef, false)) {
                            break;
                        }

                        visibleGroupRef = currentRef;

                        FormEntryCaption caption = formController.getCaptionPrompt();
                        HierarchyElement groupElement = new HierarchyElement(
                                caption.getShortText(), getString(R.string.group_label),
                                ContextCompat.getDrawable(this, R.drawable.ic_folder_open),
                                HierarchyElement.Type.VISIBLE_GROUP, caption.getIndex());

                        FormEntryPrompt[] groupsQuestions = formController.getQuestionPrompts();

                        elementsToDisplay.addAll(Arrays.asList(groupsQuestions));

                        hierarchyElementsToDisplay.add(groupElement);

                        // Skip to the next item outside the group.
                        event = formController.stepOverGroup();
                        continue;
                    }
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT: {
                        // this would display the 'add new repeat' dialog
                        // ignore it.
                        break;
                    }
                    case FormEntryController.EVENT_REPEAT: {
                        if (!formController.isGroupRelevant()) {
                            break;
                        }

                        visibleGroupRef = currentRef;

                        FormEntryCaption fc = formController.getCaptionPrompt();

                        // Don't render other groups' children.
                        if (contextGroupRef != null && !contextGroupRef.isParentOf(currentRef, false)) {


                            break;
                        }

                        if (shouldShowRepeatGroupPicker()) {
                            // Don't render other groups' instances.
                            String repeatGroupPickerRef = repeatGroupPickerIndex.getReference().toString(false);
                            if (!currentRef.toString(false).equals(repeatGroupPickerRef)) {
                                break;
                            }

                            int itemNumber = fc.getMultiplicity() + 1;

                            // e.g. `friends > 1`
                            String repeatLabel = fc.getShortText() + " > " + itemNumber;

                            // If the child of the group has a more descriptive label, use that instead.
                            if (fc.getFormElement().getChildren().size() == 1 && fc.getFormElement().getChild(0) instanceof GroupDef) {
                                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                                String itemLabel = formController.getCaptionPrompt().getShortText();
                                if (itemLabel != null) {
                                    // e.g. `1. Alice`
                                    repeatLabel = itemNumber + ".\u200E " + itemLabel;
                                }
                            }

                            HierarchyElement instance = new HierarchyElement(
                                    repeatLabel, null,
                                    null, HierarchyElement.Type.REPEAT_INSTANCE, fc.getIndex());

                            hierarchyElementsToDisplay.add(instance);

                        } else if (fc.getMultiplicity() == 0) {
                            // Display the repeat header for the group.
                            HierarchyElement group = new HierarchyElement(
                                    fc.getShortText(), getString(R.string.repeatable_group_label),
                                    ContextCompat.getDrawable(this, R.drawable.ic_repeat),
                                    HierarchyElement.Type.REPEATABLE_GROUP, fc.getIndex());

                            hierarchyElementsToDisplay.add(group);
                        }

                        break;
                    }
                }

                event = formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            }

            // TODO traverse tree recyclerView.setAdapter(new HierarchyListAdapter(elementsToDisplay, this::onElementClick));

            formController.jumpToIndex(currentIndex);


            // Prevent a redundant middle screen (common on many forms
            // that use presentation groups to display labels).
            if (isDisplayingSingleGroup() && !screenIndex.isBeginningOfFormIndex()) {
                if (isGoingUp) {
                    // Back out once more.
                    goUpLevel();
                } else {
                    // Enter automatically.
                    formController.jumpToIndex(hierarchyElementsToDisplay.get(0).getFormIndex());
                    extractQuestionsForListing(false);
                }
            }

            backgroundLocationViewModel.questions.postValue(elementsToDisplay);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private boolean isDisplayingSingleGroup() {
        return hierarchyElementsToDisplay.size() == 1
                && hierarchyElementsToDisplay.get(0).getType() == HierarchyElement.Type.VISIBLE_GROUP;
    }

    protected void goUpLevel() {
        FormController formController = Collect.getInstance().getFormController();

        // If `repeatGroupPickerIndex` is set it means we're currently displaying
        // a list of repeat instances. If we unset `repeatGroupPickerIndex`,
        // we will go back up to the previous screen.
        if (shouldShowRepeatGroupPicker()) {
            // Exit the picker.
            repeatGroupPickerIndex = null;
        } else {
            // Enter the picker if coming from a repeat group.
            int event = formController.getEvent(screenIndex);
            if (event == FormEntryController.EVENT_REPEAT || event == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                repeatGroupPickerIndex = screenIndex;
            }

            formController.stepToOuterScreenEvent();
        }

        extractQuestionsForListing(true);
    }


    private void setupViewModels() {
        backgroundLocationViewModel = ViewModelProviders
                .of(this, new BackgroundLocationViewModel.Factory(permissionsProvider, settingsProvider.getGeneralSettings()))
                .get(BackgroundLocationViewModel.class);

        backgroundAudioViewModel = new ViewModelProvider(this, backgroundAudioViewModelFactory).get(BackgroundAudioViewModel.class);
        backgroundAudioViewModel.isPermissionRequired().observe(this, isPermissionRequired -> {
            if (isPermissionRequired) {
                showIfNotShowing(BackgroundAudioPermissionDialogFragment.class, getSupportFragmentManager());
            }
        });

        backgroundLocationViewModel.questions.observe(this, questions -> {
                    readyProcessedQuestions = questions;

                    View populatedViewUsingRecycler = displayAllQuestionsInForm(questions);

                    renderQuestions(populatedViewUsingRecycler);
                }


        );
        AudioClipViewModel.Factory factory = new AudioClipViewModel.Factory(MediaPlayer::new, scheduler);

        viewModelAudioPlayer = new ViewModelAudioPlayer(ViewModelProviders
                .of(this, factory)
                .get(AudioClipViewModel.class), odkViewLifecycleFox);


        identityPromptViewModel = ViewModelProviders.of(this).get(IdentityPromptViewModel.class);
        identityPromptViewModel.requiresIdentityToContinue().observe(this, requiresIdentity -> {
            if (requiresIdentity) {
                showIfNotShowing(IdentifyUserPromptDialogFragment.class, getSupportFragmentManager());
            }
        });

        identityPromptViewModel.isFormEntryCancelled().observe(this, isFormEntryCancelled -> {
            if (isFormEntryCancelled) {
                finish();
            }
        });

        formEntryViewModel = ViewModelProviders
                .of(this, formEntryViewModelFactory)
                .get(FormEntryViewModel.class);

        formEntryViewModel.getError().observe(this, error -> {
            if (error instanceof FormEntryViewModel.NonFatal) {
                createErrorDialog(((FormEntryViewModel.NonFatal) error).getMessage(), false);
                formEntryViewModel.errorDisplayed();
            }
        });

        formSaveViewModel = new ViewModelProvider(this, formSaveViewModelFactoryFactory.create(this, null)).get(FormSaveViewModel.class);
        formSaveViewModel.getSaveResult().observe(this, this::handleSaveResult);
        formSaveViewModel.isSavingAnswerFile().observe(this, isSavingAnswerFile -> {
            if (isSavingAnswerFile) {
                DialogUtils.showIfNotShowing(SaveAnswerFileProgressDialogFragment.class, getSupportFragmentManager());
            } else {
                DialogUtils.dismissDialog(SaveAnswerFileProgressDialogFragment.class, getSupportFragmentManager());
            }
        });

        formSaveViewModel.getAnswerFileError().observe(this, file -> {
            if (file != null) {
                DialogUtils.showIfNotShowing(SaveAnswerFileErrorDialogFragment.class, getSupportFragmentManager());
            }
        });

        internalRecordingRequester = new InternalRecordingRequester(this, audioRecorder, permissionsProvider, formEntryViewModel);

        waitingForDataRegistry = new FormControllerWaitingForDataRegistry();
        externalAppRecordingRequester = new ExternalAppRecordingRequester(this, activityAvailability, waitingForDataRegistry, permissionsProvider, formEntryViewModel);

        RecordingHandler recordingHandler = new RecordingHandler(formSaveViewModel, this, audioRecorder, new AMRAppender(), new M4AAppender());
        audioRecorder.getCurrentSession().observe(this, session -> {
            if (session != null && session.getFile() != null) {
                recordingHandler.handle(getFormController(), session, success -> {
                    if (success) {
                        onScreenRefresh();
                        formSaveViewModel.resumeSave();
                    } else {
                        String path = session.getFile().getAbsolutePath();
                        String message = getString(R.string.answer_file_copy_failed_message, path);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // Precondition: the instance directory must be ready so that the audit file can be created
    private void formControllerAvailable(@NonNull FormController formController) {
        menuDelegate.formLoaded(formController);

        identityPromptViewModel.formLoaded(formController);
        formEntryViewModel.formLoaded(formController);
        formSaveViewModel.formLoaded(formController);
        backgroundAudioViewModel.formLoaded(formController);
    }

    private void setupFields(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_FORMPATH)) {
                formPath = savedInstanceState.getString(KEY_FORMPATH);
            }
            if (savedInstanceState.containsKey(KEY_INSTANCEPATH)) {
                instancePath = savedInstanceState.getString(KEY_INSTANCEPATH);
            }
            if (savedInstanceState.containsKey(KEY_XPATH)) {
                startingXPath = savedInstanceState.getString(KEY_XPATH);
                Timber.i("startingXPath is: %s", startingXPath);
            }
            if (savedInstanceState.containsKey(KEY_XPATH_WAITING_FOR_DATA)) {
                waitingXPath = savedInstanceState
                        .getString(KEY_XPATH_WAITING_FOR_DATA);
                Timber.i("waitingXPath is: %s", waitingXPath);
            }
            if (savedInstanceState.containsKey(NEWFORM)) {
                newForm = savedInstanceState.getBoolean(NEWFORM, true);
            }
            if (savedInstanceState.containsKey(KEY_ERROR)) {
                errorMessage = savedInstanceState.getString(KEY_ERROR);
            }
            saveName = savedInstanceState.getString(KEY_SAVE_NAME);
            if (savedInstanceState.containsKey(KEY_AUTO_SAVED)) {
                autoSaved = savedInstanceState.getBoolean(KEY_AUTO_SAVED);
            }
            if (savedInstanceState.containsKey(KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED)) {
                readPhoneStatePermissionRequestNeeded = savedInstanceState.getBoolean(KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED);
            }
            if (savedInstanceState.containsKey(KEY_LOCATION_PERMISSIONS_GRANTED)) {
                locationPermissionsPreviouslyGranted = savedInstanceState.getBoolean(KEY_LOCATION_PERMISSIONS_GRANTED);
            }
        }
    }

    private void loadForm() {
        allowMovingBackwards = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_MOVING_BACKWARDS);

        // If a parse error message is showing then nothing else is loaded
        // Dialogs mid form just disappear on rotation.
        if (errorMessage != null) {
            createErrorDialog(errorMessage, true);
            return;
        }

        // Check to see if this is a screen flip or a new form load.
        Object data = getLastCustomNonConfigurationInstance();
        if (data instanceof FormLoaderTask) {
            formLoaderTask = (FormLoaderTask) data;
        } else if (data == null) {
            if (!newForm) {
                FormController formController = getFormController();

                if (formController != null) {
                    formControllerAvailable(formController);
                    //onScreenRefresh();
                } else {
                    Timber.w("Reloading form and restoring state.");
                    formLoaderTask = new FormLoaderTask(instancePath, startingXPath, waitingXPath);
                    showIfNotShowing(FormLoadingDialogFragment.class, getSupportFragmentManager());
                    formLoaderTask.execute(formPath);
                }

                return;
            }

            // Not a restart from a screen orientation change (or other).
            Collect.getInstance().setFormController(null);

            Intent intent = getIntent();
            if (intent != null) {
                loadFromIntent(intent);
            }
        }
    }

    private void loadFromIntent(Intent intent) {
        Uri uri = intent.getData();
        String uriMimeType = null;

        if (uri != null) {
            uriMimeType = getContentResolver().getType(uri);
        }

        if (uriMimeType == null && intent.hasExtra(EXTRA_TESTING_PATH)) {
            formPath = intent.getStringExtra(EXTRA_TESTING_PATH);

        } else if (uriMimeType != null && uriMimeType.equals(InstanceColumns.CONTENT_ITEM_TYPE)) {
            FormInfo formInfo = ContentResolverHelper.getFormDetails(uri);

            if (formInfo == null) {
                createErrorDialog(getString(R.string.bad_uri, uri), true);
                return;
            }

            instancePath = formInfo.getInstancePath();
            List<Form> candidateForms = formsRepository.getAllByFormIdAndVersion(formInfo.getFormId(), formInfo.getFormVersion());

            if (candidateForms.isEmpty()) {
                createErrorDialog(getString(
                        R.string.parent_form_not_present,
                        formInfo.getFormId())
                                + ((formInfo.getFormVersion() == null) ? ""
                                : "\n" + getString(R.string.version) + " " + formInfo.getFormVersion()),
                        true);
                return;
            } else if (candidateForms.stream().filter(f -> !f.isDeleted()).count() > 1) {
                createErrorDialog(getString(R.string.survey_multiple_forms_error), true);
                return;
            }

            formPath = candidateForms.get(0).getFormFilePath();
        } else if (uriMimeType != null
                && uriMimeType.equals(FormsColumns.CONTENT_ITEM_TYPE)) {
            formPath = ContentResolverHelper.getFormPath(uri);
            if (formPath == null) {
                createErrorDialog(getString(R.string.bad_uri, uri), true);
                return;
            } else {
                /**
                 * This is the fill-blank-form code path.See if there is a savepoint for this form
                 * that has never been explicitly saved by the user. If there is, open this savepoint(resume this filled-in form).
                 * Savepoints for forms that were explicitly saved will be recovered when that
                 * explicitly saved instance is edited via edit-saved-form.
                 */
                final String filePrefix = formPath.substring(
                        formPath.lastIndexOf('/') + 1,
                        formPath.lastIndexOf('.'))
                        + "_";
                final String fileSuffix = ".xml.save";
                File cacheDir = new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE));
                File[] files = cacheDir.listFiles(pathname -> {
                    String name = pathname.getName();
                    return name.startsWith(filePrefix)
                            && name.endsWith(fileSuffix);
                });

                /**
                 * See if any of these savepoints are for a filled-in form that has never
                 * been explicitly saved by the user.
                 */
                for (File candidate : files) {
                    String instanceDirName = candidate.getName()
                            .substring(
                                    0,
                                    candidate.getName().length()
                                            - fileSuffix.length());
                    File instanceDir = new File(
                            storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES) + File.separator
                                    + instanceDirName);
                    File instanceFile = new File(instanceDir,
                            instanceDirName + ".xml");
                    if (instanceDir.exists()
                            && instanceDir.isDirectory()
                            && !instanceFile.exists()) {
                        // yes! -- use this savepoint file
                        instancePath = instanceFile
                                .getAbsolutePath();
                        break;
                    }
                }
            }
        } else {
            Timber.i("Unrecognized URI: %s", uri);
            createErrorDialog(getString(R.string.unrecognized_uri, uri), true);
            return;
        }

        formLoaderTask = new FormLoaderTask(instancePath, null, null);
        showIfNotShowing(FormLoadingDialogFragment.class, getSupportFragmentManager());
        formLoaderTask.execute(formPath);
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.loading_form));
    }

    /**
     * Creates save-points asynchronously in order to not affect swiping performance on larger forms.
     * If moving backwards through a form is disabled, also saves the index of the form element that
     * was last shown to the user so that no matter how the app exits and relaunches, the user can't
     * see previous questions.
     */
    private void nonblockingCreateSavePointData() {
        try {
            SavePointTask savePointTask = new SavePointTask(this);
            savePointTask.execute();

            if (!allowMovingBackwards) {
                FormController formController = getFormController();
                if (formController != null) {
                    new SaveFormIndexTask(this, formController.getFormIndex()).execute();
                }
            }
        } catch (Exception e) {
            Timber.e("Could not schedule SavePointTask. Perhaps a lot of swiping is taking place?");
        }
    }

    // This method may return null if called before form loading is finished
    @Nullable
    private FormController getFormController() {
        return Collect.getInstance().getFormController();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FORMPATH, formPath);
        FormController formController = getFormController();
        if (formController != null) {
            if (formController.getInstanceFile() != null) {
                outState.putString(KEY_INSTANCEPATH, getAbsoluteInstancePath());
            }
            outState.putString(KEY_XPATH,
                    formController.getXPath(formController.getFormIndex()));
            FormIndex waiting = formController.getIndexWaitingForData();
            if (waiting != null) {
                outState.putString(KEY_XPATH_WAITING_FOR_DATA,
                        formController.getXPath(waiting));
            }
            // save the instance to a temp path...
            nonblockingCreateSavePointData();
        }
        outState.putBoolean(NEWFORM, false);
        outState.putString(KEY_ERROR, errorMessage);
        outState.putString(KEY_SAVE_NAME, saveName);
        outState.putBoolean(KEY_AUTO_SAVED, autoSaved);
        outState.putBoolean(KEY_READ_PHONE_STATE_PERMISSION_REQUEST_NEEDED, readPhoneStatePermissionRequestNeeded);
        outState.putBoolean(KEY_LOCATION_PERMISSIONS_GRANTED, locationPermissionsPreviouslyGranted);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        FormController formController = getFormController();
        if (formController == null) {
            // we must be in the midst of a reload of the FormController.
            // try to save this callback data to the FormLoaderTask
            if (formLoaderTask != null
                    && formLoaderTask.getStatus() != AsyncTask.Status.FINISHED) {
                formLoaderTask.setActivityResult(requestCode, resultCode, intent);
            } else {
                Timber.e("Got an activityResult without any pending form loader");
            }
            return;
        }

        // If we're coming back from the hierarchy view, the user has either tapped the back
        // button or another question to jump to so we need to rebuild the view.
        if (requestCode == RequestCodes.HIERARCHY_ACTIVITY || requestCode == RequestCodes.CHANGE_SETTINGS) {
            onScreenRefresh();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            waitingForDataRegistry.cancelWaitingForData();
            return;
        }

        // intent is needed for all requestCodes except of DRAW_IMAGE, ANNOTATE_IMAGE, SIGNATURE_CAPTURE, IMAGE_CAPTURE and HIERARCHY_ACTIVITY
        if (intent == null && requestCode != RequestCodes.DRAW_IMAGE && requestCode != RequestCodes.ANNOTATE_IMAGE
                && requestCode != RequestCodes.SIGNATURE_CAPTURE && requestCode != RequestCodes.IMAGE_CAPTURE) {
            Timber.d("The intent has a null value for requestCode: %s", requestCode);
            showLongToast(getString(R.string.null_intent_value));
            return;
        }

        switch (requestCode) {
            case RequestCodes.OSM_CAPTURE:
                setWidgetData(intent.getStringExtra("OSM_FILE_NAME"));
                break;
            case RequestCodes.EX_ARBITRARY_FILE_CHOOSER:
            case RequestCodes.EX_VIDEO_CHOOSER:
            case RequestCodes.EX_IMAGE_CHOOSER:
            case RequestCodes.EX_AUDIO_CHOOSER:
                if (intent.getClipData() != null
                        && intent.getClipData().getItemCount() > 0
                        && intent.getClipData().getItemAt(0) != null) {
                    loadFile(intent.getClipData().getItemAt(0).getUri());
                } else {
                    setWidgetData(null);
                }
                break;
            case RequestCodes.EX_GROUP_CAPTURE:
                try {
                    Bundle extras = intent.getExtras();
                    if (currentView != null) {
                        setDataForFields(extras);
                        Timber.d("SERIOUS some data found %s", extras.toString());
                    } else {
                        Timber.d("NOT SERIOUS - current view is null");
                    }
                } catch (JavaRosaException e) {
                    Timber.e(e);
                    createErrorDialog(e.getCause().getMessage(), false);
                }
                break;
            case RequestCodes.DRAW_IMAGE:
            case RequestCodes.ANNOTATE_IMAGE:
            case RequestCodes.SIGNATURE_CAPTURE:
            case RequestCodes.IMAGE_CAPTURE:
                loadFile(Uri.fromFile(new File(storagePathProvider.getTmpImageFilePath())));
                break;
            case RequestCodes.ALIGNED_IMAGE:
            case RequestCodes.ARBITRARY_FILE_CHOOSER:
            case RequestCodes.AUDIO_CAPTURE:
            case RequestCodes.AUDIO_CHOOSER:
            case RequestCodes.VIDEO_CAPTURE:
            case RequestCodes.VIDEO_CHOOSER:
            case RequestCodes.IMAGE_CHOOSER:
                loadFile(intent.getData());
                break;
            case RequestCodes.LOCATION_CAPTURE:
            case RequestCodes.GEOSHAPE_CAPTURE:
            case RequestCodes.GEOTRACE_CAPTURE:
            case RequestCodes.BEARING_CAPTURE:
            case RequestCodes.BARCODE_CAPTURE:
            case RequestCodes.EX_STRING_CAPTURE:
            case RequestCodes.EX_INT_CAPTURE:
            case RequestCodes.EX_DECIMAL_CAPTURE:
                setWidgetData(intent.getExtras().get(ANSWER_KEY));
                break;
        }
    }

    //Load from Intent
    public void setDataForFields(Bundle bundle) throws JavaRosaException {
        Timber.d("DATA SETTER - ");
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            return;
        }

        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                Object answer = bundle.get(key);
                if (answer == null) {
                    continue;
                }
                for (QuestionWidget questionWidget : questionWidgetArrayList) {
                    FormEntryPrompt prompt = questionWidget.getFormEntryPrompt();
                    TreeReference treeReference =
                            (TreeReference) prompt.getFormElement().getBind().getReference();

                    if (treeReference.getNameLast().equals(key)) {
                        switch (prompt.getDataType()) {
                            case Constants.DATATYPE_TEXT:
                                formController.saveAnswer(prompt.getIndex(),
                                        ExternalAppsUtils.asStringData(answer));
                                ((StringWidget) questionWidget).setDisplayValueFromModel();
                                questionWidget.showAnswerContainer();
                                break;
                            case Constants.DATATYPE_INTEGER:
                                formController.saveAnswer(prompt.getIndex(),
                                        ExternalAppsUtils.asIntegerData(answer));
                                ((StringWidget) questionWidget).setDisplayValueFromModel();
                                questionWidget.showAnswerContainer();
                                break;
                            case Constants.DATATYPE_DECIMAL:
                                formController.saveAnswer(prompt.getIndex(),
                                        ExternalAppsUtils.asDecimalData(answer));
                                ((StringWidget) questionWidget).setDisplayValueFromModel();
                                questionWidget.showAnswerContainer();
                                break;
                            case Constants.DATATYPE_BINARY:
                                try {
                                    Uri uri;
                                    if (answer instanceof Uri) {
                                        uri = (Uri) answer;
                                    } else if (answer instanceof String) {
                                        uri = Uri.parse(bundle.getString(key));
                                    } else {
                                        throw new RuntimeException("The value for " + key + " must be a URI but it is " + answer);
                                    }

                                    permissionsProvider.requestReadUriPermission((Activity) getApplicationContext(), uri, getApplicationContext().getContentResolver(), new PermissionListener() {
                                        @Override
                                        public void granted() {
                                            File destFile = FileUtils.createDestinationMediaFile(formController.getInstanceFile().getParent(), ContentResolverHelper.getFileExtensionFromUri(uri));
                                            //TODO might be better to use QuestionMediaManager in the future
                                            FileUtils.saveAnswerFileFromUri(uri, destFile, getApplicationContext());
                                            ((WidgetDataReceiver) questionWidget).setData(destFile);

                                            questionWidget.showAnswerContainer();
                                        }

                                        @Override
                                        public void denied() {

                                        }
                                    });
                                } catch (Exception | Error e) {
                                    Timber.w(e);
                                }
                                break;
                            default:
                                throw new RuntimeException(
                                        getApplicationContext().getString(R.string.ext_assign_value_error,
                                                treeReference.toString(false)));
                        }
                        break;
                    }
                }
            }
        }
    }

    private void loadFile(Uri uri) {
        permissionsProvider.requestReadUriPermission(this, uri, getContentResolver(), new PermissionListener() {
            @Override
            public void granted() {
                ProgressDialogFragment progressDialog = new ProgressDialogFragment();
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.show(getSupportFragmentManager(), ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);

                mediaLoadingFragment.beginMediaLoadingTask(uri);
            }

            @Override
            public void denied() {

            }
        });
    }

    public QuestionWidget getWidgetWaitingForBinaryData() {

        if (currentView != null) {
            for (QuestionWidget qw : questionWidgetArrayList) {
                if (waitingForDataRegistry.isWaitingForData(qw.getFormEntryPrompt().getIndex())) {
                    return qw;
                }
            }
        } else {
            Timber.e("currentView returned null.");
        }
        return null;
    }

    public void setWidgetData(Object data) {
        Timber.d("RESULT RECEIVED %s", data.toString());

        if (currentView != null) {
            boolean set = false;
            for (QuestionWidget widget : questionWidgetArrayList) {
                if (widget instanceof WidgetDataReceiver) {
                    if (waitingForDataRegistry.isWaitingForData(widget.getFormEntryPrompt().getIndex())) {
                        try {
                            Timber.d("WAITER -- set");
                            ((WidgetDataReceiver) widget).setData(data);
                            waitingForDataRegistry.cancelWaitingForData();
                            Timber.d("DONE setting - data");
                            return;
                        } catch (Exception e) {
                            Timber.e(e);
                            ToastUtils.showLongToast(getApplicationContext().getString(R.string.error_attaching_binary_file,
                                    e.getMessage()));
                        }
                        set = true;
                        break;
                    }
                }
            }

            if (!set) {
                Timber.e("Attempting to return data to a widget or set of widgets not looking for data");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuDelegate.onCreateOptionsMenu(getMenuInflater(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menuDelegate.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (menuDelegate.onOptionsItemSelected(item)) {
            return true;
        }

        // These actions should move into the `FormEntryMenuDelegate`
        switch (item.getItemId()) {
            case R.id.menu_languages:
                createLanguageDialog();
                return true;

            case R.id.menu_save:
                // don't exit
//                onSaveChangesClicked();
                saveForm(false, InstancesDaoHelper.isInstanceComplete(false, settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_COMPLETED_DEFAULT)), null);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempt to save the answer(s) in the current screen to into the data
     * model.
     *
     * @return false if any error occurs while saving (constraint violated,
     * etc...), true otherwise.
     */


    private boolean validateFormAnswers(boolean evaluateConstraints) {
        FormController formController = getFormController();
        if (formController != null) {
            HashMap<FormIndex, IAnswerData> answers = getAnswers();
            try {
                FormController.FailedConstraint constraint = formController.saveAllScreenAnswers(answers, evaluateConstraints);

                if (constraint != null) {
                    createConstraintToast(constraint.index, constraint.status);
                    int position = questionsAdapter.getItemPosition(constraint.index);
                    recycler.smoothScrollToPosition(position);

                    highlightWidget(constraint.index);
                    if (formController.indexIsInFieldList() && formController.getQuestionPrompts().length > 1) {
                        //TODO(TO FIGURE OUT)
                    }
                    endView.dismiss();
                    return false;
                }
            } catch (JavaRosaException | FormDesignException e) {
                Timber.e(e);
                createErrorDialog(e.getMessage(), false);
                return false;
            }
        }

        return true;
    }

    /**
     * Highlights the question at the given {@link FormIndex} in red for 2.5 seconds, scrolls the
     * view to display that question at the top and gives it focus.
     */
    public void highlightWidget(FormIndex formIndex) {
        QuestionWidget qw = getQuestionWidget(formIndex);

        if (qw != null) {
            // postDelayed is needed because otherwise scrolling may not work as expected in case when
            // answers are validated during form finalization.
            qw.setFocus(qw.getContext());
            ValueAnimator va = new ValueAnimator();
            va.setIntValues(getResources().getColor(R.color.red_500), qw.getDrawingCacheBackgroundColor());
            va.setEvaluator(new ArgbEvaluator());
            va.addUpdateListener(valueAnimator -> qw.setBackgroundColor((int) valueAnimator.getAnimatedValue()));
            va.setDuration(2500);
            va.start();

        }
    }

    private QuestionWidget getQuestionWidget(FormIndex formIndex) {
        for (QuestionWidget qw : questionWidgetArrayList) {
            if (formIndex.equals(qw.getFormEntryPrompt().getIndex())) {
                return qw;
            }
        }
        return null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        FormController formController = getFormController();

        menu.add(0, v.getId(), 0, getString(R.string.clear_answer));
        if (formController.indexContainsRepeatableGroup()) {
            menu.add(0, DELETE_REPEAT, 0, getString(R.string.delete_repeat));
        }
        menu.setHeaderTitle(getString(R.string.edit_prompt));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == DELETE_REPEAT) {
            DialogUtils.showIfNotShowing(DeleteRepeatDialogFragment.class, getSupportFragmentManager());
        }


        return super.onContextItemSelected(item);
    }

    @Override
    public void deleteGroup() {
        FormController formController = getFormController();
        if (formController != null && !formController.indexIsInFieldList()) {
           // swipeHandler.setBeenSwiped(true);
            //onSwipeForward();
            //TODO must figure out what 'delete' does to the displayed questions
        } else {
            onScreenRefresh();
        }
    }

    /**
     * If we're loading, then we pass the loading thread to our next instance.
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        FormController formController = getFormController();
        // if a form is loading, pass the loader task
        if (formLoaderTask != null
                && formLoaderTask.getStatus() != AsyncTask.Status.FINISHED) {
            return formLoaderTask;
        }

        return null;
    }

    private Integer notifyFromRecycler(QuestionWidget changedWidget) {
        if (changedWidget != null) {
            widgetValueChanged(changedWidget);
            return 1;
        }
        return 0;

    }


    /**
     * Creates and returns a new view based on the event type passed in. The view returned is
     * of type {@link View} if the event passed in represents the end of the form or of type
     * otherwise.
     *
     * @param --        true if this results from advancing through the form
     * @param questions
     * @return newly created View
     */

    private View displayAllQuestionsInForm(List<FormEntryPrompt> questions) {

        questionWidgetArrayList.clear();

        View questionsView = View.inflate(this, R.layout.nexus_questions_layout, (ViewGroup) currentView);

        odkViewLifecycleFox.start();

        audioHelper = audioHelperFactory.create((Context) screenContext);

        this.widgetFactory = new WidgetFactory(this
                ,
                false,
                settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_EXTERNAL_APP_RECORDING),
                waitingForDataRegistry,//set - OK
                formSaveViewModel,
                viewModelAudioPlayer,
                activityAvailability,
                new RecordingRequesterProvider(
                        internalRecordingRequester,
                        externalAppRecordingRequester
                ),
                formEntryViewModel,
                audioRecorder,
                odkViewLifecycleFox

        );

        for (FormEntryPrompt question : questions) {

            QuestionWidget qw = widgetFactory.createWidgetFromPrompt(question, permissionsProvider);

            questionWidgetArrayList.add(qw);

        }


        questionsAdapter = new QuestionsAdapter(questionWidgetArrayList, this::notifyFromRecycler);

        recycler = questionsView.findViewById(R.id.recycler_view_questions);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    submitButton.setVisibility(View.VISIBLE);
                } else {
                    submitButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));

        recycler.setAdapter(questionsAdapter);

        DialogUtils.dismissDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());

        return questionsView;

    }

    @Override
    public FragmentActivity getActivity() {
        return this;
    }

    @Override
    public LifecycleOwner getViewLifecycle() {
        return odkViewLifecycleFox;
    }

    private void releaseOdkView() {
        odkViewLifecycleFox.destroy();

    }



    /**
     * Creates the final screen in a form-filling interaction. Allows the user to set a display
     * name for the instance and to decide whether the form should be finalized or not. Presents
     * a button for saving and exiting.
     */
    private void displayFormEndDialog(FormController formController, Boolean isInstanceCalledFromCompletion) {

        if (formController.getSubmissionMetadata().instanceName != null) {
            saveName = formController.getSubmissionMetadata().instanceName;
        } else {
            // no meta/instanceName field in the form -- see if we have a
            // name for this instance from a previous save attempt...
            String uriMimeType = null;
            Uri instanceUri = getIntent().getData();
            if (instanceUri != null) {
                uriMimeType = getContentResolver().getType(instanceUri);
            }

            if (saveName == null && uriMimeType != null
                    && uriMimeType.equals(InstanceColumns.CONTENT_ITEM_TYPE)) {
                Cursor instance = null;
                try {
                    instance = getContentResolver().query(instanceUri,
                            null, null, null, null);
                    if (instance != null && instance.getCount() == 1) {
                        instance.moveToFirst();
                        saveName = instance
                                .getString(instance
                                        .getColumnIndex(InstanceColumns.DISPLAY_NAME));
                    }
                } finally {
                    if (instance != null) {
                        instance.close();
                    }
                }
            }

            if (saveName == null) {
                saveName = formSaveViewModel.getFormName();
            }
        }


        endView = new FormEndView(this, formSaveViewModel.getFormName(),
                saveName, InstancesDaoHelper.isInstanceComplete(isInstanceCalledFromCompletion,
                settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_COMPLETED_DEFAULT)),

                new FormEndView.Listener() {

                    @Override
                    public void onSaveAsChanged(String saveAs) {
                        // Seems like this is needed for rotation?
                        saveName = saveAs;
                    }

                    @Override
                    public void onSaveClicked(boolean markAsFinalized) {

                        if (saveName.length() < 1) {

                            showShortToast(R.string.save_as_error);
                        } else {

                            if (markAsFinalized) {
                                if (validateFormAnswers(true)) {
                                    formSaveViewModel.saveForm(getIntent().getData(), markAsFinalized, saveName, true);
                                }
                            } else {
                                formSaveViewModel.saveForm(getIntent().getData(), markAsFinalized, saveName, true);
                            }

                        }
                    }
                });

        endView.show();

        if (!settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_MARK_AS_FINALIZED)) {
            endView.findViewById(R.id.mark_finished).setVisibility(View.GONE);
        }

        if (formController.getSubmissionMetadata().instanceName != null) {
            // if instanceName is defined in form, this is the name -- no
            // revisions
            // display only the name, not the prompt, and disable edits
            endView.findViewById(R.id.save_form_as).setVisibility(View.GONE);
            endView.findViewById(R.id.save_name).setEnabled(false);
            endView.findViewById(R.id.save_name).setVisibility(View.VISIBLE);
        }

        // override the visibility settings based upon admin preferences
        if (!settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_SAVE_AS)) {
            endView.findViewById(R.id.save_form_as).setVisibility(View.GONE);
            endView.findViewById(R.id.save_name).setVisibility(View.GONE);
        }

    }

    /**
     * Rebuilds the current view. the controller and the displayed view can get
     * out of sync due to dialogs and restarts caused by screen orientation
     * changes, so they're resynchronized here.
     */

    public void onScreenRefresh() {
        extractQuestionsForListing(false);
    }

    public void renderQuestions(View preparedView) {
        currentView = preparedView;

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        ViewParent currentViewParent;

        currentViewParent = currentView.getParent();


        if (currentViewParent != null) {
            ((ViewGroup) currentViewParent).removeView(currentView);

        }
        questionHolder.addView(currentView, lp);

    }

    /**
     * Displays the View specified by the parameter 'next', animating both the
     * current view and next appropriately given the AnimationType. Also updates
     * the progress bar.
     */
    public void showView(View next) {
        invalidateOptionsMenu();

        // drop keyboard before transition...
        if (currentView != null) {
            softKeyboardController.hideSoftKeyboard(currentView);
        }

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        // adjust which view is in the layout container...
        View staleView = currentView;
        currentView = next;

        questionHolder.addView(currentView, lp);

        if (staleView != null) {
            // and remove the old view (MUST occur after start of animation!!!)
            questionHolder.removeView(staleView);
        }

    }

    /**
     * Creates and displays a dialog displaying the violated constraint.
     */
    private void createConstraintToast(FormIndex index, int saveStatus) {
        FormController formController = getFormController();
        if(formController == null)return;
        String constraintText;
        switch (saveStatus) {
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                constraintText = formController
                        .getQuestionPromptConstraintText(index);
                if (constraintText == null) {
                    constraintText = formController.getQuestionPrompt(index)
                            .getSpecialFormQuestionText("constraintMsg");
                    if (constraintText == null) {
                        constraintText = getString(R.string.invalid_answer_error);
                    }
                }
                break;
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                constraintText = formController
                        .getQuestionPromptRequiredText(index);
                if (constraintText == null) {
                    constraintText = formController.getQuestionPrompt(index)
                            .getSpecialFormQuestionText("requiredMsg");
                    if (constraintText == null) {
                        constraintText = getString(R.string.required_answer_error);
                    }
                }
                break;
            default:
                return;
        }

        ToastUtils.showShortToast(constraintText);
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        if (alertDialog != null && alertDialog.isShowing()) {
            errorMsg = errorMessage + "\n\n" + errorMsg;
            errorMessage = errorMsg;
        } else {
            alertDialog = new AlertDialog.Builder(this).create();
            errorMessage = errorMsg;
        }

        alertDialog.setTitle(getString(R.string.error_occured));
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE:
                    if (shouldExit) {
                        errorMessage = null;
                        finish();
                    }
                    break;
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(BUTTON_POSITIVE, getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    /**
     * Saves data and writes it to disk. If exit is set, program will exit after
     * save completes. Complete indicates whether the user has marked the
     * isntances as complete. If updatedSaveName is non-null, the instances
     * content provider is updated with the new name
     */
    private boolean saveForm(boolean exit, boolean complete, String updatedSaveName) {

        formSaveViewModel.saveForm(getIntent().getData(), complete, updatedSaveName, exit);

        return true;
    }

    private void handleSaveResult(FormSaveViewModel.SaveResult result) {
        if (result == null) {
            return;
        }

        if (endView != null) {
            endView.dismiss();
        }

        switch (result.getState()) {
            case CHANGE_REASON_REQUIRED:
                showIfNotShowing(ChangesReasonPromptDialogFragment.class, getSupportFragmentManager());
                break;

            case SAVING:
                autoSaved = true;
                showIfNotShowing(SaveFormProgressDialogFragment.class, getSupportFragmentManager());
                break;

            case SAVED:
                DialogUtils.dismissDialog(SaveFormProgressDialogFragment.class, getSupportFragmentManager());
                DialogUtils.dismissDialog(ChangesReasonPromptDialogFragment.class, getSupportFragmentManager());

                showShortToast(R.string.data_saved_ok);

                if (result.getRequest().viewExiting()) {
                    if (result.getRequest().shouldFinalize()) {
                        formSubmitManager.scheduleSubmit();
                    }

                    finishAndReturnInstance();
                }
                formSaveViewModel.resumeFormEntry();
                break;

            case SAVE_ERROR:
                DialogUtils.dismissDialog(SaveFormProgressDialogFragment.class, getSupportFragmentManager());
                DialogUtils.dismissDialog(ChangesReasonPromptDialogFragment.class, getSupportFragmentManager());

                String message;

                if (result.getMessage() != null) {
                    message = getString(R.string.data_saved_error) + " "
                            + result.getMessage();
                } else {
                    message = getString(R.string.data_saved_error);
                }

                showLongToast(message);
                formSaveViewModel.resumeFormEntry();
                break;

            case FINALIZE_ERROR:
                DialogUtils.dismissDialog(SaveFormProgressDialogFragment.class, getSupportFragmentManager());
                DialogUtils.dismissDialog(ChangesReasonPromptDialogFragment.class, getSupportFragmentManager());

                showLongToast(String.format(getString(R.string.encryption_error_message),
                        result.getMessage()));
                finishAndReturnInstance();
                formSaveViewModel.resumeFormEntry();
                break;

            case CONSTRAINT_ERROR: {
                DialogUtils.dismissDialog(SaveFormProgressDialogFragment.class, getSupportFragmentManager());
                DialogUtils.dismissDialog(ChangesReasonPromptDialogFragment.class, getSupportFragmentManager());

                onScreenRefresh();

                formSaveViewModel.resumeFormEntry();
                break;
            }
        }
    }

    @Override
    public void onSaveChangesClicked() {
        FormController controller = getFormController();
        if (controller != null) {
            displayFormEndDialog(controller, false);
        }
    }

    @Nullable
    private String getAbsoluteInstancePath() {
        FormController formController = getFormController();
        return formController != null ? formController.getAbsoluteInstancePath() : null;
    }


    /**
     * Creates and displays a dialog allowing the user to set the language for
     * the form.
     */
    private void createLanguageDialog() {
        FormController formController = getFormController();
        final String[] languages = formController.getLanguages();
        int selected = -1;
        if (languages != null) {
            String language = formController.getLanguage();
            for (int i = 0; i < languages.length; i++) {
                if (language.equals(languages[i])) {
                    selected = i;
                }
            }
        }
        alertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(languages, selected,
                        (dialog, whichButton) -> {
                            Form form = formsRepository.getOneByPath(formPath);
                            if (form != null) {
                                formsRepository.save(new Form.Builder(form)
                                        .language(languages[whichButton])
                                        .build()
                                );
                            }

                            getFormController().setLanguage(languages[whichButton]);
                            dialog.dismiss();
                            if (getFormController().currentPromptIsQuestion()) {
                                //saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                            }
                            onScreenRefresh();
                        })
                .setTitle(getString(R.string.change_language))
                .setNegativeButton(getString(R.string.do_not_change), null).create();
        alertDialog.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FormController formController = getFormController();

        // Register to receive location provider change updates and write them to the audit log
        if (formController != null && formController.currentFormAuditsLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(this)) {
            registerReceiver(locationProvidersReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }

        // User may have changed location permissions in Android settings
        if (permissionsProvider.areLocationPermissionsGranted() != locationPermissionsPreviouslyGranted) {
            backgroundLocationViewModel.locationPermissionChanged();
            locationPermissionsPreviouslyGranted = !locationPermissionsPreviouslyGranted;
        }
        activityDisplayed();
    }

    @Override
    protected void onStop() {
        backgroundLocationViewModel.activityHidden();

        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();

        String navigation = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_NAVIGATION);
        showNavigationButtons = navigation.contains(GeneralKeys.NAVIGATION_BUTTONS);

        /*findViewById(R.id.buttonholder).setVisibility(showNavigationButtons ? View.VISIBLE : View.GONE);
        findViewById(R.id.shadow_up).setVisibility(showNavigationButtons ? View.VISIBLE : View.GONE);*/


        if (errorMessage != null) {
            if (alertDialog != null && !alertDialog.isShowing()) {
                createErrorDialog(errorMessage, true);
            } else {
                return;
            }
        }

        FormController formController = getFormController();

        if (formController != null && !formEntryViewModel.isFormControllerSet()) {
            Timber.e("FormController set in App but not ViewModel");
        }

        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(this);
            if (formController == null
                    && formLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                FormController fec = formLoaderTask.getFormController();
                if (fec != null) {
                    if (!readPhoneStatePermissionRequestNeeded) {
                        loadingComplete(formLoaderTask, formLoaderTask.getFormDef(), null);
                    }
                } else {
                    DialogUtils.dismissDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());
                    FormLoaderTask t = formLoaderTask;
                    formLoaderTask = null;
                    t.cancel(true);
                    t.destroy();
                    // there is no formController -- fire MainMenu activity?
                    startActivity(new Intent(this, MainMenuActivity.class));
                }
            }
        } else {
            if (formController == null) {
                // there is no formController -- fire MainMenu activity?
                startActivity(new Intent(this, MainMenuActivity.class));
                finish();
                return;
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        /*
          Make sure the progress dialog is dismissed.
          In most cases that dialog is dismissed in MediaLoadingTask#onPostExecute() but if the app
          is in the background when MediaLoadingTask#onPostExecute() is called then the dialog
          can not be dismissed. In such a case we need to make sure it's dismissed in order
          to avoid blocking the UI.
         */
        if (!mediaLoadingFragment.isMediaLoadingTaskRunning()) {
            Fragment progressDialogFragment =
                    getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);
            if (progressDialogFragment != null) {
                ((DialogFragment) progressDialogFragment).dismiss();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                    // We want the user to stop recording before changing screens
                    //TODO stop recording before submission
                    DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, getSupportFragmentManager());
                    return true;
                }

                showIfNotShowing(QuitFormDialogFragment.class, getSupportFragmentManager());
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(null);
            // We have to call cancel to terminate the thread, otherwise it
            // lives on and retains the FEC in memory.
            // but only if it's done, otherwise the thread never returns
            if (formLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                FormLoaderTask t = formLoaderTask;
                formLoaderTask = null;
                t.cancel(true);
                t.destroy();
            }
        }

        releaseOdkView();
        compositeDisposable.dispose();

        try {
            unregisterReceiver(locationProvidersReceiver);
        } catch (IllegalArgumentException e) {
            // This is the common case -- the form didn't have location audits enabled so the
            // receiver was not registered.
        }

        super.onDestroy();
    }

    private int animationCompletionSet;


    /**
     * Given a {@link FormLoaderTask} which has created a {@link FormController} for either a new or
     * existing instance, shows that instance to the user. Either launches {@link FormHierarchyActivity}
     * if an existing instance is being edited or builds the view for the current question(s) if a
     * new instance is being created.
     * <p>
     * May do some or all of these depending on current state:
     * - Ensures phone state permissions are given if this form needs them
     * - Cleans up {@link #formLoaderTask}
     * - Sets the global form controller and database manager for search()/pulldata()
     * - Restores the last-used language
     * - Handles activity results that may have come in while the form was loading
     * - Alerts the user of a recovery from savepoint
     * - Verifies whether an instance folder exists and creates one if not
     * - Initializes background location capture (only if the instance being loaded is a new one)
     */
    @Override
    public void loadingComplete(FormLoaderTask task, FormDef formDef, String warningMsg) {

        final FormController formController = task.getFormController();

        if (formController != null) {
            if (readPhoneStatePermissionRequestNeeded) {
                permissionsProvider.requestReadPhoneStatePermission(this, true, new PermissionListener() {
                    @Override
                    public void granted() {
                        readPhoneStatePermissionRequestNeeded = false;
                        propertyManager.reload();
                        loadForm();
                    }

                    @Override
                    public void denied() {
                        DialogUtils.dismissDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());

                        finish();

                    }
                });
            } else {
                formLoaderTask.setFormLoaderListener(null);
                FormLoaderTask t = formLoaderTask;
                formLoaderTask = null;
                t.cancel(true);
                t.destroy();

                Collect.getInstance().setFormController(formController);

                backgroundLocationViewModel.formFinishedLoading();
                Collect.getInstance().setExternalDataManager(task.getExternalDataManager());

                // Set the language if one has already been set in the past
                String[] languageTest = formController.getLanguages();
                if (languageTest != null) {
                    String defaultLanguage = formController.getLanguage();
                    Form form = formsRepository.getOneByPath(formPath);

                    if (form != null) {
                        String newLanguage = form.getLanguage();

                        try {
                            formController.setLanguage(newLanguage);
                        } catch (Exception e) {
                            // if somehow we end up with a bad language, set it to the default
                            Timber.i("Ended up with a bad language. %s", newLanguage);
                            formController.setLanguage(defaultLanguage);
                        }
                    }
                }

                boolean pendingActivityResult = task.hasPendingActivityResult();

                if (pendingActivityResult) {
                    Timber.w("Calling onActivityResult from loadingComplete");

                    formControllerAvailable(formController);
                    onScreenRefresh();
                    onActivityResult(task.getRequestCode(), task.getResultCode(), task.getIntent());
                    return;
                }

                // it can be a normal flow for a pending activity result to restore from a savepoint
                // (the call flow handled by the above if statement). For all other use cases, the
                // user should be notified, as it means they wandered off doing other things then
                // returned to ODK Collect and chose Edit Saved Form, but that the savepoint for
                // that form is newer than the last saved version of their form data.
                boolean hasUsedSavepoint = task.hasUsedSavepoint();

                if (hasUsedSavepoint) {
                    runOnUiThread(() -> showLongToast(R.string.savepoint_used));
                }

                if (formController.getInstanceFile() == null) {
                    FormInstanceFileCreator formInstanceFileCreator = new FormInstanceFileCreator(
                            storagePathProvider,
                            System::currentTimeMillis
                    );

                    File instanceFile = formInstanceFileCreator.createInstanceFile(formPath);
                    if (instanceFile != null) {
                        formController.setInstanceFile(instanceFile);
                    } else {
                        DialogUtils.dismissDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());

                        showFormLoadErrorAndExit(getString(R.string.loading_form_failed));
                    }

                    formControllerAvailable(formController);
                    identityPromptViewModel.requiresIdentityToContinue().observe(this, requiresIdentity -> {
                        if (!requiresIdentity) {
                            formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_START, true, System.currentTimeMillis());
                            startFormEntry(formController, warningMsg);
                        }
                    });
                } else {
                    Intent reqIntent = getIntent();
                    boolean showFirst = reqIntent.getBooleanExtra("start", false);

                    if (!showFirst) {
                        // we've just loaded a saved form, so start in the hierarchy view
                        String formMode = reqIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
                        if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                            formControllerAvailable(formController);
                            identityPromptViewModel.requiresIdentityToContinue().observe(this, requiresIdentity -> {
                                if (!requiresIdentity) {
                                    if (!allowMovingBackwards) {
                                        // we aren't allowed to jump around the form so attempt to
                                        // go directly to the question we were on last time the
                                        // form was saved.
                                        // TODO: revisit the fallback. If for some reason the index
                                        // wasn't saved, we can now jump around which doesn't seem right.
                                        FormIndex formIndex = SaveFormIndexTask.loadFormIndexFromFile();
                                        if (formIndex != null) {
                                            formController.jumpToIndex(formIndex);
                                            onScreenRefresh();
                                            return;
                                        }
                                    }

                                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_RESUME, true, System.currentTimeMillis());
                                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true, System.currentTimeMillis());


                                    onScreenRefresh();

                                    //startActivityForResult(new Intent(this, FormHierarchyActivity.class), RequestCodes.HIERARCHY_ACTIVITY);
                                }
                            });

                            formSaveViewModel.editingForm();
                        } else {
                            if (ApplicationConstants.FormModes.VIEW_SENT.equalsIgnoreCase(formMode)) {
                                startActivity(new Intent(this, ViewOnlyFormHierarchyActivity.class));
                            }
                            finish();
                        }
                    } else {
                        formControllerAvailable(formController);
                        identityPromptViewModel.requiresIdentityToContinue().observe(this, requiresIdentity -> {
                            if (!requiresIdentity) {
                                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_RESUME, true, System.currentTimeMillis());
                                startFormEntry(formController, warningMsg);
                            }
                        });
                    }
                }
            }
        } else {
            // DialogUtils.dismissDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());

            Timber.e("FormController is null");
            showLongToast(R.string.loading_form_failed);
            finish();
        }
    }

    private void startFormEntry(FormController formController, String warningMsg) {
        // Register to receive location provider change updates and write them to the audit
        // log. onStart has already run but the formController was null so try again.
        if (formController.currentFormAuditsLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(this)) {
            registerReceiver(locationProvidersReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        }

        // onStart ran before the form was loaded. Let the viewModel know that the activity
        // is about to be displayed and configured. Do this before the refresh actually
        // happens because if audit logging is enabled, the refresh logs a question event
        // and we want that to show up after initialization events.
        activityDisplayed();

        onScreenRefresh();

        if (warningMsg != null) {
            showLongToast(warningMsg);
            Timber.w(warningMsg);
        }
    }

    /**
     * called by the FormLoaderTask if something goes wrong.
     */
    @Override
    public void loadingError(String errorMsg) {
        showFormLoadErrorAndExit(errorMsg);
    }

    private void showFormLoadErrorAndExit(String errorMsg) {
        DialogUtils.dismissDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());

        if (errorMsg != null) {
            createErrorDialog(errorMsg, true);
        } else {
            createErrorDialog(getString(R.string.parse_error), true);
        }
    }

    public void onProgressStep(String stepMessage) {
        showIfNotShowing(FormLoadingDialogFragment.class, getSupportFragmentManager());

        FormLoadingDialogFragment dialog = getDialog(FormLoadingDialogFragment.class, getSupportFragmentManager());
        if (dialog != null) {
            dialog.setMessage(getString(R.string.please_wait) + "\n\n" + stepMessage);
        }
    }


    /**
     * Returns the instance that was just filled out to the calling activity, if
     * requested.
     */
    private void finishAndReturnInstance() {
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_EDIT.equals(action)) {
            // caller is waiting on a picked form
            Uri uri = InstancesDaoHelper.getLastInstanceUri(getAbsoluteInstancePath());
            if (uri != null) {
                setResult(RESULT_OK, new Intent().setData(uri));
            }
        }

        finish();
    }


    @Override
    public void onSavePointError(String errorMessage) {
        if (errorMessage != null && errorMessage.trim().length() > 0) {
            showLongToast(getString(R.string.save_point_error, errorMessage));
        }
    }

    @Override
    public void onSaveFormIndexError(String errorMessage) {
        if (errorMessage != null && errorMessage.trim().length() > 0) {
            showLongToast(getString(R.string.save_point_error, errorMessage));
        }
    }

    @Override
    public void onNumberPickerValueSelected(int widgetId, int value) {

        if (currentView != null) {

            for (QuestionWidget qw : questionWidgetArrayList) {

                if (qw instanceof RangePickerIntegerWidget && widgetId == qw.getId()) {
                    ((RangePickerIntegerWidget) qw).setNumberPickerValue(value);
                    widgetValueChanged(qw);
                    return;
                } else if (qw instanceof RangePickerDecimalWidget && widgetId == qw.getId()) {
                    ((RangePickerDecimalWidget) qw).setNumberPickerValue(value);
                    widgetValueChanged(qw);
                    return;
                }


            }
        }
    }

    @Override
    public void onDateChanged(LocalDateTime selectedDate) {
        onDataChanged(selectedDate);
    }

    @Override
    public void onTimeChanged(DateTime selectedTime) {
        onDataChanged(selectedTime);
    }

    @Override
    public void onRankingChanged(List<SelectChoice> items) {
        onDataChanged(items);
    }

    /*
     *TODO: this is not an ideal way to solve communication between a dialog created by a widget and the widget.
     * Instead we should use viewmodels: https://github.com/getodk/collect/pull/3964#issuecomment-670155433
     */
    @Override
    public void updateSelectedItems(List<Selection> items) {

        if (currentView != null) {
            QuestionWidget widgetGettingNewValue = getWidgetWaitingForBinaryData();
            setWidgetData(items);
            widgetValueChanged(widgetGettingNewValue);
        }
    }

    @Override
    public void onCancelFormLoading() {
        if (formLoaderTask != null) {
            formLoaderTask.setFormLoaderListener(null);
            FormLoaderTask t = formLoaderTask;
            formLoaderTask = null;
            t.cancel(true);
            t.destroy();
        }
        finish();
    }

    private void onDataChanged(Object data) {
        if (currentView != null) {
            QuestionWidget widgetGettingNewValue = getWidgetWaitingForBinaryData();
            setWidgetData(data);
            widgetValueChanged(widgetGettingNewValue);
        }
    }



    private class LocationProvidersReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null
                    && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                backgroundLocationViewModel.locationProvidersChanged();
            }
        }
    }

    private void activityDisplayed() {
        displayUIFor(backgroundLocationViewModel.activityDisplayed());

        if (backgroundLocationViewModel.isBackgroundLocationPermissionsCheckNeeded()) {
            permissionsProvider.requestLocationPermissions(this, new PermissionListener() {
                @Override
                public void granted() {
                    displayUIFor(backgroundLocationViewModel.locationPermissionsGranted());
                }

                @Override
                public void denied() {
                    backgroundLocationViewModel.locationPermissionsDenied();
                }
            });
        }
    }

    /**
     * Displays UI representing the given background location message, if there is one.
     */
    private void displayUIFor(@Nullable BackgroundLocationManager.BackgroundLocationMessage
                                      backgroundLocationMessage) {
        if (backgroundLocationMessage == null) {
            return;
        }

        if (backgroundLocationMessage == BackgroundLocationManager.BackgroundLocationMessage.PROVIDERS_DISABLED) {
            new LocationProvidersDisabledDialog().show(getSupportFragmentManager(), LocationProvidersDisabledDialog.LOCATION_PROVIDERS_DISABLED_DIALOG_TAG);
            return;
        }

        String snackBarText;

        if (backgroundLocationMessage.isMenuCharacterNeeded()) {
            snackBarText = String.format(getString(backgroundLocationMessage.getMessageTextResourceId()), "⋮");
        } else {
            snackBarText = getString(backgroundLocationMessage.getMessageTextResourceId());
        }

        SnackbarUtils.showLongSnackbar(findViewById(R.id.llParent), snackBarText);
    }

    /**
     * A new answer is posted
     * <p>
     * Take a stock of all the questions provided by controller.getQuestionPrompts()
     * <p>
     * Save the answer - this will trigger the FromDef trigger-ables to evaluate the relevance
     * of all the questions in the fieldList(in this case, the whole form)
     * <p>
     * Ask the FormDef again for the new prompts, check the difference - Add/Eject based on the difference
     * <p>
     * Start the factory for any extra questions - don't process all the questions afresh.
     * <p>
     * Inject/Purge any changes on the recycler, notify the changes
     * <p>
     * Done.
     *
     * @param changedWidget
     */
    public void updateQuestionsInViewPerRelevance(QuestionWidget changedWidget) {

        FormController formController = getFormController();

        if (formController == null) return;
        try {

            FormIndex currentIndexForQuestion = changedWidget.getQuestionDetails().getPrompt().getIndex();

            FormIndex previousLevel = currentIndexForQuestion.getPreviousLevel();

            int level = currentIndexForQuestion.getDepth();

            Timber.d("EVALUATING LEVEL: %s for fIndex %s", level, currentIndexForQuestion.toString());

            if (level < 2) {
                pleaseSaveForUs(changedWidget);
                return;
            }

            formController.jumpToIndex(previousLevel);

            FormEntryPrompt[] promptsBeforeSave = formController.getQuestionPrompts();

            List<ImmutableDisplayableQuestion> immutableQuestionsBeforeSave = new ArrayList<>();
            List<ImmutableDisplayableQuestion> immutableQuestionsAfterSave = new ArrayList<>();

            for (FormEntryPrompt questionBeforeSave : promptsBeforeSave) {
                immutableQuestionsBeforeSave.add(new ImmutableDisplayableQuestion(questionBeforeSave));
            }


            pleaseSaveForUs(changedWidget);

            FormEntryPrompt[] promptsAfterSave = formController.getQuestionPrompts();

            for (FormEntryPrompt questionAfterSave : promptsAfterSave) {
                immutableQuestionsAfterSave.add(new ImmutableDisplayableQuestion(questionAfterSave));
            }

            if (immutableQuestionsAfterSave.containsAll(immutableQuestionsBeforeSave)) return;

            Map<FormIndex, FormEntryPrompt> questionsAfterSaveByIndex = new HashMap<>();

            for (FormEntryPrompt question : promptsAfterSave) {
                questionsAfterSaveByIndex.put(question.getIndex(), question);
            }

            List<FormEntryPrompt> questionsThatHaveNotChanged = new ArrayList<>();
            List<FormIndex> formIndexesToRemove = new ArrayList<>();

            for (ImmutableDisplayableQuestion questionBeforeSave : immutableQuestionsBeforeSave) {
                FormEntryPrompt questionAtSameFormIndex = questionsAfterSaveByIndex.get(questionBeforeSave.getFormIndex());

                // Always rebuild questions that use database-driven external data features since they
                // bypass SelectChoices stored in ImmutableDisplayableQuestion
                if (questionBeforeSave.sameAs(questionAtSameFormIndex)
                        && !getFormController().usesDatabaseExternalDataFeature(questionBeforeSave.getFormIndex())) {

                    questionsThatHaveNotChanged.add(questionAtSameFormIndex);

                } else if (!currentIndexForQuestion.equals(questionBeforeSave.getFormIndex())) {
                    formIndexesToRemove.add(questionBeforeSave.getFormIndex());
                }
            }

            ArrayList<FormIndex> indexesToRemoveFinal = new ArrayList<>();

            for (int i = immutableQuestionsBeforeSave.size() - 1; i >= 0; i--) {
                ImmutableDisplayableQuestion questionBeforeSave = immutableQuestionsBeforeSave.get(i);

                if (formIndexesToRemove.contains(questionBeforeSave.getFormIndex())) {

                    ImmutableDisplayableQuestion toEject = immutableQuestionsBeforeSave.get(i);

                    FormIndex indexForItemToRemove = toEject.getFormIndex();

                    indexesToRemoveFinal.add(indexForItemToRemove);

                }
            }

            ArrayList<QuestionWidget> listToModify = questionWidgetArrayList;


            for (int ind = questionWidgetArrayList.size() - 1; ind >= 0; ind--) {
                FormEntryPrompt pointedPrompt = questionWidgetArrayList.get(ind).getQuestionDetails().getPrompt();

                if (indexesToRemoveFinal.contains(pointedPrompt.getIndex())) {

                    int finalInd = ind;
                    readyProcessedQuestions.removeIf(providedEntry -> providedEntry.getIndex() ==
                            questionWidgetArrayList.get(finalInd).getQuestionDetails().getPrompt().getIndex());

                    listToModify.remove(questionWidgetArrayList.get(ind));

                    questionsAdapter.notifyItemRemoved(ind);

                }

            }

            questionWidgetArrayList = listToModify;//after mod.

            ArrayList<QuestionWidget> promptsToBeAdded = new ArrayList<>();

            ArrayList<FormIndex> indexesOfReadyProcessedQns = new ArrayList<>();

            for (FormEntryPrompt entryPrompt : readyProcessedQuestions) {
                indexesOfReadyProcessedQns.add(entryPrompt.getIndex());
            }

            for (FormEntryPrompt formEntryPrompt : promptsAfterSave) {
                if (!questionsThatHaveNotChanged.contains(formEntryPrompt)
                        && !formEntryPrompt.getIndex().equals(currentIndexForQuestion)) {
                    // The values of widgets in intent groups are set by the view so widgetValueChanged
                    // is never called. This means readOnlyOverride can always be set to false.

                    if (!indexesOfReadyProcessedQns.contains(formEntryPrompt.getIndex())) {

                        readyProcessedQuestions.add(formEntryPrompt);

                        QuestionWidget addedQuestion = this.widgetFactory.createWidgetFromPrompt(formEntryPrompt, permissionsProvider);

                        promptsToBeAdded.add(addedQuestion);
                    }
                }
            }


            if (promptsToBeAdded.size() > 0) {
                int positionOfRelatedQsn = questionsAdapter.getPositionForWidget(currentIndexForQuestion);

                if (positionOfRelatedQsn != -1) {

                    for (int c = promptsToBeAdded.size() - 1; c >= 0; c--) {
                        /**
                         * Insertion of new questions
                         * get the index for the new insertion,
                         * check the closeness of its index to other siblings
                         * Insert next to the closest related question.
                         */

                        ArrayList<String> stackOfFamilyIndices = new ArrayList<>();

                        QuestionWidget addition = promptsToBeAdded.get(c);

                        FormIndex insertionQuestionIndex = addition.getQuestionDetails().getPrompt().getIndex();

                        String positionOfCallingWidgetInStack = currentIndexForQuestion.toString();

                        for (FormEntryPrompt qsn : promptsAfterSave) {
                            String toAdd = qsn.getIndex().toString();

                            stackOfFamilyIndices.add(toAdd);

                        }

                        Collections.sort(stackOfFamilyIndices);

                        int positionOfCurrentCallerInStack = stackOfFamilyIndices.indexOf(positionOfCallingWidgetInStack);

                        int relativePositionOfWidgetToBeInserted = stackOfFamilyIndices.indexOf(insertionQuestionIndex.toString());

                        int differentialForNewInsertion = (relativePositionOfWidgetToBeInserted - positionOfCurrentCallerInStack);


                        int indexFactor = positionOfRelatedQsn + differentialForNewInsertion; //positionOfRelatedQsn + indexPointer;

                        if (indexFactor > questionWidgetArrayList.size()) {

                            questionWidgetArrayList.add(questionWidgetArrayList.size() - 1, addition);

                            questionsAdapter.notifyItemInserted(questionWidgetArrayList.size() - 1);

                        } else if (indexFactor < 0) {
                            questionWidgetArrayList.add(positionOfRelatedQsn, addition);

                            questionsAdapter.notifyItemInserted(positionOfRelatedQsn);

                        } else {
                            questionWidgetArrayList.add(indexFactor, addition);

                            questionsAdapter.notifyItemInserted(indexFactor);
                        }

                    }

                }
            }

            //shift the index back to the calling children
            formController.jumpToIndex(currentIndexForQuestion);

        } catch (FormDesignException exception) {
            Timber.e(exception);
        }


    }


    @Override
    public void widgetValueChanged(QuestionWidget changedWidget) {
        FormController formController = getFormController();
        if (formController == null) {
            return;
        }

            runOnUiThread(() -> {
                try {
                    updateQuestionsInViewPerRelevance(changedWidget);

                } catch (Exception e) {
                    Timber.e(e);
                    createErrorDialog(e.getMessage(), false);
                }
            });
        }


    private void pleaseSaveForUs(QuestionWidget widget) {

        IAnswerData selectedAnswer = widget.getAnswer();

        try {
            getFormController().saveAnswer(widget.getQuestionDetails().getPrompt().getIndex(), selectedAnswer);

        } catch (JavaRosaException | NullPointerException exception) {
            Timber.d(exception);
        }

    }

    /**
     * Saves the form and updates displayed widgets accordingly:
     * - removes widgets corresponding to questions that are no longer relevant
     * - adds widgets corresponding to questions that are newly-relevant
     * - removes and rebuilds widgets corresponding to questions that have changed in some way. For
     * example, the question text or hint may have updated due to a value they refer to changing.
     * <p>
     * The widget corresponding to the {@param lastChangedIndex} is never changed.
     */

    private HashMap<FormIndex, IAnswerData> getAnswers() {
        HashMap<FormIndex, IAnswerData> answers = new LinkedHashMap<>();
        if (questionWidgetArrayList.isEmpty()) {
            return new HashMap<>();
        } else {
            for (int i = 0; i < questionWidgetArrayList.size(); i++) {
                FormEntryPrompt formEntry = questionWidgetArrayList.get(i).getFormEntryPrompt();
                answers.put(formEntry.getIndex(), formEntry.getAnswerValue());
            }

            return answers;
        }
    }
}
