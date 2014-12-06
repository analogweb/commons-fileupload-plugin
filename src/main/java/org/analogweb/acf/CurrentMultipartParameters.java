package org.analogweb.acf;

import org.analogweb.Multipart;
import org.analogweb.RequestContext;

class CurrentMultipartParameters {

    static final String ATTRIBUTE_NAME = CurrentMultipartParameters.class.getCanonicalName() + "";

    @SuppressWarnings("unchecked")
    static <T extends Multipart> MultipartParameters<T> get(RequestContext request) {
        final Object value = request.getAttribute(ATTRIBUTE_NAME);
        if (value instanceof MultipartParameters) {
            return (MultipartParameters<T>) value;
        }
        return null;
    }

    static <T extends Multipart> void put(RequestContext request, MultipartParameters<T> params) {
        request.setAttribute(ATTRIBUTE_NAME, params);
    }

    static void dispose(RequestContext request) {
        request.setAttribute(ATTRIBUTE_NAME, null);
    }
}
