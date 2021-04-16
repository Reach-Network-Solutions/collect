/*
 * Copyright (C) 2018 Nafundi
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

package app.nexusforms.android.gdrive;

import app.nexusforms.analytics.Analytics;
import org.odk.collect.android.R;

import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.application.Collect;
import app.nexusforms.android.database.DatabaseFormsRepository;
import app.nexusforms.android.instances.Instance;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.upload.UploadException;
import app.nexusforms.android.utilities.InstanceUploaderUtils;
import app.nexusforms.android.utilities.TranslationHandler;
import app.nexusforms.android.forms.Form;
import app.nexusforms.android.tasks.InstanceUploaderTask;

import java.util.List;

import timber.log.Timber;

public class InstanceGoogleSheetsUploaderTask extends InstanceUploaderTask {

    private final GoogleApiProvider googleApiProvider;
    private final Analytics analytics;

    public InstanceGoogleSheetsUploaderTask(GoogleApiProvider googleApiProvider, Analytics analytics) {
        this.googleApiProvider = googleApiProvider;
        this.analytics = analytics;
    }

    @Override
    protected Outcome doInBackground(Long... instanceIdsToUpload) {
        String account = settingsProvider
                .getGeneralSettings()
                .getString(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT);

        InstanceGoogleSheetsUploader uploader = new InstanceGoogleSheetsUploader(googleApiProvider.getDriveApi(account), googleApiProvider.getSheetsApi(account));
        final Outcome outcome = new Outcome();

        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        for (int i = 0; i < instancesToUpload.size(); i++) {
            Instance instance = instancesToUpload.get(i);

            if (isCancelled()) {
                outcome.messagesByInstanceId.put(instance.getId().toString(),
                        TranslationHandler.getString(Collect.getInstance(), R.string.instance_upload_cancelled));
                return outcome;
            }

            publishProgress(i + 1, instancesToUpload.size());

            // Get corresponding blank form and verify there is exactly 1
            List<Form> forms = new DatabaseFormsRepository().getAllByFormIdAndVersion(instance.getJrFormId(), instance.getJrVersion());

            if (forms.size() != 1) {
                outcome.messagesByInstanceId.put(instance.getId().toString(),
                        TranslationHandler.getString(Collect.getInstance(), R.string.not_exactly_one_blank_form_for_this_form_id));
            } else {
                try {
                    String destinationUrl = uploader.getUrlToSubmitTo(instance, null, null, settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_GOOGLE_SHEETS_URL));
                    if (InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                        uploader.uploadOneSubmission(instance, destinationUrl);
                        outcome.messagesByInstanceId.put(instance.getId().toString(), InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT);

                        analytics.logEvent(AnalyticsEvents.SUBMISSION, "HTTP-Sheets", Collect.getFormIdentifierHash(instance.getJrFormId(), instance.getJrVersion()));
                    } else {
                        outcome.messagesByInstanceId.put(instance.getId().toString(), InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                    }
                } catch (UploadException e) {
                    Timber.d(e);
                    outcome.messagesByInstanceId.put(instance.getId().toString(),
                            e.getDisplayMessage());
                }
            }
        }
        return outcome;
    }
}
