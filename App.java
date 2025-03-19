package com.edubridge_mini_project.restaurant_ordering_system;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        SessionFactory factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        Session session = factory.openSession();
        Restaurant restaurant = new Restaurant(session);
        restaurant.run();
        session.close();
        factory.close();
    }
}
