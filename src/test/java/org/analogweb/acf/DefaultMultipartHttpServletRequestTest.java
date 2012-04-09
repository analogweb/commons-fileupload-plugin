package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


import org.analogweb.MultipartFile;
import org.analogweb.acf.DefaultMultipartHttpServletRequest;
import org.analogweb.acf.MultipartRequestParameters;
import org.analogweb.util.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultMultipartHttpServletRequestTest {

    private DefaultMultipartHttpServletRequest wrapper;
    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFileParameter() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa" });

        MultipartFile file = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        assertThat(wrapper.getFileParameter("baz"), is(file));
        assertThat(wrapper.getParameter("foo"), is("baa"));
    }

    @Test
    public void testFileParameterNames() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa" });

        MultipartFile file = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file });
        files.put("bad", new MultipartFile[] { file2 });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        Collection<String> actual = wrapper.getFileParameterNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("baz"));
        assertTrue(actual.contains("bad"));
    }

    @Test
    public void testFileParameterValues() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa" });

        MultipartFile file = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        MultipartFile file3 = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file,file2 });
        files.put("bad", new MultipartFile[] { file3 });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        List<MultipartFile> actual = wrapper.getFileParameterValues("baz");
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0),is(file));
        assertThat(actual.get(1),is(file2));

        actual = wrapper.getFileParameterValues("bad");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0),is(file3));

        actual = wrapper.getFileParameterValues("bag");
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testFileParameterMap() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa" });

        MultipartFile file = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        MultipartFile file3 = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file,file2 });
        files.put("bad", new MultipartFile[] { file3 });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        Map<String,MultipartFile[]> actualMap = wrapper.getFileParameterMap();
        
        List<MultipartFile> actual = Arrays.asList(actualMap.get("baz"));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0),is(file));
        assertThat(actual.get(1),is(file2));

        actual = Arrays.asList(actualMap.get("bad"));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0),is(file3));

        assertNull(actualMap.get("bag"));
    }

    @Test
    public void testFileParameterNotAvairable() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa" });
        when(request.getParameter("food")).thenReturn("one");

        Map<String, MultipartFile[]> files = Maps.newEmptyHashMap();

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        assertThat(wrapper.getFileParameter("bad"), is(nullValue()));
        assertThat(wrapper.getParameter("foo"), is("baa"));
        assertThat(wrapper.getParameter("food"), is("one"));
    }

    @Test
    public void testParameterValues() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa", "baz" });
        when(request.getParameterValues("food")).thenReturn(new String[] { "one", "two" });

        MultipartFile file = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        assertThat(wrapper.getParameterValues("bad"), is(nullValue()));
        assertArrayEquals(wrapper.getParameterValues("foo"), new String[] { "baa", "baz" });
        assertArrayEquals(wrapper.getParameterValues("food"), new String[] { "one", "two" });
    }

    @Test
    public void testParameterNames() {
        when(request.getParameterNames()).thenReturn(
                Collections.enumeration(Arrays.asList("foo", "baa")));
        Map<String, String[]> parameters = Maps.newHashMap("baz", new String[] { "baa", "baz" });

        MultipartFile file = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        Enumeration<String> actual = wrapper.getParameterNames();
        assertThat(actual.nextElement(), is("baz"));
        assertThat(actual.nextElement(), is("foo"));
        assertThat(actual.nextElement(), is("baa"));
    }

    @Test
    public void testParameterNamesWithEmptyParams() {
        when(request.getParameterNames()).thenReturn(
                Collections.enumeration(Arrays.asList("foo", "baa")));
        Map<String, String[]> parameters = Maps.newEmptyHashMap();

        MultipartFile file = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file });

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        Enumeration<String> actual = wrapper.getParameterNames();
        assertThat(actual.nextElement(), is("foo"));
        assertThat(actual.nextElement(), is("baa"));
    }

    @Test
    public void testParameterMaps() {
        Map<String, String[]> parameters = Maps.newHashMap("foo", new String[] { "baa", "baz" });
        when(request.getParameterMap()).thenReturn(
                Maps.newHashMap("food", new String[] { "one", "two" }));

        Map<String, MultipartFile[]> files = Maps.newEmptyHashMap();

        MultipartRequestParameters params = new MultipartRequestParameters(parameters, files);
        wrapper = new DefaultMultipartHttpServletRequest(request, params);

        Map<String, String[]> actual = wrapper.getParameterMap();
        assertArrayEquals(actual.get("foo"), new String[] { "baa", "baz" });
        assertArrayEquals(actual.get("food"), new String[] { "one", "two" });
    }

}
