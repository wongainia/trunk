package cn.emoney.acg.helper;

import java.util.HashMap;
import java.util.Set;

/**
 * 简单双向map,必须保证key和value都唯一,如value不唯一,数据会乱
 * 
 * @author daizhipeng
 *
 */
public class DualHashMap {
    private HashMap<Object, Object> map1 = null;
    private HashMap<Object, Object> map2 = null;


    public DualHashMap() {
        map1 = new HashMap<Object, Object>();
        map2 = new HashMap<Object, Object>();
    }

    public Object getValueByKey(Object key) {
        if (map1.containsKey(key)) {
            return map1.get(key);
        }

        return null;
    }

    public Object getKeyByValue(Object value) {
        if (map2.containsKey(value)) {
            return map2.get(value);
        }

        return null;
    }

    public void put(Object key, Object value) {
        map1.put(key, value);
        map2.put(value, key);
    }

    public void removeByKey(Object key) {
        if (map1.containsKey(key)) {
            Object value = map1.get(key);
            map2.remove(value);
            map1.remove(key);
        }
    }

    public void clear() {
        map1.clear();
        map2.clear();

    }

    public Set<Object> keySet() {
        Set<Object> set = null;
        set = map1.keySet();

        return set;
    }

    public Set<Object> valueSet() {
        Set<Object> set = null;
        set = map2.keySet();

        return set;
    }
}
