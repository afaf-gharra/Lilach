package com.lilach.server.services;

import com.lilach.server.models.Complaint;
import com.lilach.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class ComplaintService {
    
    public static Complaint createComplaint(Complaint complaint) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(complaint);
            transaction.commit();
            EmailService.sendComplaintConfirmation(complaint);

            return complaint;
        }
    }
    
    public static Complaint getComplaintById(int complaintId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Complaint.class, complaintId);
        }
    }
    
    public static List<Complaint> getAllComplaints() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Complaint> query = session.createQuery("FROM Complaint ORDER BY createdAt DESC", Complaint.class);
            return query.list();
        }
    }
    
    public static List<Complaint> getComplaintsByStore(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint WHERE store.id = :storeId ORDER BY createdAt DESC", 
                Complaint.class
            );
            query.setParameter("storeId", storeId);
            return query.list();
        }
    }
    
    public static List<Complaint> getComplaintsByUser(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint WHERE user.id = :userId ORDER BY createdAt DESC", 
                Complaint.class
            );
            query.setParameter("userId", userId);
            return query.list();
        }
    }
    
    public static List<Complaint> getComplaintsByStatus(Complaint.ComplaintStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint WHERE status = :status ORDER BY createdAt DESC", 
                Complaint.class
            );
            query.setParameter("status", status);
            return query.list();
        }
    }
    
    public static Complaint updateComplaint(int complaintId, Complaint complaintUpdates) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Complaint complaint = session.get(Complaint.class, complaintId);
            
            if (complaint != null) {
                // Update fields
                if (complaintUpdates.getType() != null) complaint.setType(complaintUpdates.getType());
                if (complaintUpdates.getDescription() != null) complaint.setDescription(complaintUpdates.getDescription());
                if (complaintUpdates.getDesiredResolution() != null) complaint.setDesiredResolution(complaintUpdates.getDesiredResolution());
                if (complaintUpdates.getResolutionNotes() != null) complaint.setResolutionNotes(complaintUpdates.getResolutionNotes());
                if (complaintUpdates.getCompensation() != null) complaint.setCompensation(complaintUpdates.getCompensation());
                if (complaintUpdates.getStatus() != null) complaint.setStatus(complaintUpdates.getStatus());
                
                complaint.setContactEmail(complaintUpdates.isContactEmail());
                complaint.setContactPhone(complaintUpdates.isContactPhone());
                
                session.update(complaint);
                transaction.commit();
                if (complaintUpdates.getStatus() != null) {
                    EmailService.sendComplaintUpdate(complaint);
                }
                return complaint;
            }
            return null;
        }
    }
    
    public static boolean deleteComplaint(int complaintId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Complaint complaint = session.get(Complaint.class, complaintId);
            
            if (complaint != null) {
                session.delete(complaint);
                transaction.commit();
                return true;
            }
            return false;
        }
    }
    
    public static long getOpenComplaintsCount(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(*) FROM Complaint WHERE store.id = :storeId AND status = 'OPEN'", 
                Long.class
            );
            query.setParameter("storeId", storeId);
            return query.uniqueResult();
        }
    }
    
    public static List<Complaint> getRecentComplaints(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint ORDER BY createdAt DESC", 
                Complaint.class
            );
            query.setMaxResults(limit);
            return query.list();
        }
    }
}