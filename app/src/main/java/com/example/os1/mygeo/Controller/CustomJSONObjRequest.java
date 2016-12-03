package com.example.os1.mygeo.Controller;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by OS1 on 23.08.2016.
 */
public class CustomJSONObjRequest extends Request<JSONObject> {

    private static final String LOG_TAG = "myLogs";

    private Response.Listener<JSONObject> listener;
    private Map<String, String> params;

    public CustomJSONObjRequest(int method, String url, Map<String, String> params, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);

        this.listener = responseListener;
        this.params   = params;
    }

    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
        return params;
    };

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            // Log.d(LOG_TAG, "--> CustomJSONObjRequest: parseNetworkResponse: jsonString=" +jsonString);

            JSONObject dataJSONObject = new JSONObject(jsonString);

            return Response.success(dataJSONObject, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {

            // Log.d(LOG_TAG, "--> CustomJSONObjRequest: UnsupportedEncodingException");

            return Response.error(new ParseError(e));
        } catch (JSONException je) {

            // Log.d(LOG_TAG, "--> CustomJSONObjRequest: JSONException");

            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        // TODO Auto-generated method stub
        listener.onResponse(response);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError){

        // Log.d(LOG_TAG, "--> CustomJSONObjRequest: parseNetworkError: " +volleyError.toString());
        // Log.d(LOG_TAG, "--> CustomJSONObjRequest: volleyError.networkResponse is null " +(volleyError.networkResponse == null));

        if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){

            // Log.d(LOG_TAG, "--> CustomJSONObjRequest: volleyError.networkResponse.data is null " +(volleyError.networkResponse.data == null));
            // Log.d(LOG_TAG, "--> CustomJSONObjRequest: volleyError.networkResponse.data= " +volleyError.networkResponse.data.toString());

            VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
            volleyError = error;
        }

        return volleyError;
    }
}