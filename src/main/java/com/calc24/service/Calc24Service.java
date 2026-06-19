package com.calc24.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class Calc24Service {

    private static final double TARGET = 24.0;
    private static final double EPSILON = 1e-9;
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 13;
    private static final int COUNT = 4;

    private record Expr(double value, String repr) {}

    public List<Integer> generatePuzzle() {
        var rng = ThreadLocalRandom.current();
        while (true) {
            List<Integer> numbers = rng.ints(COUNT, MIN_NUMBER, MAX_NUMBER + 1).boxed().toList();
            if (!findSolutions(numbers).isEmpty()) {
                return numbers;
            }
        }
    }

    public List<String> findSolutions(List<Integer> numbers) {
        if (numbers == null || numbers.size() != COUNT) {
            throw new IllegalArgumentException("Exactly 4 numbers required");
        }
        Set<String> solutions = new LinkedHashSet<>();
        List<Expr> exprs = numbers.stream()
                .map(n -> new Expr(n, String.valueOf(n)))
                .toList();
        solveRecursive(exprs, solutions);
        return List.copyOf(solutions);
    }

    private void solveRecursive(List<Expr> exprs, Set<String> solutions) {
        if (exprs.size() == 1) {
            Expr e = exprs.getFirst();
            if (Math.abs(e.value() - TARGET) < EPSILON) {
                solutions.add(e.repr() + " = 24");
            }
            return;
        }

        for (int i = 0; i < exprs.size(); i++) {
            for (int j = 0; j < exprs.size(); j++) {
                if (i == j) continue;

                List<Expr> remaining = new ArrayList<>();
                for (int k = 0; k < exprs.size(); k++) {
                    if (k != i && k != j) remaining.add(exprs.get(k));
                }

                Expr a = exprs.get(i);
                Expr b = exprs.get(j);

                if (i < j) {
                    tryOperator(remaining, solutions, a, b, "+", a.value() + b.value());
                    tryOperator(remaining, solutions, a, b, "*", a.value() * b.value());
                }
                tryOperator(remaining, solutions, a, b, "-", a.value() - b.value());
                if (Math.abs(b.value()) > EPSILON) {
                    tryOperator(remaining, solutions, a, b, "/", a.value() / b.value());
                }
            }
        }
    }

    private void tryOperator(List<Expr> remaining, Set<String> solutions,
                             Expr a, Expr b, String op, double result) {
        String repr = "(" + a.repr() + " " + op + " " + b.repr() + ")";
        remaining.add(new Expr(result, repr));
        solveRecursive(remaining, solutions);
        remaining.removeLast();
    }

    public double evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression must not be null or blank");
        }
        String expr = expression.replaceAll("\\s+", "");
        int[] pos = {0};
        double result = parseExpression(expr, pos);
        if (pos[0] != expr.length()) {
            throw new IllegalArgumentException("Unexpected characters at position " + pos[0]);
        }
        return result;
    }

    private double parseExpression(String expr, int[] pos) {
        double result = parseTerm(expr, pos);
        while (pos[0] < expr.length()) {
            char c = expr.charAt(pos[0]);
            if (c == '+' || c == '-') {
                pos[0]++;
                double term = parseTerm(expr, pos);
                result = (c == '+') ? result + term : result - term;
            } else {
                break;
            }
        }
        return result;
    }

    private double parseTerm(String expr, int[] pos) {
        double result = parseFactor(expr, pos);
        while (pos[0] < expr.length()) {
            char c = expr.charAt(pos[0]);
            if (c == '*' || c == '/') {
                pos[0]++;
                double factor = parseFactor(expr, pos);
                if (c == '*') {
                    result *= factor;
                } else {
                    if (Math.abs(factor) < EPSILON) {
                        throw new ArithmeticException("Division by zero");
                    }
                    result /= factor;
                }
            } else {
                break;
            }
        }
        return result;
    }

    private double parseFactor(String expr, int[] pos) {
        if (pos[0] >= expr.length()) {
            throw new IllegalArgumentException("Unexpected end of expression");
        }
        char c = expr.charAt(pos[0]);
        if (c == '(') {
            pos[0]++;
            double result = parseExpression(expr, pos);
            if (pos[0] >= expr.length() || expr.charAt(pos[0]) != ')') {
                throw new IllegalArgumentException("Missing closing parenthesis");
            }
            pos[0]++;
            return result;
        }
        if (Character.isDigit(c)) {
            int start = pos[0];
            while (pos[0] < expr.length() && Character.isDigit(expr.charAt(pos[0]))) {
                pos[0]++;
            }
            return Double.parseDouble(expr.substring(start, pos[0]));
        }
        throw new IllegalArgumentException("Unexpected character: " + c);
    }

    public boolean usesCorrectNumbers(String expression, List<Integer> numbers) {
        if (expression == null || numbers == null) {
            return false;
        }
        String expr = expression.replaceAll("\\s+", "");
        List<Integer> exprNumbers = new ArrayList<>();
        for (int i = 0; i < expr.length(); i++) {
            if (Character.isDigit(expr.charAt(i))) {
                int start = i;
                while (i < expr.length() && Character.isDigit(expr.charAt(i))) {
                    i++;
                }
                exprNumbers.add(Integer.parseInt(expr.substring(start, i)));
                i--;
            }
        }
        List<Integer> expected = new ArrayList<>(numbers);
        List<Integer> actual = new ArrayList<>(exprNumbers);
        expected.sort(Integer::compareTo);
        actual.sort(Integer::compareTo);
        return expected.equals(actual);
    }
}
