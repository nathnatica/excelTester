package com.github.nathnatica.validator;

import org.apache.commons.lang3.StringUtils;

public class Argument {

    public enum Action {
        INSERT("insert"),
        DELETE("delete"),
        CHECK("check");

        private String value;
        
        Action(String value) {
            this.value = value;    
        }
        public String getValue() {
            return value;
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
        if (args.length == 2) {
            if (StringUtils.equalsIgnoreCase(args[1], "insert") || StringUtils.equalsIgnoreCase(args[1], "ins")) {
                action = Action.INSERT;
            } else if (StringUtils.equalsIgnoreCase(args[1], "delete") || StringUtils.equalsIgnoreCase(args[1], "del")) {
                action = Action.DELETE;
            } else if (StringUtils.equalsIgnoreCase(args[1], "check") || StringUtils.equalsIgnoreCase(args[1], "che")) {
                action = Action.CHECK;
            } else {
                System.out.println("wrong action name for argument[1]");
                System.out.println(">> ins[ert], che[ck], del[ete] are available");
                return false;
            }
        }
        return true;
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
