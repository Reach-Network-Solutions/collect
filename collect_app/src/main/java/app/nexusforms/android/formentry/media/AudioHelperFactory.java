package app.nexusforms.android.formentry.media;

import android.content.Context;

import app.nexusforms.android.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
