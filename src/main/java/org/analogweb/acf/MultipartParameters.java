package org.analogweb.acf;

import java.util.Collection;
import java.util.Map;

import org.analogweb.Multipart;

public interface MultipartParameters<T extends Multipart> extends Iterable<T> {

    String[] getParameter(String name);

    Collection<String> getParameterNames();

    Map<String, String[]> getParameterMap();

    T[] getMultiparts(String name);

    Collection<String> getMultipartParameterNames();
}
