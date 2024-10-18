package com.ryy.utils.thread;

public class WmThreadLocalUtil {

    private final static InheritableThreadLocal<Integer> WM_USER_THREAD_LOCAL = new InheritableThreadLocal<>();

    //存入线程中
    public static void setUser(Integer userId){
        WM_USER_THREAD_LOCAL.set(userId);
    }

    //从线程中获取
    public static Integer getUser(){
        return WM_USER_THREAD_LOCAL.get();
    }

    //清理
    public static void clear(){
        WM_USER_THREAD_LOCAL.remove();
    }

}
