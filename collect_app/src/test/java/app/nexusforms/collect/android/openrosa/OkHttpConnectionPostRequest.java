package app.nexusforms.collect.android.openrosa;

import app.nexusforms.android.openrosa.OpenRosaHttpInterface;
import app.nexusforms.android.openrosa.okhttp.OkHttpConnection;
import app.nexusforms.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import okhttp3.OkHttpClient;

public class OkHttpConnectionPostRequest extends OpenRosaPostRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject(OpenRosaHttpInterface.FileToContentTypeMapper mapper) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                mapper,
                "Test Agent"
        );
    }
}
