package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.analogweb.Application;
import org.analogweb.InvocationMetadata;
import org.analogweb.MediaType;
import org.analogweb.Multipart;
import org.analogweb.Parameters;
import org.analogweb.RequestContext;
import org.analogweb.core.DefaultApplicationProperties;
import org.analogweb.core.MediaTypes;
import org.analogweb.util.ApplicationPropertiesHolder;
import org.analogweb.util.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class MultipartParameterStreamResolverTest {

    private MultipartParameterStreamResolver resolver;
    private RequestContext context;
    private InvocationMetadata metadata;
    private Parameters params;
    private Parameters empty;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ApplicationPropertiesHolder.configure(mock(Application.class),
                DefaultApplicationProperties.defaultProperties());
        resolver = new MultipartParameterStreamResolver();
        context = mock(RequestContext.class);
        metadata = mock(InvocationMetadata.class);
        params = mock(Parameters.class);
        empty = mock(Parameters.class);
        when(context.getFormParameters()).thenReturn(params);
        when(context.getQueryParameters()).thenReturn(empty);
        when(context.getMatrixParameters()).thenReturn(empty);
        when(context.getRequestMethod()).thenReturn("POST");
    }

    @After
    public void tearDown() {
        ApplicationPropertiesHolder.dispose(mock(Application.class));
    }

    @Test
    public void testResolveAttributeParameterValue() {
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        final Object actual = resolver.resolveValue(context, metadata, "foo", String.class, null);
        assertThat((String) actual, is("baa"));
    }

    @Test
    public void testResolveAttributeParameterValues() {
        final String[] expected = { "baa", "baz" };
        when(context.getRequestMethod()).thenReturn("GET");
        when(params.getValues("foo")).thenReturn(Arrays.asList(expected));
        final Object actual = resolver.resolveValue(context, metadata, "foo", String[].class, null);
        assertThat((String[]) actual, is(expected));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithMultipartArray() {
        thrown.expect(UnsupportedParameterTypeException.class);
        final Multipart file = mock(Multipart.class);
        final Multipart file2 = mock(Multipart.class);
        final MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        final Object actual = resolver.resolveValue(context, metadata, "foo", Multipart[].class, null);
        final Multipart[] actualFiles = (Multipart[]) actual;
        assertThat(actualFiles[0], is(file));
        assertThat(actualFiles[1], is(file2));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testResolveAttributeMultipartParameters() {
        final MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        final Object actual = resolver.resolveValue(context, metadata, "", Iterable.class, null);
        assertThat((MultipartParameters) actual, is(params));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveAttributeMultipartNotAvairableParameters() throws Exception {
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(null);
        when(context.getContentLength()).thenReturn(199L);
        final MediaType mt = MediaTypes
                .valueOf("multipart/form-data; boundary=------------------------------4ebf00fbcf09");
        when(context.getContentType()).thenReturn(mt);
        final byte[] part = new StringBuilder()
        .append("--------------------------------4ebf00fbcf09\r\n")
        .append("Content-Disposition: form-data; name=\"example\"\r\n")
        .append("Content-Type: text/plain\r\n").append("\r\n").append("test\r\n")
        .append("--------------------------------4ebf00fbcf09--\r\n").toString().getBytes();
        when(context.getRequestBody()).thenReturn(new ByteArrayInputStream(part));
        final Iterable<Multipart> actual = (Iterable<Multipart>) resolver.resolveValue(context, metadata,
                "", Iterable.class, null);
        final Multipart actualPart = actual.iterator().next();
        assertThat(actualPart.getName(), is("example"));
        assertThat(IOUtils.toString(actualPart.getInputStream()), is("test"));
    }

    @Test
    public void testResolveAttributeRequiresUnsupportedType() {
        thrown.expect(new BaseMatcher<UnsupportedParameterTypeException>() {

            @Override
            public boolean matches(Object obj) {
                if (obj instanceof UnsupportedParameterTypeException) {
                    final UnsupportedParameterTypeException un = (UnsupportedParameterTypeException) obj;
                    assertThat(un.getSpecifiedType().getCanonicalName(),
                            is(InputStream[].class.getCanonicalName()));
                    assertThat(un.getMissedParameterName(), is("foo"));
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description desc) {
                // nop.
            }
        });
        @SuppressWarnings("rawtypes")
        final
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(params);
        final Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { multipart });
        final Object actual = resolver.resolveValue(context, metadata, "foo", InputStream[].class, null);
        assertThat((Multipart) actual, is(multipart));
    }
}
