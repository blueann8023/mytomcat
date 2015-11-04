package org.apache.catalina.util;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

public class ParameterMap extends HashMap {

    public ParameterMap() {
        super();
    }

    public ParameterMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ParameterMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ParameterMap(Map map) {
        super(map);
    }

    @Setter
    private boolean locked = false;
 
    public boolean isLocked(){
        return (this.locked);
    }
    private static final StringManager sm =
            StringManager.getManager("org.apache.catalina.util");
    
    public void clear(){
        if(locked){
            throw new IllegalStateException(sm.getString("parameterMap.locked"));
        }
        super.clear();
    }
    
    public Object put(Object key ,Object value){
        if(locked)
            throw new IllegalStateException(sm.getString("parameterMap.locked"));
        return super.put(key, value);
    }
    public void putAll(Map map) {

        if (locked)
            throw new IllegalStateException
                (sm.getString("parameterMap.locked"));
        super.putAll(map);

    }
    public Object remove(Object key) {

        if (locked)
            throw new IllegalStateException
                (sm.getString("parameterMap.locked"));
        return (super.remove(key));

    }
}
