package org.analogweb.acf;

import java.io.IOException;
import java.io.InputStream;

import org.analogweb.RequestContext;

/**
 * @author snowgooseyk
 */
public class AnalogwebRequestContext implements org.apache.commons.fileupload.RequestContext {

    private final org.analogweb.RequestContext request;

    public AnalogwebRequestContext(RequestContext request) {
        this.request = request;
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return request.getContentType().toString();
    }

    @Override
    public int getContentLength() {
        return (int) request.getContentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getRequestBody().asInputStream();
    }
}
