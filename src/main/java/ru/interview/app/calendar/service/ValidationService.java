package ru.interview.app.calendar.service;

public interface ValidationService {
    <T> T validate(T objectToValidate);
}
