package app.nexusforms.collect.android.openrosa;

import android.webkit.MimeTypeMap;

import app.nexusforms.android.openrosa.CollectThenSystemContentTypeMapper;
import app.nexusforms.android.openrosa.OpenRosaHttpInterface;
import app.nexusforms.android.openrosa.okhttp.OkHttpConnection;
import app.nexusforms.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton()),
                USER_AGENT
        );
    }
}
