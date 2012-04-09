package org.analogweb.acf;

import java.io.IOException;
import java.io.InputStream;


import org.analogweb.MultipartFile;
import org.apache.commons.fileupload.FileItem;

/**
 * 内部的に{@link FileItem}を使用する{@link MultipartFile}の実装です。<br/>
 * 殆どの場合において、{@link FileItem}に処理を委譲しています。
 * @author snowgoose
 */
public class FileItemMultipartFile implements MultipartFile {
    
    private final FileItem fileItem;
    
    public FileItemMultipartFile(FileItem fileItem){
        this.fileItem = fileItem;
    }
    
    public FileItem getFileItem(){
        return this.fileItem;
    }

    @Override
    public String getParameterName() {
        return fileItem.getFieldName();
    }

    @Override
    public String getFileName() {
        return fileItem.getName();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return fileItem.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public byte[] getBytes() {
        return fileItem.get();
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

}
