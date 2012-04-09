package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.analogweb.ModulesBuilder;
import org.junit.Before;
import org.junit.Test;

public class CommonsFileUploadModulesConfigTest {

    private CommonsFileUploadModulesConfig config;
    private ModulesBuilder builder;

    @Before
    public void setUp() throws Exception {
        config = new CommonsFileUploadModulesConfig();
        builder = mock(ModulesBuilder.class);
    }

    @Test
    public void testPrepare() {
        when(builder.setRequestContextFactoryClass(FileItemIteratorRequestContextFactory.class))
                .thenReturn(builder);
        when(builder.addAttributesHandlerClass(MultipartRequestParameterResolver.class))
                .thenReturn(builder);

        ModulesBuilder actual = config.prepare(builder);

        assertThat(actual, is(sameInstance(builder)));

        verify(builder).setRequestContextFactoryClass(FileItemIteratorRequestContextFactory.class);
        verify(builder).addAttributesHandlerClass(MultipartRequestParameterResolver.class);
    }

}
