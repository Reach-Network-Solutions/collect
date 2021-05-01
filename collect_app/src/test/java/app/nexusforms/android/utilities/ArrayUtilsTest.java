package app.nexusforms.android.utilities;

import org.junit.Assert;
import org.junit.Test;

import app.nexusforms.android.utilities.ArrayUtils;

import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilsTest {

    @Test
    public void toPrimitiveCreatesPrimitiveLongArray() throws Exception {
        Assert.assertArrayEquals(new long[] {1, 2, 3, 4, 5}, ArrayUtils.toPrimitive(new Long[] {1L, 2L, 3L, 4L, 5L}));
    }

    @Test
    public void nullToPrimitiveCreatesEmptyPrimitiveLongArray() throws Exception {
        Assert.assertArrayEquals(new long[0], ArrayUtils.toPrimitive(null));
    }

    @Test(expected = NullPointerException.class)
    public void arrayContainingNullCausesNpe() {
        ArrayUtils.toPrimitive(new Long[] {1L, null, 3L, 4L, 5L});
    }

    @Test(expected = NullPointerException.class)
    public void arrayStartingWithNullCausesNpe() {
        ArrayUtils.toPrimitive(new Long[] {null, 3L, 4L, 5L});
    }

    @Test(expected = NullPointerException.class)
    public void arrayEndingWithNullCausesNpe() {
        ArrayUtils.toPrimitive(new Long[] {1L, 3L, 4L, null});
    }

    @Test
    public void toObjectCreatesLongArray() throws Exception {
        Assert.assertArrayEquals(new Long[] {1L, 2L, 3L, 4L, 5L}, ArrayUtils.toObject(new long[] {1, 2, 3, 4, 5}));
    }

    @Test
    public void nullBecomesEmptyLongArray() throws Exception {
        Assert.assertArrayEquals(new Long[0], ArrayUtils.toObject(null));
    }
}
