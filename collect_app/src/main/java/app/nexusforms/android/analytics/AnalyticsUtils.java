package app.nexusforms.android.analytics;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.forms.FormSourceException;

import static java.lang.String.format;
import static app.nexusforms.android.forms.FormSourceException.AuthRequired;
import static app.nexusforms.android.forms.FormSourceException.FetchError;
import static app.nexusforms.android.forms.FormSourceException.ParseError;
import static app.nexusforms.android.forms.FormSourceException.SecurityError;
import static app.nexusforms.android.forms.FormSourceException.ServerError;
import static app.nexusforms.android.forms.FormSourceException.Unreachable;

public class AnalyticsUtils {

    private AnalyticsUtils() {

    }

    public static void logMatchExactlyCompleted(Analytics analytics, FormSourceException exception) {
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, getFormSourceExceptionAction(exception));
    }

    private static String getFormSourceExceptionAction(FormSourceException exception) {
        if (exception == null) {
            return "Success";
        } else if (exception instanceof Unreachable) {
            return "UNREACHABLE";
        } else if (exception instanceof AuthRequired) {
            return "AUTH_REQUIRED";
        } else if (exception instanceof ServerError) {
            return format("SERVER_ERROR_%s", ((ServerError) exception).getStatusCode());
        } else if (exception instanceof SecurityError) {
            return "SECURITY_ERROR";
        } else if (exception instanceof ParseError) {
            return "PARSE_ERROR";
        } else if (exception instanceof FetchError) {
            return "FETCH_ERROR";
        } else {
            throw new IllegalArgumentException();
        }
    }
}
