package com.system.view;

import com.system.CTLVerificator;
import com.system.Controller;
import com.system.VerificationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class ConsoleUserInterface {
    
    private Controller controller;
    private static ConsoleUserInterface instance;
    private Scanner scanner;
    private static final String PROGRAM_MESSAGE = "Enter program (double ENTER to finish)";
    private static final String FORMULA_MESSAGE = "Enter CTL formula to verify";
    private static final String PREDICATE_MESSAGE = "Enter condition of atomic predicate number ";
    private static final String NEXT_PREDICATE_MESSAGE = "Would you like to enter next predicate? (y/n)";
    private static final String EXAMPLES_MESSAGE = "Would you like to see counterexamples? (y/n)";
    private static final String RESULT_MESSAGE = "The result of verification: ";
    private static final String ERROR_MESSAGE = "There is an error. See logs for much information.";
    private static final String EXAMPLES_ANSWER = "How many counterexamples do you want to see?";
    private static final String YES_ANSWER = "Y";
    private static final String NO_ANSWER = "N";
    private int predicateNumber;
    
    private ConsoleUserInterface() {
        controller = new Controller();
        scanner = new Scanner(System.in);
        predicateNumber = 0;
    }
    
    public static ConsoleUserInterface getInstance() {
        if (instance == null) {
            instance = new ConsoleUserInterface();
        }
        return instance;
    }
    
    public void doWork() {
        controller.setProgram(readProgram());
        List<String> forAtomicPredicates = new ArrayList<>();
        do {
            forAtomicPredicates.add(readNotEmptyString(PREDICATE_MESSAGE + predicateNumber));
            predicateNumber++;
        }
        while (isThereNext(NEXT_PREDICATE_MESSAGE));
        controller.setAtomicPredicates(forAtomicPredicates);
        controller.setCTLFormula(readNotEmptyString(FORMULA_MESSAGE));
        CTLVerificator verificator = controller.startVerification();
        if (verificator == null) {
            System.out.println(ERROR_MESSAGE);
            return;
        }
        VerificationResult result = verificator.getVerificationResult();
        System.out.println(RESULT_MESSAGE + result.toString());
        if (result == VerificationResult.PROPERTY_NOT_HOLDS) {
            if (isThereNext(EXAMPLES_MESSAGE)) {
                List<String> counterexamples = verificator.getCounterexamples(getNumberOfExamples());
                for (String example : counterexamples) {
                    System.out.println(example);
                }
            }
        }
    }
    
    private String readNotEmptyString(String message) {
        System.out.println(message);
        String readString = scanner.nextLine();
        while (readString == null || readString.isEmpty()) {
            System.out.println(message);
            readString = scanner.nextLine();
        }
        return readString;
    }
    
    private boolean isThereNext(String message) {
        System.out.println(message);
        String answer = scanner.nextLine().toUpperCase();
        while (!answer.equals(YES_ANSWER) && !answer.equals(NO_ANSWER)) {
            System.out.println(message);
            answer = scanner.nextLine().toUpperCase();
        }
        return answer.equals(YES_ANSWER);
    }
    
    private int getNumberOfExamples() {
        System.out.println(EXAMPLES_ANSWER);
        int answer = scanner.nextInt();
        while (answer <= 0) {
            System.out.println(EXAMPLES_ANSWER);
            answer = scanner.nextInt();
        }
        return answer;
    }
    
    private String readProgram() {
        System.out.println(PROGRAM_MESSAGE);
        String program = "";
        String currentString = scanner.nextLine();
        while (!currentString.isEmpty()) {
            program += currentString;
            currentString = scanner.nextLine();
        }
        return program;
    }
}
