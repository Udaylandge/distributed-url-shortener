package com.uday.urlshortener.service;

public interface SequenceGeneratorService {

    long getNextSequence(String sequenceName);

}