/*
 * Copyright 2019 Nafundi
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

package app.nexusforms.android.utilities;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import app.nexusforms.android.utilities.FormNameUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;

public class FormNameUtilsTest {

    @Test
    public void normalizeFormNameTest() {
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName(null, false), is(nullValue()));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem", false), is("Lorem"));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem ipsum", false), is("Lorem ipsum"));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem\nipsum", false), is("Lorem ipsum"));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem\n\nipsum", false), is("Lorem  ipsum"));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("\nLorem\nipsum\n", false), is(" Lorem ipsum "));

        MatcherAssert.assertThat(FormNameUtils.normalizeFormName(null, true), is(nullValue()));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem", true), is(nullValue()));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem ipsum", true), is(nullValue()));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem\nipsum", true), is("Lorem ipsum"));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("Lorem\n\nipsum", true), is("Lorem  ipsum"));
        MatcherAssert.assertThat(FormNameUtils.normalizeFormName("\nLorem\nipsum\n", true), is(" Lorem ipsum "));
    }

    @Test
    public void formatFilenameFromFormNameTest() {
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName(null), is(nullValue()));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("simple"), is("simple"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("CamelCase"), is("CamelCase"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("01234566789"), is("01234566789"));

        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName(" trimWhitespace "), is("trimWhitespace"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("keep internal spaces"), is("keep internal spaces"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("other\n\twhitespace"), is("other whitespace"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("repeated         whitespace"), is("repeated whitespace"));

        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("Turkish İ kept"), is("Turkish İ kept"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("registered symbol ® stripped"), is("registered symbol stripped"));
        MatcherAssert.assertThat(FormNameUtils.formatFilenameFromFormName("unicode fragment \ud800 stripped"), is("unicode fragment stripped"));
    }
}
