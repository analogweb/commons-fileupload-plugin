package org.analogweb.acf;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.Multipart;

class CurrentMultipartParameters {

    static final String ATTRIBUTE_NAME = CurrentMultipartParameters.class.getCanonicalName() + "";

    @SuppressWarnings("unchecked")
    static <T extends Multipart> MultipartParameters<T> get(HttpServletRequest request) {
        Object value = request.getAttribute(ATTRIBUTE_NAME);
        if (value instanceof MultipartParameters) {
            return (MultipartParameters<T>) value;
        }
        return null;
    }

    static <T extends Multipart> void put(HttpServletRequest request, MultipartParameters<T> params) {
        request.setAttribute(ATTRIBUTE_NAME, params);
    }

    static void dispose(HttpServletRequest request) {
        request.removeAttribute(ATTRIBUTE_NAME);
    }

}
