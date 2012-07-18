package org.analogweb.acf;

import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.RequestContext;
import org.analogweb.core.AbstractInvocationProcessor;

public class TemporaryUploadFolderDisposeProcessor extends AbstractInvocationProcessor {

    @Override
    public void afterCompletion(RequestContext request, InvocationArguments args,
            InvocationMetadata metadata, Object invocationResult) {
        TemporaryUploadFolder tmp = TemporaryUploadFolder.current(request);
        if (tmp != null) {
            tmp.dispose();
        }
    }

}
