package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.acf.FileItemIteratorMultipartParameters;
import org.analogweb.acf.FileItemIteratorRequestContextFactory;
import org.analogweb.acf.FileUploadFactory;
import org.analogweb.acf.FileUploadFailureException;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FileItemStreamRequestContextFactoryTest {

    private FileItemIteratorRequestContextFactory factory;
    private FileUploadFactory<FileUpload> fileUploadFactory;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        factory = new FileItemIteratorRequestContextFactory();
        fileUploadFactory = mock(FileUploadFactory.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        servletContext = mock(ServletContext.class);
    }

    @Test
    public void testCreateRequestContext() throws Exception {

        // multi-part request.
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data");

        final FileItemIterator iterator = mock(FileItemIterator.class);
        FileUpload upload = mock(FileUpload.class);
        when(upload.getItemIterator(isA(RequestContext.class))).thenReturn(iterator);
        when(fileUploadFactory.createFileUpload()).thenReturn(upload);
        factory.setFileUploadFactory(fileUploadFactory);
        org.analogweb.RequestContext actual = factory.createRequestContext(servletContext, request,
                response);
        HttpServletRequest actualRequest = actual.getRequest();

        assertTrue(actualRequest instanceof MultipartHttpServletRequest);

        MultipartHttpServletRequest actualMultipartRequest = (MultipartHttpServletRequest) actualRequest;
        assertTrue(actualMultipartRequest.getMultipartParameters() instanceof FileItemIteratorMultipartParameters);

    }

    @Test
    public void testCreateRequestContextWithoutMultipartRequest() throws Exception {

        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        org.analogweb.RequestContext actual = factory.createRequestContext(servletContext, request,
                response);

        assertThat(actual.getRequest(), is(request));
    }

    @Test
    public void testCreateRequestContextAndRaiseIOException() throws Exception {

        thrown.expect(FileUploadFailureException.class);

        // multi-part request.
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data");

        FileUpload upload = mock(FileUpload.class);
        when(upload.getItemIterator(isA(RequestContext.class))).thenThrow(new IOException());
        when(fileUploadFactory.createFileUpload()).thenReturn(upload);

        factory.setFileUploadFactory(fileUploadFactory);
        factory.createRequestContext(servletContext, request, response);
    }

}
