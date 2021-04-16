package app.nexusforms.collect.android.formentry.audit;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.junit.Assert;
import org.junit.Test;

import app.nexusforms.android.formentry.audit.AuditEvent;
import app.nexusforms.android.formentry.audit.AuditEventCSVLine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AuditEventCSVLineTest {

    private static final long START_TIME = 1545392727685L;
    private static final long END_TIME = 1545392728527L;

    //region CSV spec (https://tools.ietf.org/html/rfc4180)
    @Test
    public void commas_shouldBeSurroundedByQuotes() {
        AuditEvent auditEvent = new AuditEvent(1L, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "a, b", "c, d", "e, f");
        auditEvent.recordValueChange("g, h");
        auditEvent.setEnd(2L);
        String csvLine = AuditEventCSVLine.toCSVLine(auditEvent, false, true, true);
        assertThat(csvLine, is("question,/data/text1,1,2,\"a, b\",\"g, h\",\"c, d\",\"e, f\""));
    }

    @Test
    public void newlines_shouldBeSurroundedByQuotes() {
        AuditEvent auditEvent = new AuditEvent(1L, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "a\nb", "c\nd", "e\nf");
        auditEvent.recordValueChange("g\nh");
        auditEvent.setEnd(2L);
        String csvLine = AuditEventCSVLine.toCSVLine(auditEvent, false, true, true);
        assertThat(csvLine, is("question,/data/text1,1,2,\"a\nb\",\"g\nh\",\"c\nd\",\"e\nf\""));
    }

    @Test
    public void quotes_shouldBeEscaped_andSurroundedByQuotes() {
        AuditEvent auditEvent = new AuditEvent(1L, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "a\"b", "c\"d", "e\"f");
        auditEvent.recordValueChange("g\"h");
        auditEvent.setEnd(2L);
        String csvLine = AuditEventCSVLine.toCSVLine(auditEvent, false, true, true);
        assertThat(csvLine, is("question,/data/text1,1,2,\"a\"\"b\",\"g\"\"h\",\"c\"\"d\",\"e\"\"f\""));
    }
    //endregion

    @Test
    public void toString_() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "", null, null);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("question,/data/text1,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        Assert.assertEquals("question,/data/text1,1545392727685,1545392728527", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));
    }

    @Test
    public void toString_withLocationCoordinates() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "", null, null);
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertTrue(auditEvent.isLocationAlreadySet());
        Assert.assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10", AuditEventCSVLine.toCSVLine(auditEvent, true, false, false));
    }

    @Test
    public void toString_withTrackingChanges() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "First answer", null, null);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("Second answer");
        Assert.assertEquals("question,/data/text1,1545392727685,1545392728527,First answer,Second answer", AuditEventCSVLine.toCSVLine(auditEvent, false, true, false));
    }

    @Test
    public void toString_withLocationCoordinates_andTrackingChanges() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "First answer", null, null);
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("54.35202520000001", "18.64663840000003", "10");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertTrue(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("Second, answer");
        Assert.assertEquals("question,/data/text1,1545392727685,1545392728527,54.35202520000001,18.64663840000003,10,First answer,\"Second, answer\"", AuditEventCSVLine.toCSVLine(auditEvent, true, true, false));
    }

    @Test
    public void toStringNullValues() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.QUESTION, getTestFormIndex(), "Old value", null, null);
        assertNotNull(auditEvent);
        auditEvent.setLocationCoordinates("", "", "");
        assertTrue(auditEvent.isIntervalAuditEventType());
        assertFalse(auditEvent.isEndTimeSet());
        auditEvent.setEnd(END_TIME);
        assertTrue(auditEvent.isEndTimeSet());
        assertFalse(auditEvent.isLocationAlreadySet());
        auditEvent.recordValueChange("New value");
        Assert.assertEquals("question,/data/text1,1545392727685,1545392728527,,,,Old value,New value", AuditEventCSVLine.toCSVLine(auditEvent, true, true, false));
    }

    @Test
    public void testEventTypes() {
        AuditEvent auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.QUESTION);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("question,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.FORM_START);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("form start,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.END_OF_FORM);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("end screen,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.REPEAT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("repeat,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.PROMPT_NEW_REPEAT);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("add repeat,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.GROUP);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("group questions,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.BEGINNING_OF_FORM);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("beginning of form,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.FORM_EXIT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("form exit,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.FORM_RESUME);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("form resume,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.FORM_SAVE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("form save,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.FORM_FINALIZE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("form finalize,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.HIERARCHY);
        assertNotNull(auditEvent);
        assertTrue(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("jump,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.SAVE_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("save error,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.FINALIZE_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("finalize error,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.CONSTRAINT_ERROR);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("constraint error,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.DELETE_REPEAT);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("delete repeat,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("google play services not available,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.LOCATION_PERMISSIONS_GRANTED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("location permissions granted,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.LOCATION_PERMISSIONS_NOT_GRANTED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("location permissions not granted,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.LOCATION_TRACKING_ENABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("location tracking enabled,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("location tracking disabled,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("location providers enabled,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("location providers disabled,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));

        auditEvent = new AuditEvent(START_TIME, AuditEvent.AuditEventType.UNKNOWN_EVENT_TYPE);
        assertNotNull(auditEvent);
        assertFalse(auditEvent.isIntervalAuditEventType());
        Assert.assertEquals("Unknown AuditEvent Type,,1545392727685,", AuditEventCSVLine.toCSVLine(auditEvent, false, false, false));
    }

    private FormIndex getTestFormIndex() {
        TreeReference treeReference = new TreeReference();
        treeReference.add("data", 0);
        treeReference.add("text1", 0);

        return new FormIndex(0, treeReference);
    }
}