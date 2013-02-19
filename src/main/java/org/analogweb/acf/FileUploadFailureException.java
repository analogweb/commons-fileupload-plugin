package org.analogweb.acf;

import java.io.IOException;

import org.analogweb.core.ApplicationRuntimeException;
import org.apache.commons.fileupload.FileUploadException;

/**
 * ファイルアップロードの処理における例外を表します。
 * @author snowgoose
 */
public class FileUploadFailureException extends ApplicationRuntimeException {

    private static final long serialVersionUID = 939190704851384887L;
    
    public FileUploadFailureException(FileUploadException e){
        super(e);
    }

    public FileUploadFailureException(IOException e){
        super(e);
    }
}
