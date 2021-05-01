/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.nexusforms.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import app.nexusforms.android.R;

import app.nexusforms.android.formmanagement.FormDownloadException;
import app.nexusforms.android.formmanagement.FormDownloader;
import app.nexusforms.android.formmanagement.ServerFormDetails;
import app.nexusforms.android.formmanagement.ServerFormsDetailsFetcher;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.notifications.Notifier;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.utilities.TranslationHandler;
import app.nexusforms.async.TaskSpec;
import app.nexusforms.async.WorkerAdapter;
import app.nexusforms.android.forms.FormSourceException;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import static app.nexusforms.android.preferences.keys.GeneralKeys.KEY_AUTOMATIC_UPDATE;

public class AutoUpdateTaskSpec implements TaskSpec {

    @Inject
    ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    @Inject
    FormDownloader formDownloader;

    @Inject
    Notifier notifier;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    @Named("FORMS")
    ChangeLock changeLock;

    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            try {
                List<ServerFormDetails> serverForms = serverFormsDetailsFetcher.fetchFormDetails();
                List<ServerFormDetails> updatedForms = serverForms.stream().filter(ServerFormDetails::isUpdated).collect(Collectors.toList());

                if (!updatedForms.isEmpty()) {
                    if (settingsProvider.getGeneralSettings().getBoolean(KEY_AUTOMATIC_UPDATE)) {
                        changeLock.withLock(acquiredLock -> {
                            if (acquiredLock) {
                                HashMap<ServerFormDetails, String> results = new HashMap<>();
                                for (ServerFormDetails serverFormDetails : updatedForms) {
                                    try {
                                        formDownloader.downloadForm(serverFormDetails, null, null);
                                        results.put(serverFormDetails, TranslationHandler.getString(context, R.string.success));
                                    } catch (FormDownloadException e) {
                                        results.put(serverFormDetails, TranslationHandler.getString(context, R.string.failure));
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }

                                notifier.onUpdatesDownloaded(results);
                            }

                            return null;
                        });
                    } else {
                        notifier.onUpdatesAvailable(updatedForms);
                    }
                }

                return true;
            } catch (FormSourceException e) {
                return true;
            }
        };
    }

    @NotNull
    @Override
    public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
        return Adapter.class;
    }

    public static class Adapter extends WorkerAdapter {

        public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new AutoUpdateTaskSpec(), context, workerParams);
        }
    }

}