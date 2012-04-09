package org.analogweb.acf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.analogweb.MultipartFile;
import org.analogweb.MultipartParameters;

/**
 * {@link MultipartFile}のインスタンスを内部に保持する{@link MultipartParameters}
 * の実装です。
 * @author snowgoose
 */
public class MultipartRequestParameters implements MultipartParameters {

    private final Map<String, String[]> params;
    private final Map<String, MultipartFile[]> files;

    public MultipartRequestParameters(Map<String, String[]> params,Map<String, MultipartFile[]> files){
        this.params = params;
        this.files  = files;
    }

    @Override
    public String[] getParameter(String name){
        return this.params.get(name);
    }

    @Override
    public Collection<String> getParameterNames(){
        return getParameterMap().keySet();
    }

    @Override
    public Map<String,String[]> getParameterMap(){
        return this.params;
    }

    @Override
    public MultipartFile[] getFile(String name){
        return this.files.get(name);
    }
    
    @Override
    public Collection<String> getFileParameterNames() {
        return this.files.keySet();
    }

    @Override
    public Map<String, MultipartFile[]> getFileMap() {
        return this.files;
    }

    @Override
    public Iterator<MultipartParameter> iterator() {
        List<MultipartParameter> result = new ArrayList<MultipartParameter>();
        for(final Entry<String, MultipartFile[]> entry : files.entrySet()){
            for(final MultipartFile file : entry.getValue()){
                result.add(new MultipartParameter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public MultipartFile value() {
                        return file;
                    }
                    
                    @Override
                    public boolean isMultipartFile() {
                        return true;
                    }
                    
                    @Override
                    public String getParameterName() {
                        return entry.getKey();
                    }
                });
            }
        }
        for(final Entry<String, String[]> entry : params.entrySet()){
            for(final String value : entry.getValue()){
                result.add(new MultipartParameter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public String value() {
                        return value;
                    }
                    @Override
                    public boolean isMultipartFile() {
                        return false;
                    }
                    @Override
                    public String getParameterName() {
                        return entry.getKey();
                    }
                });
            }
        }
        return result.iterator();
    }

}
