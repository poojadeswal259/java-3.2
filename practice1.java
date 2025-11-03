package com.example.springdemo;

public class Course {
    private String courseName;

    public Course(String courseName) {
        this.courseName = courseName;
    }

    public void displayCourse() {
        System.out.println("Enrolled in course: " + courseName);
    }
}


package com.example.springdemo;

public class Student {
    private Course course;

    // Constructor-based injection
    public Student(Course course) {
        this.course = course;
    }

    public void showInfo() {
        System.out.println("Student details:");
        course.displayCourse();
    }
}


package com.example.springdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Course course() {
        return new Course("Spring Framework - Java Based Configuration");
    }

    @Bean
    public Student student() {
        // Injecting Course bean into Student bean
        return new Student(course());
    }
}


package com.example.springdemo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {

        // Initialize the Spring container with Java-based configuration
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        // Retrieve the Student bean
        Student student = context.getBean(Student.class);

        // Call method to verify dependency injection
        student.showInfo();

        // Close context
        context.close();
    }
}
