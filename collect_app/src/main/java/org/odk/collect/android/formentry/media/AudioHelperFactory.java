package org.odk.collect.android.formentry.media;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;

import org.odk.collect.android.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
