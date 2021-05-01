package app.nexusforms.android.formentry.audit;

import app.nexusforms.android.javarosawrapper.FormController;

import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;

public class AuditUtils {

    private AuditUtils() {

    }

    public static void logCurrentScreen(FormController formController, AuditEventLogger auditEventLogger, long currentTime) {
        if (formController.getEvent() == EVENT_QUESTION
                || formController.getEvent() == EVENT_GROUP
                || formController.getEvent() == EVENT_REPEAT) {
//            try {
//                FormEntryPrompt[] prompts = formController.getQuestionPrompts();
//                for (FormEntryPrompt question : prompts) {
//                    String answer = question.getAnswerValue() != null ? question.getAnswerValue().getDisplayText() : null;
//                    auditEventLogger.logEvent(AuditEvent.AuditEventType.QUESTION, question.getIndex(), true, answer, currentTime, null);
//                }
//            } catch (FormDesignException e) {
//                throw new RuntimeException(e.getMessage(), e);
//            }
        }
    }
}
