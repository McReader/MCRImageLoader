package by.grsu.mcreader.mcrimageloader.imageloader.http;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

public class HttpWorker {

    public static final String LOG_TAG = HttpWorker.class.getSimpleName();

    private static final String UTF_8 = "UTF_8";
    private static final int SO_TIMEOUT = 20000;

    private HttpClient mClient;

    public HttpWorker() {

        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, UTF_8);

        params.setBooleanParameter("http.protocol.expect-continue", false);

        HttpConnectionParams.setConnectionTimeout(params, SO_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);

        // REGISTERS SCHEMES FOR BOTH HTTP AND HTTPS
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        registry.register(new Scheme("https", sslSocketFactory, 443));

        mClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);

    }

    public InputStream getStream(String source) {

        if (TextUtils.isEmpty(source)) {

            return null;

        }

        HttpGet request = new HttpGet(source);

        HttpResponse response;
        InputStream inputStream = null;
        BufferedHttpEntity httpEntity = null;

        try {

            response = mClient.execute(request);

            if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

                Log.e(LOG_TAG, response == null ? "UNUSUAL ERROR!" : EntityUtils.toString(response.getEntity()));

                return null;

            }

            httpEntity = new BufferedHttpEntity(response.getEntity());

            inputStream = httpEntity.getContent();

        } catch (ClientProtocolException protocolException) {

            handleError(protocolException, request, inputStream);

        } catch (IOException e) {

            handleError(e, request, inputStream);

        } finally {

            try {

                if (httpEntity != null) {

                    httpEntity.consumeContent();

                }

            } catch (IOException e) {

                // can be ignored

            }

        }

        return inputStream;

    }

    private void handleError(Exception e, HttpGet request, InputStream inputStream) {

        Log.e(LOG_TAG, e == null ? "UNUSUAL ERROR!" : e.getMessage());

        if (request != null) {

            request.abort();

        }

        IOUtils.closeStream(inputStream);

    }

}
