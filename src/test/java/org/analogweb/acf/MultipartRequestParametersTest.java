package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.Map;


import org.analogweb.MultipartFile;
import org.analogweb.MultipartParameters;
import org.analogweb.acf.MultipartRequestParameters;
import org.analogweb.util.Maps;
import org.junit.Test;

public class MultipartRequestParametersTest {

    private MultipartRequestParameters params;

    @Test
    public void testIterator() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        Map<String, MultipartFile[]> files = Maps.newHashMap("baz", new MultipartFile[] { file });
        Map<String, String[]> param = Maps.newHashMap("foo", new String[] { "baa" });

        params = new MultipartRequestParameters(param, files);
        Iterator<MultipartParameters.MultipartParameter> actual = params.iterator();

        assertTrue(actual.hasNext());
        MultipartParameters.MultipartParameter actualParam = actual.next();
        assertTrue(actualParam.isMultipartFile());
        assertThat(actualParam.getParameterName(), is("baz"));
        assertThat((MultipartFile) actualParam.value(), is(file));

        assertTrue(actual.hasNext());
        actualParam = actual.next();
        assertFalse(actualParam.isMultipartFile());
        assertThat(actualParam.getParameterName(), is("foo"));
        assertThat((String) actualParam.value(), is("baa"));

        assertFalse(actual.hasNext());
    }

    @Test
    public void testIteratorEmpty() throws Exception {
        Map<String, MultipartFile[]> files = Maps.newEmptyHashMap();
        Map<String, String[]> param = Maps.newEmptyHashMap();

        params = new MultipartRequestParameters(param, files);
        Iterator<MultipartParameters.MultipartParameter> actual = params.iterator();
        
        assertFalse(actual.hasNext());
    }

}
