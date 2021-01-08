package com.adityamhatre.bookingscheduler.service

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser


class FileRequest(
    mUrl: String,
    listener: Response.Listener<ByteArray>,
    errorListener: Response.ErrorListener
) : Request<ByteArray?>(Method.GET, mUrl, errorListener) {
    private val mListener: Response.Listener<ByteArray>
    private val mErrorListener: Response.ErrorListener

    override fun deliverResponse(response: ByteArray?) {
        if (response == null) {
            deliverError(VolleyError("Could not get file"))
            return
        }
        mListener.onResponse(response)
    }

    override fun deliverError(error: VolleyError?) {
        mErrorListener.onErrorResponse(error)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<ByteArray?>? {
        return if (response.statusCode == 200) {
            Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response))
        } else {
            Response.error(VolleyError(response.statusCode.toString()))
        }
    }

    init {
        setShouldCache(false)
        mListener = listener
        mErrorListener = errorListener
    }
}