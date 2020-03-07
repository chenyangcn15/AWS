package com.ccproject.whatsaround.http;

/**
 * Created by lei on 4/25/2018.
 */

public interface IParser<T> {
    public T parse(String data);
}
