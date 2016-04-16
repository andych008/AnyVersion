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


    public Observable<String> test_onErrorReturn() {

        final int code = 2;

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.i("RxThread", "create_db:" + ", run In :" + Thread.currentThread().getName());
                subscriber.onNext(getCodeFromDb(code));
                subscriber.onCompleted();
            }
        }).onErrorReturn(new Func1<Throwable, String>() {
            @Override
            public String call(Throwable throwable) {
                Log.i("RxThread", "create_net:" + ", run In :" + Thread.currentThread().getName());
                return getCodeFromNet(code);
            }
        }).subscribeOn(Schedulers.io());

    }


    //希望返回：[1A, 2B, 3A]。但实际返回[1A, 2B, 3B]。怎么搞？
    public Observable<List<String>> test_onErrorResumeNext() {
        final List<Integer> codeList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            codeList.add(i);
        }
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Log.i("RxThread", "create_db:" + ", run In :" + Thread.currentThread().getName());
                int i = 0;
                try {
                    for (; i < codeList.size(); i++) {
                        subscriber.onNext(getCodeFromDb(codeList.get(i)));
                    }
                } catch (Exception e) {
                    codeList.removeAll(codeList.subList(0, i));
                }
                subscriber.onCompleted();
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
            @Override
            public Observable<? extends String> call(Throwable throwable) {
                return Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        Log.i("RxThread", "create_net:" + ", run In :" + Thread.currentThread().getName());
                        for (Integer code : codeList) {
                            subscriber.onNext(getCodeFromNet(code));
                        }
                        subscriber.onCompleted();
                    }
                });
            }
        }).toList().subscribeOn(Schedulers.io());

    }

    //从list1从找出包含list2的值。希望返回：[2, 6]。怎么搞？
    public Observable<List<Integer>> test_filter() {
        final List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(4);
        list1.add(6);
        final List<Integer> list2 = new ArrayList<>();
        list2.add(2);
        list2.add(6);
        list2.add(8);

        return Observable.from(list2).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer item) {
                return list1.contains(item);
            }
        }).toList();

    }

    private String getCodeFromDb(int code) {
        if (code==1 || code==3) {
            return code+"A";
        }
        throw new RetryException("db not found", code);
    }

    private String getCodeFromNet(int code) {
        if (code<100) {
            return code+"B";
        }
        throw new RuntimeException("net not found");
    }

    static class RetryException extends RuntimeException {
        int code;

        public RetryException(String detailMessage, int code) {
            super(detailMessage);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }


}
