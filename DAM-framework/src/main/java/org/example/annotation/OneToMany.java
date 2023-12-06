package org.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {
    // Name of the referenced table
    String refTable();

    // Name of the foreign key column in the current table
    String joinColumn();

    // Name of the referenced table's primary key column
    String refColumn();
}