package com.lilach.server.services;

import com.lilach.server.models.Complaint;
import com.lilach.server.models.User;

public class EmailService {
    
    public static void sendComplaintConfirmation(Complaint complaint) {
        User user = complaint.getUser();
        String subject = "Complaint Received - Reference #" + complaint.getId();
        String body = String.format(
            "Dear %s,\n\n" +
            "Thank you for bringing your concern to our attention. We have received your complaint regarding order #%d.\n\n" +
            "Complaint Details: %s\n\n" +
            "We will review your complaint and contact you within 24 hours.\n\n" +
            "Best regards,\nLilach Flower Shop Team",
            user.getFullName(), complaint.getOrder().getId(), complaint.getDescription()
        );
        
        // Implement email sending logic here
        System.out.println("Sending email to: " + user.getEmail());
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
    
    public static void sendComplaintUpdate(Complaint complaint) {
        User user = complaint.getUser();
        String subject = "Complaint Update - Reference #" + complaint.getId();
        String body = String.format(
            "Dear %s,\n\n" +
            "Your complaint regarding order #%d has been updated.\n\n" +
            "Current Status: %s\n" +
            "Resolution Notes: %s\n" +
            "Compensation: %s\n\n" +
            "If you have any questions, please contact us.\n\n" +
            "Best regards,\nLilach Flower Shop Team",
            user.getFullName(), 
            complaint.getOrder().getId(), 
            complaint.getStatus().toString(),
            complaint.getResolutionNotes() != null ? complaint.getResolutionNotes() : "N/A",
            complaint.getCompensation() != null ? "$" + complaint.getCompensation() : "N/A"
        );
        
        // Implement email sending logic here
        System.out.println("Sending update email to: " + user.getEmail());
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}