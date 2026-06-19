package com.calc24.model;

import java.util.List;

public record ValidateRequest(List<Integer> numbers, String expression) {
}
