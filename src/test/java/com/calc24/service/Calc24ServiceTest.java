package com.calc24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Calc24ServiceTest {

    private Calc24Service service;

    @BeforeEach
    void setUp() {
        service = new Calc24Service();
    }

    @Test
    void generatePuzzle_shouldReturn4Numbers() {
        List<Integer> puzzle = service.generatePuzzle();
        assertEquals(4, puzzle.size());
        for (int n : puzzle) {
            assertTrue(n >= 1 && n <= 13);
        }
    }

    @Test
    void generatePuzzle_shouldBeSolvable() {
        for (int i = 0; i < 50; i++) {
            List<Integer> puzzle = service.generatePuzzle();
            List<String> solutions = service.findSolutions(puzzle);
            assertFalse(solutions.isEmpty(), "No solution for " + puzzle);
        }
    }

    @Test
    void findSolutions_knownPuzzle() {
        List<String> solutions = service.findSolutions(List.of(1, 2, 3, 4));
        assertFalse(solutions.isEmpty());
    }

    @Test
    void findSolutions_requiresExactly4Numbers() {
        assertThrows(IllegalArgumentException.class, () -> service.findSolutions(List.of(1, 2, 3)));
        assertThrows(IllegalArgumentException.class, () -> service.findSolutions(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    void evaluate_simpleAddition() {
        assertEquals(7.0, service.evaluate("3 + 4"), 1e-9);
    }

    @Test
    void evaluate_complexExpression() {
        double result = service.evaluate("(3 + 5) * (4 - 1)");
        assertEquals(24.0, result, 1e-9);
    }

    @Test
    void evaluate_division() {
        double result = service.evaluate("8 / (1 - 2/3)");
        assertEquals(24.0, result, 1e-9);
    }

    @Test
    void evaluate_throwsOnDivisionByZero() {
        assertThrows(ArithmeticException.class, () -> service.evaluate("5 / 0"));
    }

    @Test
    void evaluate_throwsOnMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluate("3 +"));
        assertThrows(IllegalArgumentException.class, () -> service.evaluate("(3 + 4"));
    }

    @Test
    void usesCorrectNumbers_valid() {
        assertTrue(service.usesCorrectNumbers("(3 + 5) * (4 - 1)", List.of(3, 5, 4, 1)));
    }

    @Test
    void usesCorrectNumbers_wrongNumbers() {
        assertFalse(service.usesCorrectNumbers("10 + 10 + 4", List.of(1, 2, 3, 4)));
    }

    @Test
    void usesCorrectNumbers_missingNumber() {
        assertFalse(service.usesCorrectNumbers("3 + 5 + 4", List.of(3, 5, 4, 1)));
    }

    @Test
    void usesCorrectNumbers_extraNumber() {
        assertFalse(service.usesCorrectNumbers("3 + 5 + 4 + 1 + 2", List.of(3, 5, 4, 1)));
    }

    @Test
    void puzzle_1_1_1_1_hasNoSolution() {
        List<String> solutions = service.findSolutions(List.of(1, 1, 1, 1));
        assertTrue(solutions.isEmpty());
    }

    @Test
    void puzzle_8_3_8_3_hasSolution() {
        List<String> solutions = service.findSolutions(List.of(8, 3, 8, 3));
        assertFalse(solutions.isEmpty());
        assertTrue(solutions.stream().anyMatch(s -> s.contains("8 / (3 - (8 / 3))")));
    }
}
