package app.nexusforms.android.openrosa;

import org.jetbrains.annotations.Nullable;
import org.kxml2.kdom.Document;
import app.nexusforms.android.forms.FormListItem;
import app.nexusforms.android.forms.MediaFile;

import java.util.List;

public interface OpenRosaResponseParser {

    @Nullable List<FormListItem> parseFormList(Document document);
    @Nullable List<MediaFile> parseManifest(Document document);
}
