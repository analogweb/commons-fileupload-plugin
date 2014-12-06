package org.analogweb.acf;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;

/**
 * {@link FileUpload}のインスタンスを生成するファクトリです。<br/>
 * @author snowgoose
 * @param <T> このファクトリで生成される{@link FileUpload}のサブタイプ
 */
public interface FileUploadFactory<T extends FileUpload> {

    /**
     * {@link FileUpload}のインスタンスを生成します。<br/>
     * {@link FileItemFactory}を使用しないインスタンスが生成されます。
     * @return 新しい{@link FileUpload}のインスタンス
     */
    T createFileUpload();

    /**
     * {@link FileUpload}のインスタンスを生成します。<br/>
     * 指定する{@link FileItemFactory}を使用するインスタンスが生成されます。
     * {@link org.apache.commons.fileupload.FileItem}の生成による
     * 取得前にバイナリを展開する方式が選択されます。
     * @param fileItemFactory {@link FileItemFactory}
     * @return 新しい{@link FileUpload}のインスタンス
     */
    T createFileUpload(FileItemFactory fileItemFactory);
}
