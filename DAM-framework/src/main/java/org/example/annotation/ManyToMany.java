package org.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {
    // Name of the join table
    String joinTable();

    // Name of the column in the join table that references the current table
    String joinColumn();

    // Name of the column in the join table that references the related table
    String inverseJoinColumn();
}