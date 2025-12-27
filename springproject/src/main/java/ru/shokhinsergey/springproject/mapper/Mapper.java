package ru.shokhinsergey.springproject.mapper;

public interface Mapper <From, To>{
    To mapFrom(From from);

}
