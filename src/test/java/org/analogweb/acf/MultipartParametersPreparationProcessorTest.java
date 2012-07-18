package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.InvocationProcessor;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.RequestAttributes;
import org.analogweb.RequestContext;
import org.analogweb.TypeMapperContext;
import org.analogweb.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;

public class MultipartParametersPreparationProcessorTest {

    private MultipartParametersPreparationProcessor processor;
    private InvocationArguments invocation;
    private InvocationMetadata metadata;
    private RequestContext context;
    private RequestAttributes attributes;
    private TypeMapperContext converters;

    @Before
    public void setUp() throws Exception {
        processor = new MultipartParametersPreparationProcessor();
        invocation = mock(InvocationArguments.class);
        metadata = mock(InvocationMetadata.class);
        context = mock(RequestContext.class);
        attributes = mock(RequestAttributes.class);
        converters = mock(TypeMapperContext.class);
    }

    @Test
    public void testPrepareInvoke() throws Exception {
        Method method = ReflectionUtils.getMethodQuietly(getClass(), "doSomething",
                new Class<?>[0]);

        when(metadata.getArgumentTypes()).thenReturn(
                new Class<?>[] { String.class, MultipartParameters.class });
        MultipartHttpServletRequest request = mock(MultipartHttpServletRequest.class);
        when(context.getRequest()).thenReturn(request);
        MultipartParameters params = mock(MultipartParameters.class);
        when(request.getMultipartParameters()).thenReturn(params);

        Object actual = processor.prepareInvoke(method, invocation, metadata, context,
                attributes, converters);

        assertThat(actual, is(sameInstance(InvocationProcessor.NO_INTERRUPTION)));
        verify(metadata).getArgumentTypes();
        verify(invocation).putInvocationArgument(1, params);
    }

    @Test
    public void testPrepareInvokeNotContainsMultipartParameterArgument() throws Exception {
        Method method = ReflectionUtils.getMethodQuietly(getClass(), "doSomething",
                new Class<?>[0]);

        when(metadata.getArgumentTypes())
                .thenReturn(new Class<?>[] { String.class, Integer.class });
        MultipartHttpServletRequest request = mock(MultipartHttpServletRequest.class);
        when(context.getRequest()).thenReturn(request);
        MultipartParameters params = mock(MultipartParameters.class);
        when(request.getMultipartParameters()).thenReturn(params);

        Object actual = processor.prepareInvoke(method, invocation, metadata, context,
                attributes, converters);

        assertThat(actual, is(sameInstance(InvocationProcessor.NO_INTERRUPTION)));
        verify(metadata).getArgumentTypes();
    }

    @Test
    public void testPrepareInvokeNotMultipartRequest() throws Exception {
        Method method = ReflectionUtils.getMethodQuietly(getClass(), "doSomething",
                new Class<?>[0]);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(context.getRequest()).thenReturn(request);

        Object actual = processor.prepareInvoke(method, invocation, metadata, context,
                attributes, converters);

        assertThat(actual, is(sameInstance(InvocationProcessor.NO_INTERRUPTION)));
    }

    public Object doSomething() {
        return "something";
    }

}
