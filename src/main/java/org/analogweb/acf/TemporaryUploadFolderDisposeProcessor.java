package org.analogweb.acf;

import org.analogweb.Invocation;
import org.analogweb.InvocationMetadata;
import org.analogweb.RequestContext;
import org.analogweb.core.AbstractInvocationProcessor;

public class TemporaryUploadFolderDisposeProcessor extends
		AbstractInvocationProcessor {

	@Override
	public void afterCompletion(RequestContext request, Invocation invocation,
			InvocationMetadata metadata, Object invocationResult) {
		TemporaryUploadFolder tmp = TemporaryUploadFolder.current(request);
		if(tmp != null){
			tmp.dispose();
		}
	}

}
