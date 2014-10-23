package com.github.tester.validator;

import org.apache.commons.lang3.StringUtils;

public class Argument {

    public enum Action {
        INSERT("insert"),
        DELETE("delete"),
        CHECK("check");

        private String value;
        private int start = 1;
        private int end = 1;
        private int current;
        Action(String value) {
            this.value = value;    
        }
        public String getValue() {
            return value;
        }
        public int getStart() {
            return start; 
        }
        public int getEnd() {
            return end;
        }
        public int getCurrent() {
            return current;    
        }
        public void setCurrent(int cur) {
            current = cur;
        }
    }
    
    public static Action action = Action.CHECK;
    
    public static boolean isValid(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("needs argument[0] - excel file name");
            return false;
        }
        if (!args[0].contains(".xls")) {
            System.out.println("needs argument[0] - excel file name");
            return false;
        }
        if (args.length >= 2) {
            if (decideAction(args[1])) return false;
        }
        
        if (args.length == 4) {
            int start = Integer.parseInt(args[2]);
            int end = Integer.parseInt(args[3]);
            action.start = start;
            action.end = end;
        }
        return true;
    }

    public static boolean decideAction(String arg) {
        if (StringUtils.equalsIgnoreCase(arg, "insert") || StringUtils.equalsIgnoreCase(arg, "ins")) {
            action = Action.INSERT;
        } else if (StringUtils.equalsIgnoreCase(arg, "delete") || StringUtils.equalsIgnoreCase(arg, "del")) {
            action = Action.DELETE;
        } else if (StringUtils.equalsIgnoreCase(arg, "check") || StringUtils.equalsIgnoreCase(arg, "che")) {
            action = Action.CHECK;
        } else {
            System.out.println("wrong action name for argument[1]");
            System.out.println(">> ins[ert], che[ck], del[ete] are available");
            return true;
        }
        return false;
    }

    public static boolean isInsertAction() {
        return action == Action.INSERT;
    }
    
    public static boolean isDeleteAction() {
        return action == Action.DELETE;
    }
    public static boolean isCheckAction() {
        return action == Action.CHECK;
    }
}
