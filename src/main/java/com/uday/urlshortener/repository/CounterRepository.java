package com.uday.urlshortener.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.uday.urlshortener.model.Counter;

public interface CounterRepository extends MongoRepository<Counter, String> {

}