/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.jaxrs.filter.cookie;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Priority(Priorities.HEADER_DECORATOR)
public class ClientCookieSupportFilter implements ClientResponseFilter, ClientRequestFilter {

    private final Map<String, String> cookies = new LinkedHashMap<>();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (!cookies.isEmpty()) {
            Stream<String> stream = cookies.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue());
            requestContext
                .getHeaders()
                .add("Cookie", String.join("; ", (Iterable<String>) stream::iterator));
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Map<String, NewCookie> newCookies = responseContext.getCookies();
        if(newCookies != null && !newCookies.isEmpty()) {
            long now = System.currentTimeMillis();
            for (NewCookie cookie : newCookies.values()) {
                if(cookie.getMaxAge() == 0 || cookie.getExpiry() != null && cookie.getExpiry().getTime() <= now) {
                    this.cookies.remove(cookie.getName());
                } else {
                    this.cookies.put(cookie.getName(), cookie.getValue());
                }
            }
        }
    }

}
