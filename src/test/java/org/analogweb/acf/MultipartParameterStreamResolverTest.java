package org.analogweb.acf;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.analogweb.InvocationMetadata;
import org.analogweb.Multipart;
import org.analogweb.Parameters;
import org.analogweb.RequestContext;
import org.analogweb.core.MediaTypes;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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

    @Test
    public void testResolveAttributeParameterValue() {
        when(params.getValues("foo")).thenReturn(Arrays.asList("baa"));
        Object actual = resolver.resolveValue(context, metadata, "foo", String.class, null);
        assertThat((String) actual, is("baa"));
    }

    @Test
    public void testResolveAttributeParameterValues() {
        String[] expected = { "baa", "baz" };
        when(context.getRequestMethod()).thenReturn("GET");
        when(params.getValues("foo")).thenReturn(Arrays.asList(expected));
        Object actual = resolver.resolveValue(context, metadata, "foo", String[].class, null);
        assertThat((String[]) actual, is(expected));
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testResolveAttributeWithMultipartArray() {
    	thrown.expect(UnsupportedParameterTypeException.class);
        Multipart file = mock(Multipart.class);
        Multipart file2 = mock(Multipart.class);
        MultipartParameters<Multipart> params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { file, file2 });
        Object actual = resolver.resolveValue(context, metadata, "foo", Multipart[].class, null);
        Multipart[] actualFiles = (Multipart[]) actual;
        assertThat(actualFiles[0], is(file));
        assertThat(actualFiles[1], is(file2));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testResolveAttributeMultipartParameters() {
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        Object actual = resolver.resolveValue(context, metadata, "", Iterable.class, null);
        assertThat((MultipartParameters) actual, is(params));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testResolveAttributeMultipartNotAvairableParameters() throws Exception {
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(null);
        when(context.getContentType()).thenReturn(MediaTypes.valueOf("multipart/form-data; boundary=------------------------------4ebf00fbcf09"));
        when(context.getRequestBody()).thenReturn(new ByteArrayInputStream(new String(""
+"------------------------------4ebf00fbcf09\r\n"
+"Content-Disposition: form-data; name='example'\r\n"
+"\r\n"
+"test\r\n"
+"------------------------------4ebf00fbcf09--\r\n").getBytes()));
        Iterable<Multipart> actual = (Iterable<Multipart>) resolver.resolveValue(context, metadata, "", Iterable.class, null);
        // TODO:Verify boundary data.
        assertThat(actual, is(notNullValue()));
    }

    @Test
    public void testResolveAttributeRequiresUnsupportedType() {
        thrown.expect(new BaseMatcher<UnsupportedParameterTypeException>() {

            @Override
            public boolean matches(Object obj) {
                if (obj instanceof UnsupportedParameterTypeException) {
                    UnsupportedParameterTypeException un = (UnsupportedParameterTypeException) obj;
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
        MultipartParameters params = mock(MultipartParameters.class);
        when(context.getAttribute(CurrentMultipartParameters.ATTRIBUTE_NAME)).thenReturn(
                params);
        Multipart multipart = mock(Multipart.class);
        when(params.getMultiparts("foo")).thenReturn(new Multipart[] { multipart });
        Object actual = resolver.resolveValue(context, metadata, "foo", InputStream[].class, null);
        assertThat((Multipart) actual, is(multipart));
    }
}
