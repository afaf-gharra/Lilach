package com.lilach.server.services;

import org.hibernate.Session;
import com.lilach.server.models.User;
import com.lilach.server.utils.HibernateUtil;

public class DatabaseService {
    public static User authenticate(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM User WHERE username = :username AND password = :password", User.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .uniqueResult();
        }
    }
}