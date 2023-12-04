package org.example;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface VeryImportant {
    int times();

    int test();
}

public class Test {
    @VeryImportant(times = 3, test = 5)
    public void meow(){
        System.out.println("meow");
    }
}
