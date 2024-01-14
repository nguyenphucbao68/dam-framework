package org.example.mapper;

import org.example.annotation.Table;

public class ORMManagement {
    private static final ClassScanner classScanner = new ClassScanner();
    private static String packageName;

    public static void setPackageName(String packageName) {
        ORMManagement.packageName = packageName;
    }

    public static String getPackageName() {
        return ORMManagement.packageName;
    }

    public static void save(){
        ORMManagement.classScanner.scanClassesWithAnnotation(ORMManagement.getPackageName(), Table.class);
    }

    public static ClassScanner getClassScanner() {
        return ORMManagement.classScanner;
    }
}
