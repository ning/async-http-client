package com.ning.http.client.fancy;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.RequestType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class FancyClientBuilder
{
    private final AsyncHttpClient client;
    private final TypeMapper mapper;

    public FancyClientBuilder(AsyncHttpClient client)
    {
        this.client = client;
        mapper = new TypeMapper();
    }

    public <T> T build(Class<T> clientClass)
    {
        final LinkedHashMap<String, Handler> urls = new LinkedHashMap<String, Handler>();
        final String base;
        if (clientClass.isAnnotationPresent(BaseURL.class)) {
            base = clientClass.getAnnotation(BaseURL.class).value();
        }
        else {
            base = "";
        }

        for (final Method method : clientClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GET.class)) {
                final GET get = method.getAnnotation(GET.class);
                final String url = base + get.value();

                final Class crt;
                final Type rt = method.getGenericReturnType();
                if (rt instanceof ParameterizedType) {
                    final ParameterizedType prt = (ParameterizedType) rt;
                    final Type[] generics = prt.getActualTypeArguments();
                    if (generics.length != 1 || !(generics[0] instanceof Class)) {
                        throw new UnsupportedOperationException("Not Yet Implemented!");
                    }
                    crt = (Class) generics[0];
                }
                else {
                    throw new UnsupportedOperationException("Not Yet Implemented!");
                }


                final Request template = new RequestBuilder(RequestType.GET)
                    .setUrl(url)
                    .build();

                urls.put(method.toGenericString(), new Handler(template, mapper.getAsyncHandlerFor(crt), method));
            }
        }

        return (T) Proxy.newProxyInstance(clientClass.getClassLoader(),
                                          new Class[]{clientClass},
                                          new InvocationHandler()
                                          {
                                              public Object invoke(Object o, Method method, Object[] objects) throws Throwable
                                              {
                                                  if (urls.containsKey(method.toGenericString())) {
                                                      return urls.get(method.toGenericString()).handle(objects);
                                                  }
                                                  else {
                                                      return method.invoke(o, objects);
                                                  }
                                              }
                                          });
    }


    private class Handler
    {
        private final Request template;
        private final AsyncHandler handler;
        private final Map<Integer, ParamWriter> params = new LinkedHashMap<Integer, ParamWriter>();

        private Handler(Request template, AsyncHandler handler, Method m)
        {
            this.template = template;
            this.handler = handler;

            Class[] param_types = m.getParameterTypes();
            Annotation[][] param_annos = m.getParameterAnnotations();
            for (int i = 0; i < param_annos.length; i++) {
                Annotation[] annos = param_annos[i];
                for (Annotation anno : annos) {
                    if (anno instanceof QueryParam) {
                        QueryParam qp = (QueryParam) anno;
                        final String name = qp.value();

                        if (param_types[i].isArray()) {
                            params.put(i, new ParamWriter()
                            {
                                public void write(AsyncHttpClient.BoundRequestBuilder r, Object arg)
                                {
                                    for (int i = 0; i < Array.getLength(arg); i++) {
                                        r.addQueryParameter(name, String.valueOf(Array.get(arg, i)));
                                    }
                                }
                            });
                        }
                        else if (param_types[i].isAssignableFrom(Iterable.class)) {
                            params.put(i, new ParamWriter()
                            {
                                public void write(AsyncHttpClient.BoundRequestBuilder r, Object arg)
                                {
                                    for (Object it : (Iterable)arg) {
                                        r.addQueryParameter(name, String.valueOf(it));
                                    }
                                }
                            });
                        }
                        else {
                            params.put(i, new ParamWriter()
                            {
                                public void write(AsyncHttpClient.BoundRequestBuilder r, Object arg)
                                {
                                    r.addQueryParameter(name, String.valueOf(arg));
                                }
                            });
                        }
                    }
                }
            }
        }

        Object handle(Object[] args) throws IOException
        {
            AsyncHttpClient.BoundRequestBuilder r = client.prepareRequest(template);

            for (Map.Entry<Integer, ParamWriter> entry : params.entrySet()) {
                entry.getValue().write(r, args[entry.getKey()]);
            }

            return r.execute(handler);
        }
    }

    private interface ParamWriter
    {
        void write(AsyncHttpClient.BoundRequestBuilder r, Object arg);
    }
}
