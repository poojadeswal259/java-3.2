package com.example.springdemo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// --- Course Class ---
class Course {
    private String courseName;

    public Course(String courseName) {
        this.courseName = courseName;
    }

    public void displayCourse() {
        System.out.println("Enrolled in course: " + courseName);
    }
}

// --- Student Class ---
class Student {
    private Course course;

    // Constructor-based dependency injection
    public Student(Course course) {
        this.course = course;
    }

    public void showInfo() {
        System.out.println("Student details:");
        course.displayCourse();
    }
}

// --- Configuration Class ---
@Configuration
class AppConfig {

    @Bean
    public Course course() {
        return new Course("Spring Framework - Java Based Configuration");
    }

    @Bean
    public Student student() {
        // Inject Course bean into Student bean
        return new Student(course());
    }
}

// --- Main Class ---
public class MainApp {
    public static void main(String[] args) {

        // Initialize Spring context using Java configuration
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        // Retrieve the Student bean
        Student student = context.getBean(Student.class);

        // Call method to test dependency injection
        student.showInfo();

        // Close context
        context.close();
    }
}
