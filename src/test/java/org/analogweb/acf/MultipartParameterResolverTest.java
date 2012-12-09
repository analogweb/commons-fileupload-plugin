package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.Parameters;
import org.analogweb.ServletRequestContext;
import org.junit.Before;
import org.junit.Test;

public class MultipartParameterResolverTest {

    private MultipartParameterResolver resolver;
    private ServletRequestContext context;
    private HttpServletRequest multipartRequest;
    private InvocationMetadata metadata;
    private Parameters params;

    @Before
    public void setUp() throws Exception {
        resolver = new MultipartParameterResolver();
        context = mock(ServletRequestContext.class);
        multipartRequest = mock(HttpServletRequest.class);
        metadata = mock(InvocationMetadata.class);
        params = mock(Parameters.class);
        when(context.getParameters()).thenReturn(params);
    }

    @Test
    public void testResolveAttributeParameterValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getAttribute(MultipartParameterResolver.KEY_IS_MULTIPART_CONTENT))
                .thenReturn(Boolean.FALSE);
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String.class);

        assertThat((String) actual, is("baa"));
    }

    @Test
    public void testResolveAttributeParameterValues() {
        String[] expected = { "baa", "baz" };
        when(context.getServletRequest()).thenReturn(multipartRequest);
        when(multipartRequest.getAttribute(MultipartParameterResolver.KEY_IS_MULTIPART_CONTENT))
                .thenReturn(Boolean.FALSE);
        when(params.getValues("foo")).thenReturn(Arrays.asList(expected));
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String[].class);

        assertThat((String[]) actual, is(expected));
    }

    @Test
    public void testResolveAttributeFileValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);

        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file });
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        assertThat((Multipart) actual, is(file));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeFileValues() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });

        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart[].class);

        Multipart[] actualFiles = (Multipart[]) actual;
        assertThat(actualFiles[0], is(file));
        assertThat(actualFiles[1], is(file2));
    }

    @Test
    public void testResolveAttributeNotAvairableValue() {
        when(context.getServletRequest()).thenReturn(multipartRequest);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", String.class);

        assertNull(actual);
    }

    @Test
    public void testResolveAttributeMultipartParameters() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Object actual = resolver.resolveAttributeValue(context, metadata, "",
                Iterable.class);

        assertThat((MultipartParameters) actual, is(params));
    }

    @Test
    public void testResolveAttributeRequiresMultipart() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[]{multipart});
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        assertThat((Multipart)actual, is(multipart));
    }

    @Test
    public void testResolveAttributeNotMultipartRequestWithoutParameter() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(multipartRequest.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(context.getServletRequest()).thenReturn(multipartRequest);
        when(params.getMultiparts("foo")).thenReturn(null);
        Object actual = resolver.resolveAttributeValue(context, metadata, "foo", Multipart.class);

        assertNull(actual);
    }

}
