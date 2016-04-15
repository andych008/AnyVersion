package com.github.yoojia.versiontest.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

/**
 * Created by Andy on 4/7/2016.
 */
public class MyModel {


    private static MyModel instance;

    synchronized public static MyModel getInstance() {
        if (instance == null) {
            synchronized (MyModel.class) {
                if (instance == null) {
                    instance = new MyModel();
                }
            }
        }
        return instance;
    }

    private MyModel() {
    }

    public Observable<String> testConcat() {

        Observable<String> dbObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.i("RxThread", "create_db:" + ", run In :" + Thread.currentThread().getName());

//                subscriber.onNext("db");
                subscriber.onCompleted();
            }
        });

        Observable<String> netObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.i("RxThread", "create_net:" + ", run In :" + Thread.currentThread().getName());

                subscriber.onNext("net");
                subscriber.onCompleted();
            }
        });

        return Observable.concat(dbObservable, netObservable)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        Log.i("RxThread", "map_concat:" + ", run In :" + Thread.currentThread().getName());
                        return s;
                    }
                })
                .first()
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        Log.i("RxThread", "map_first:" + ", run In :" + Thread.currentThread().getName());
                        return s;
                    }
                });
    }

    public Observable<List<MyData>> test_toSortedList() {

        List<MyData> list = new ArrayList<>();
        list.add(new MyData(2, "A"));
        list.add(new MyData(3, "B"));
        list.add(new MyData(2, "B"));
        list.add(new MyData(1, "A"));
        list.add(new MyData(1, "B"));
        list.add(new MyData(3, "A"));

        //希望返回的数据只有code=2或3，怎么搞？
        return Observable.just(list)
                .flatMap(new Func1<List<MyData>, Observable<MyData>>() {
                    @Override
                    public Observable<MyData> call(List<MyData> myDatas) {
                        return Observable.from(myDatas);
                    }
                }).filter(new Func1<MyData, Boolean>() {
                    @Override
                    public Boolean call(MyData myData) {
                        return myData.code >= 2;
                    }
                }).toSortedList(new Func2<MyData, MyData, Integer>() {
                    @Override
                    public Integer call(MyData myData, MyData myData2) {
                        return myData.code - myData2.code;
                    }
                });
    }



    public static class MyData{
        public int code;
        public String name;

        public MyData(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return code + name;
        }
    }

    public Observable<List<MyData>> getTop(final int top) {

        List<MyData> list = new ArrayList<>();
        list.add(new MyData(2, "A"));
        list.add(new MyData(3, "B"));
        list.add(new MyData(2, "B"));
        list.add(new MyData(1, "A"));
        list.add(new MyData(1, "B"));
        list.add(new MyData(3, "A"));

        //如果top=2，希望返回的数据只有code=2和3，怎么搞？
        return Observable.from(list)
                .groupBy(new Func1<MyData, Integer>() {
                    @Override
                    public Integer call(MyData myData) {
                        return myData.code;
                    }
                }).toSortedList(new Func2<GroupedObservable<Integer, MyData>, GroupedObservable<Integer, MyData>, Integer>() {
                    @Override
                    public Integer call(GroupedObservable<Integer, MyData> integerMyDataGroupedObservable, GroupedObservable<Integer, MyData> integerMyDataGroupedObservable2) {
                        return integerMyDataGroupedObservable.getKey() - integerMyDataGroupedObservable2.getKey();
                    }
                }).map(new Func1<List<GroupedObservable<Integer, MyData>>, List<GroupedObservable<Integer, MyData>>>() {
                    @Override
                    public List<GroupedObservable<Integer, MyData>> call(List<GroupedObservable<Integer, MyData>> groupedObservables) {
                        int size = groupedObservables.size();
                        return size >= top ? groupedObservables.subList(size - top, size) : groupedObservables;
                    }
                }).flatMap(new Func1<List<GroupedObservable<Integer, MyData>>, Observable<GroupedObservable<Integer, MyData>>>() {
                    @Override
                    public Observable<GroupedObservable<Integer, MyData>> call(List<GroupedObservable<Integer, MyData>> groupedObservables) {
                        return Observable.from(groupedObservables);
                    }
                }).flatMap(new Func1<GroupedObservable<Integer, MyData>, Observable<MyData>>() {
                    @Override
                    public Observable<MyData> call(GroupedObservable<Integer, MyData> integerMyDataGroupedObservable) {
                        return integerMyDataGroupedObservable.asObservable();
                    }
                }).toList();

    }
}
