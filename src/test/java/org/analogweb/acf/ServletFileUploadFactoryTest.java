package org.analogweb.acf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.analogweb.acf.DefaultFileUploadFactory;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.ProgressListener;
import org.junit.Before;
import org.junit.Test;

public class ServletFileUploadFactoryTest {
    
    private DefaultFileUploadFactory factory;
    private FileItemFactory fileItemFactory;
    private ProgressListener progressListener;

    @Before
    public void setUp() throws Exception {
        fileItemFactory = mock(FileItemFactory.class);
        progressListener = mock(ProgressListener.class);
    }

    @Test
    public void testCreateFileUpload() {
        factory = new DefaultFileUploadFactory();
        factory.setFileSizeMax(1000L);
        factory.setHeaderEncoding("UTF-8");
        factory.setListener(progressListener);
        factory.setSizeMax(2000);
        
        FileUpload actual = factory.createFileUpload(fileItemFactory);
        assertThat(actual.getFileSizeMax(),is(1000L));
        assertThat(actual.getHeaderEncoding(),is("UTF-8"));
        assertThat(actual.getProgressListener(),is(progressListener));
        assertThat(actual.getSizeMax(),is(2000L));
    }

    @Test
    public void testCreateFileUploadWithoutFileItemFactory() {
        factory = new DefaultFileUploadFactory();
        factory.setFileSizeMax(2000L);
        factory.setHeaderEncoding("Shift-JIS");
        factory.setListener(progressListener);
        factory.setSizeMax(3000);

        FileUpload actual = factory.createFileUpload();
        assertThat(actual.getFileSizeMax(),is(2000L));
        assertThat(actual.getHeaderEncoding(),is("Shift-JIS"));
        assertThat(actual.getProgressListener(),is(progressListener));
        assertThat(actual.getSizeMax(),is(3000L));
    }

}
