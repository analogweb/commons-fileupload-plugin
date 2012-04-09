package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.MultipartFile;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestContext;
import org.junit.Before;
import org.junit.Test;

public class MultipartRequestParameterResolverTest {
    
    private MultipartRequestParameterResolver resolver;
    private RequestContext context;
    private MultipartHttpServletRequest multipartRequest;
    private InvocationMetadata metadata;

    @Before
    public void setUp() throws Exception {
        resolver = new MultipartRequestParameterResolver();
        context = mock(RequestContext.class);
        multipartRequest = mock(MultipartHttpServletRequest.class);
        metadata = mock(InvocationMetadata.class);
    }

    @Test
    public void testResolveAttributeParameterValue() {
        when(context.getRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getParameter("foo")).thenReturn("baa");
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        assertThat((String)actual,is("baa"));
    }

    @Test
    public void testResolveAttributeParameterValues() {
        String[] expected = {"baa","baz"};
        when(context.getRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getParameterValues("foo")).thenReturn(expected);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        assertThat((String[])actual,is(expected));
    }

    @Test
    public void testResolveAttributeFileValue() {
        when(context.getRequest()).thenReturn(multipartRequest);
        MultipartFile file = mock(MultipartFile.class);
        when(multipartRequest.getFileParameter("foo")).thenReturn(file);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        assertThat((MultipartFile)actual,is(file));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeFileValues() {
        when(context.getRequest()).thenReturn(multipartRequest);
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = Arrays.asList(file,file2);
        when(multipartRequest.getFileParameterValues("foo")).thenReturn(files);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        List<MultipartFile> actualFiles = (List<MultipartFile>)actual;
        assertThat(actualFiles.get(0),is(file));
        assertThat(actualFiles.get(1),is(file2));
    }

    @Test
    public void testResolveAttributeNotAvairableValue() {
        when(context.getRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getFileParameterValues("foo")).thenReturn(null);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        assertNull(actual);
    }

    @Test
    public void testResolveAttributeMultipartParameters() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getMultipartParameters()).thenReturn(params);
        Object actual = resolver.resolveAttributeValue(context, metadata, "");

        assertThat((MultipartParameters) actual, is(params));
    }

    @Test
    public void testResolveAttributeNotMultipartRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("foo")).thenReturn("baa");
        when(context.getRequest()).thenReturn(request);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        assertThat((String)actual,is("baa"));
    }

    @Test
    public void testResolveAttributeNotMultipartRequestWithoutParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(context.getRequest()).thenReturn(request);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo");
        
        assertNull(actual);
    }

}
