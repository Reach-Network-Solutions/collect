package app.nexusforms.android.widgets.interfaces;

import android.content.Context;

import org.javarosa.form.api.FormEntryPrompt;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;

public interface GeoDataRequester {

    void requestGeoPoint(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry);

    void requestGeoShape(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry);

    void requestGeoTrace(Context context, FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry);
}
