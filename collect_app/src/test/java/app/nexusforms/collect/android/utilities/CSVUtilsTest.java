package app.nexusforms.collect.android.utilities;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import app.nexusforms.android.utilities.CSVUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CSVUtilsTest {
    @Test
    public void null_shouldBePassedThrough() {
        MatcherAssert.assertThat(CSVUtils.getEscapedValueForCsv(null), is(nullValue()));
    }

    @Test
    public void stringsWithoutQuotesCommasOrNewlines_shouldBePassedThrough() {
        MatcherAssert.assertThat(CSVUtils.getEscapedValueForCsv("a b c d e"), is("a b c d e"));
    }

    @Test
    public void quotes_shouldBeEscaped_andSurroundedByQuotes() {
        MatcherAssert.assertThat(CSVUtils.getEscapedValueForCsv("a\"b\""), is("\"a\"\"b\"\"\""));
    }

    @Test
    public void commas_shouldBeSurroundedByQuotes() {
        MatcherAssert.assertThat(CSVUtils.getEscapedValueForCsv("a,b"), is("\"a,b\""));
    }

    @Test
    public void newlines_shouldBeSurroundedByQuotes() {
        MatcherAssert.assertThat(CSVUtils.getEscapedValueForCsv("a\nb"), is("\"a\nb\""));
    }
}
