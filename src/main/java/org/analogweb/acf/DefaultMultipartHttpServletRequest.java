package org.analogweb.acf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


import org.analogweb.MultipartFile;
import org.analogweb.MultipartHttpServletRequest;
import org.analogweb.MultipartParameters;
import org.analogweb.util.ArrayUtils;
import org.analogweb.util.Assertion;
import org.analogweb.util.Maps;

/**
 * 既定の{@link MultipartHttpServletRequest}の実装。
 * @author snowgoose
 */
public class DefaultMultipartHttpServletRequest extends HttpServletRequestWrapper implements
        MultipartHttpServletRequest {

    private final MultipartParameters params;

    public DefaultMultipartHttpServletRequest(HttpServletRequest request,
            MultipartParameters params) {
        super(request);
        Assertion.notNull(params, MultipartRequestParameters.class.getSimpleName());
        this.params = params;
    }

    @Override
    public MultipartParameters getMultipartParameters() {
        return this.params;
    }

    @Override
    public String getParameter(String name) {
        String[] params = getMultipartParameters().getParameter(name);
        if (ArrayUtils.isNotEmpty(params)) {
            return params[0];
        }
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] params = getMultipartParameters().getParameter(name);
        if (ArrayUtils.isNotEmpty(params)) {
            return params;
        }
        return super.getParameterValues(name);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getParameterNames(){
        Set<String> names = new HashSet<String>(getMultipartParameters().getParameterNames());
        Enumeration<String> deligatorNames = super.getParameterNames();
        while(deligatorNames.hasMoreElements()){
            names.add(deligatorNames.nextElement());
        }
        return Collections.enumeration(names);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String,String[]> getParameterMap() {
        Map<String,String[]> parameterMap = Maps.newEmptyHashMap();
        parameterMap.putAll(getMultipartParameters().getParameterMap());
        parameterMap.putAll(super.getParameterMap());
        return parameterMap;
    }

    @Override
    public MultipartFile getFileParameter(String name) {
        MultipartFile[] files = getMultipartParameters().getFile(name);
        if (ArrayUtils.isNotEmpty(files)) {
            return files[0];
        }
        return null;
    }

    @Override
    public Collection<String> getFileParameterNames() {
        return getMultipartParameters().getFileParameterNames();
    }

    @Override
    public List<MultipartFile> getFileParameterValues(String name) {
        MultipartFile[] files = getMultipartParameters().getFile(name);
        if (ArrayUtils.isNotEmpty(files)) {
            return Arrays.asList(files);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, MultipartFile[]> getFileParameterMap() {
        return getMultipartParameters().getFileMap();
    }
}
