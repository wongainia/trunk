package cn.emoney.acg.helper;

import java.util.ArrayList;
import java.util.List;

interface FixedLengthList_IF<T> extends List<T>, java.io.Serializable {

    /**
     * 向最后添加一个新的，如果长度超过允许的最大值，则先弹出最后一个,再添加到最后
     * 
     * @param addLast 添加元素
     * @return 弹出
     */
    T addLastSafe(T addLast);

    /**
     * 向最前添加一个新的，如果长度超过允许的最大值，则弹出最后一个
     * 
     * @param addFirst 添加元素
     * @return 弹出
     */
    T addFirstSafe(T addFirst);

    /**
     * 添加一个新的，如果长度超过允许的最大值，则弹出最后一个
     * 
     * @param add 添加元素
     * @param index 位置
     */
    void addSafe(T add, int index);

    /**
     * 获取元素
     * 
     * @return
     */
    T getSafe(int index);

    /**
     * 获得最大个数
     * 
     * @return
     */
    int getMaxSize();

    /**
     * 设置最大存储范围
     * 
     * @return
     */
    void setMaxSize(int maxSize);

}


public class FixedLengthList<T> extends ArrayList<T> implements FixedLengthList_IF<T> {

    private int maxSize = Integer.MAX_VALUE;
    private final Object synObj = new Object();

    public FixedLengthList() {
        super();
    }

    public FixedLengthList(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    @Override
    public T addLastSafe(T addLast) {
        synchronized (synObj) {
            T head = null;
            if (size() >= maxSize) {
                head = remove(0);
            }

            add(addLast);

            return head;
        }
    }

    public T addFirstSafe(T addFirst) {
        synchronized (synObj) {
            T tail = null;
            if (size() >= maxSize) {
                tail = remove(size() - 1);
            }

            add(0, addFirst);
            return tail;
        }
    }

    @Override
    public void setMaxSize(int size) {
        if (size < this.maxSize) {
            synchronized (synObj) {
                while (size() > size) {
                    remove(size() - 1);
                }
            }
        }
        this.maxSize = size;
    }

    @Override
    public int getMaxSize() {
        return this.maxSize;
    }

    @Override
    public void addSafe(T add, int index) {
        synchronized (synObj) {
            if (index < 0) {
                return;
            }
            if (index >= size()) {
                addLastSafe(add);
            } else {
                add(index, add);
                if (size() > maxSize) {
                    remove(size() - 1);
                }
            }
        }

    }

    @Override
    public T getSafe(int index) {
        return null;
    }

}
