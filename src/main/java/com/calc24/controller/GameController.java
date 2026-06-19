package com.calc24.controller;

import com.calc24.model.*;
import com.calc24.service.Calc24Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final Calc24Service service;

    public GameController(Calc24Service service) {
        this.service = service;
    }

    @PostMapping("/puzzle")
    public ResponseEntity<PuzzleResponse> generatePuzzle() {
        List<Integer> numbers = service.generatePuzzle();
        return ResponseEntity.ok(new PuzzleResponse(numbers));
    }

    @PostMapping("/solve")
    public ResponseEntity<SolveResponse> solve(@RequestBody SolveRequest request) {
        List<String> solutions = service.findSolutions(request.numbers());
        return ResponseEntity.ok(new SolveResponse(solutions));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestBody ValidateRequest request) {
        try {
            if (!service.usesCorrectNumbers(request.expression(), request.numbers())) {
                return ResponseEntity.ok(new ValidateResponse(
                        false, request.expression(), 0,
                        "Expression must use exactly the numbers: " + request.numbers()));
            }

            double result = service.evaluate(request.expression());
            boolean isValid = Math.abs(result - 24.0) < 1e-9;

            String message;
            if (isValid) {
                message = "Correct! The expression equals 24.";
            } else {
                message = "The expression evaluates to " + result + ", not 24.";
            }

            return ResponseEntity.ok(new ValidateResponse(
                    isValid, request.expression(), result, message));
        } catch (Exception e) {
            return ResponseEntity.ok(new ValidateResponse(
                    false, request.expression(), 0,
                    "Invalid expression: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
