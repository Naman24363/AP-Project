package edu.univ.erp.auth;

import edu.univ.erp.service.AdminService;
import java.sql.SQLException;

/**
 * Small CLI helper to delete sections by id using AdminService.
 * Usage: run with section ids as arguments, e.g. `java
 * edu.univ.erp.auth.AdminCleanup 1 2 3`.
 */
public class AdminCleanup {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(
                    "No section ids provided. Usage: java edu.univ.erp.auth.AdminCleanup <id> [<id> ...] or 'purge'");
            System.out.println("Defaulting to sample ids from screenshot: 9 1 2 3 8 4 7 5 6");
            args = new String[] { "9", "1", "2", "3", "8", "4", "7", "5", "6" };
        }
        // create an admin session. Adjust userId/username if your admin has different
        // values.
        Session admin = new Session(1, "admin1", "ADMIN");
        Session.set(admin);
        AdminService svc = new AdminService();
        // support a special "purge" command to remove all courses/sections
        if (args.length == 1 && "purge".equalsIgnoreCase(args[0])) {
            try {
                System.out.println("Purging all courses and sections (destructive)...");
                svc.purgeAllCoursesAndSections(admin);
                System.out.println("Purge complete.");
            } catch (SQLException se) {
                System.err.println("SQL error during purge: " + se.getMessage());
            }
        } else {
            for (String a : args) {
                try {
                    int id = Integer.parseInt(a);
                    System.out.println("Deleting section id=" + id + " ...");
                    svc.deleteSection(admin, id);
                    System.out.println("Deleted section " + id);
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid id: " + a);
                } catch (SQLException se) {
                    System.err.println("SQL error deleting section " + a + ": " + se.getMessage());
                } catch (RuntimeException re) {
                    System.err.println("Skipped section " + a + ": " + re.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error for section " + a + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Done.");
    }
}
