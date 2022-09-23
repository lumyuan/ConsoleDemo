package com.example.console.demo.console;


import java.util.HashMap;
import java.util.Map;

/**
 * 控制台数据实体类
 */
public class MutableData<T> {

    private volatile T value;
    private final Map<Integer, Observer<? super T>> observerMap = new HashMap<>();

    public MutableData() {

    }

    public MutableData(T value) {
        this.value = value;
    }

    /**
     * 获取数据
     * @return 数据
     */
    public synchronized T getValue() {
        return value;
    }

    public void clean(){
        value = null;
    }

    /**
     * 更新数据的变化
     * @param value 数据
     */
    public synchronized void setValue(T value) {
        this.value = value;
        for (Map.Entry<Integer, Observer<? super T>> entry: observerMap.entrySet()) {
            entry.getValue().onChanged(value);
        }
    }

    /**
     * 观察值的变化
     * @param observer 观察者
     */
    public void observe(Observer<? super T> observer){
        observerMap.put(observer.hashCode(), observer);
    }

    public interface Observer<T> {
        void onChanged(T value);
    }
}
