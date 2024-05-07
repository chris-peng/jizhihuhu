package top.lcmatrix.jizhihuhu.common;

import java.util.ArrayList;
import java.util.Collection;
public class FixedSizeList <T> extends ArrayList<T>{

    private final int fixedSize;

    public FixedSizeList(int size){
        super(size + 1);
        fixedSize = size;
    }

    public synchronized T push(T item){
        super.add(item);
        if(super.size() > fixedSize){
            return super.remove(0);
        }
        return null;
    }

    public synchronized void clear(){
        super.clear();
    }

    @Override
    @Deprecated
    public boolean add(T t) {
        throw new RuntimeException("not supported operation");
    }

    @Override
    @Deprecated
    public void add(int index, T element) {
        throw new RuntimeException("not supported operation");
    }

    @Override
    @Deprecated
    public boolean addAll(Collection<? extends T> c) {
        throw new RuntimeException("not supported operation");
    }

    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new RuntimeException("not supported operation");
    }
}
