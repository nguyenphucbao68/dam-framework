package org.example.mapper;

import org.example.annotation.Table;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ClassScanner {
    private final Map<String, Class<?>> tableToClassMap;

    public ClassScanner() {
        this.tableToClassMap = new HashMap<>();
    }

    public Map<String, Class<?>> getTableToClassMap() {
        return tableToClassMap;
    }

    public void scanClassesWithAnnotation(String packageName, Class<? extends Annotation> annotation) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                File directory = new File(filePath);

                if (directory.isDirectory()) {
                    scanClassesInDirectory(packageName, directory, annotation);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    private void scanClassesInDirectory(String packageName, File directory, Class<? extends Annotation> annotation) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanClassesInDirectory(packageName + "." + file.getName(), file, annotation);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotation)) {
                            Table tableAnnotation = clazz.getAnnotation(Table.class);
                            if (tableAnnotation != null) {
                                tableToClassMap.put(tableAnnotation.name(), clazz);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace(); // Handle the exception according to your needs
                    }
                }
            }
        }
    }
}

