package org.analogweb.acf;

import org.analogweb.RequestContext;
import org.analogweb.ResponseContext;
import org.analogweb.core.AbstractApplicationProcessor;

public class TemporaryUploadFolderDisposeProcessor extends AbstractApplicationProcessor {

    @Override
    public void afterCompletion(RequestContext request, ResponseContext response, Exception e) {
        TemporaryUploadFolder tmp = TemporaryUploadFolder.current(request);
        if (tmp != null) {
            tmp.dispose();
        }
    }
}
