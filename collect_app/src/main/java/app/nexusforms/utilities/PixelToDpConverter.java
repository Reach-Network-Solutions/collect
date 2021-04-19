package app.nexusforms.utilities;

import android.content.Context;

public class PixelToDpConverter {

    public int convertToDp(int input, Context context) { // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (input * scale + 0.5f);
    }
}
