package com.ning.http.client.fancy;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncCompletionHandlerBase;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;

import java.util.LinkedHashMap;

public class TypeMapper
{

    private final LinkedHashMap<Class, AsyncHandlerFactory> handlers = new LinkedHashMap<Class, AsyncHandlerFactory>();

    public TypeMapper() {
        handlers.put(Response.class, new AsyncHandlerFactory<Response>() {

            public AsyncHandler<Response> build()
            {
                return new AsyncCompletionHandlerBase();
            }
        });

        handlers.put(String.class, new AsyncHandlerFactory<String>() {

            public AsyncHandler<String> build()
            {
                return new AsyncCompletionHandler<String>() {

                    public String onCompleted(Response response) throws Exception
                    {
                        return response.getResponseBody();
                    }

                    public void onThrowable(Throwable t)
                    {
                        throw new UnsupportedOperationException("Not Yet Implemented!");
                    }
                };
            }
        });

    }

    public <T> AsyncHandler<T> getAsyncHandlerFor(Class<T> crt)
    {
        return handlers.get(crt).build();
    }
}
