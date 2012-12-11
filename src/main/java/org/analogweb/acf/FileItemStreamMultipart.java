package org.analogweb.acf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.analogweb.Multipart;
import org.analogweb.util.IOUtils;
import org.apache.commons.fileupload.FileItemStream;

/**
 * 内部的に{@link FileItemStream}を使用する{@link Multipart}の実装です。<br/>
 * @author snowgoose
 */
public class FileItemStreamMultipart implements Multipart {

    private final FileItemStream item;
    private byte[] extracted;

    public FileItemStreamMultipart(FileItemStream item) {
        this.item = item;
    }

    public boolean isMultipartFile() {
        return this.item.isFormField() == false;
    }

    @Override
    public String getName() {
        return this.item.getFieldName();
    }

    @Override
    public String getResourceName() {
        return this.item.getName();
    }

    @Override
    public InputStream getInputStream() {
        if (this.extracted != null) {
            return new ByteArrayInputStream(Arrays.copyOf(this.extracted, this.extracted.length));
        }
        try {
            return this.item.openStream();
        } catch (IOException e) {
            throw new FileUploadFailureException(e);
        }
    }

    @Override
    public byte[] getBytes() {
        if (this.extracted != null) {
            return Arrays.copyOf(this.extracted, this.extracted.length);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copyQuietly(getInputStream(), out);
        return out.toByteArray();
    }

    @Override
    public String getContentType() {
        return this.item.getContentType();
    }

    /**
     * このパラメータの内容(アップロードされたファイル)を内部的に展開します。<br/>
     * 実行することによって、何度でもストリームを読み込むことが可能となりますが、
     * メモリにアップロードされたファイルの内容を展開するため、メモリ使用量やパフォーマンス
     * などStream APIの利点を生かすことが出来ないことに注意してください。<br/>
     * キーなどによるパラメータ値の参照時などに使用されます。
     */
    void extract() {
        this.extracted = getBytes();
    }

}
