/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package com.ning.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public interface Request {

    public static interface EntityWriter {
        public void writeEntity(OutputStream out) throws IOException;
    }

    public RequestType getType();

    /**
     * Return the decoded url
     * @return  the decoded url
     */
    public String getUrl();

    /**
     * Return the undecoded url
     * @return the undecoded url
     */
    public String getRawUrl();

    public FluentCaseInsensitiveStringsMap getHeaders();

    public Collection<Cookie> getCookies();

    public byte[] getByteData();

    public String getStringData();

    public InputStream getStreamData();

    public EntityWriter getEntityWriter();

    public long getLength();

    public FluentStringsMap getParams();

    public List<Part> getParts();

    public String getVirtualHost();

    public FluentStringsMap getQueryParams();

    public ProxyServer getProxyServer();
}
