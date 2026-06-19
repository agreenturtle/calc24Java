package com.calc24.model;

public record ValidateResponse(boolean valid, String expression, double value, String message) {
}
