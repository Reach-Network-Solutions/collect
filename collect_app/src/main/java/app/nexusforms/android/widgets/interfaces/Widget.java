package app.nexusforms.android.widgets.interfaces;

import org.javarosa.core.model.data.IAnswerData;

/**
 * @author James Knight
 */
public interface Widget {

    IAnswerData getAnswer();

    void clearAnswer();
}
