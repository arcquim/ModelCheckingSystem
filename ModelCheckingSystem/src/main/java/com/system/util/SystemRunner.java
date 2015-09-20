package com.system.util;

import com.system.view.ConsoleUserInterface;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class SystemRunner {
    
    public static void main(String[] args) {
        ConsoleUserInterface userInterface = ConsoleUserInterface.getInstance();
        userInterface.doWork();
    }
}
