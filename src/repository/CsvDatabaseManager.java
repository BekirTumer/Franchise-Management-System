package repository;

import domain.*;
import service.UserFactory;

import java.io.*;
import java.util.*;

public class CsvDatabaseManager {
    private static final String USERS_FILE = "users.csv";
    private static final String MENU_FILE = "menu.csv";
    private static final String BRANCHES_FILE = "branches.csv";
    private static final String INVENTORY_FILE = "inventory.csv";



    public static void saveUsers(Collection<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {

            writer.println("Role,UserID,Username,Password,ManagedBranchID");
            for (User u : users) {
                String role = "";
                String branchId = "NONE";

                if (u instanceof HQAdmin) role = "ADMIN";
                else if (u instanceof BranchManager) {
                    role = "MANAGER";
                    branchId = ((BranchManager) u).getManagedBranchID();
                }
                else if (u instanceof Applicant) role = "APPLICANT";

                writer.println(role + "," + u.getUserID() + "," + u.getUsername() + "," + u.getPassword() + "," + branchId);
            }
        } catch (IOException e) {
            System.out.println("Error saving users to database: " + e.getMessage());
        }
    }

    public static Map<String, User> loadUsers() {
        Map<String, User> userMap = new HashMap<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) return userMap;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 4) continue;

                String role = data[0];
                String id = data[1];
                String username = data[2];
                String password = data[3];

                User user = null;
                if (role.equals("ADMIN")) user = UserFactory.createUser(UserRole.HQ_ADMIN, id, username, password);
                else if (role.equals("MANAGER")) {
                    user = UserFactory.createUser(UserRole.BRANCH_MANAGER, id, username, password);
                    if (data.length == 5 && !data[4].equals("NONE")) {
                        ((BranchManager) user).setManagedBranchID(data[4]);
                    }
                }
                else if (role.equals("APPLICANT")) user = UserFactory.createUser(UserRole.APPLICANT, id, username, password);

                if (user != null) userMap.put(username, user);
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
        return userMap;
    }



    public static void saveMenu(Collection<MenuItem> menuItems) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MENU_FILE))) {
            writer.println("ItemID,Name,Description,Price");
            for (MenuItem item : menuItems) {
                writer.println(item.getMenuItemID() + "," + item.getName() + "," + item.getDescription() + "," + item.getPrice());
            }
        } catch (IOException e) {
            System.out.println("Error saving menu: " + e.getMessage());
        }
    }

    public static List<MenuItem> loadMenu() {
        List<MenuItem> menuList = new ArrayList<>();
        File file = new File(MENU_FILE);
        if (!file.exists()) return menuList;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    menuList.add(new MenuItem(data[0], data[1], data[2], Double.parseDouble(data[3])));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading menu: " + e.getMessage());
        }
        return menuList;
    }
    public static void saveBranches(Collection<Branch> branches) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BRANCHES_FILE))) {
            writer.println("BranchID,ManagerID,Location,IsActive,RoyaltyType,RoyaltyValue,MonthlyRevenue");
            for (Branch b : branches) {
                writer.println(b.getBranchID() + "," + b.getManagerID() + "," + b.getLocation() + "," +
                        b.isActive() + "," + b.getRoyaltyType() + "," + b.getRoyaltyValue() + "," + b.getMonthlyRevenue());
            }
        } catch (IOException e) {
            System.out.println("Error saving branches: " + e.getMessage());
        }
    }

    public static Map<String, Branch> loadBranches() {
        Map<String, Branch> branchMap = new HashMap<>();
        File file = new File(BRANCHES_FILE);
        if (!file.exists()) return branchMap;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Başlığı atla
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    Branch b = new Branch(data[0], data[1], data[2], Boolean.parseBoolean(data[3]), data[4], Double.parseDouble(data[5]) , Double.parseDouble(data[6]));
                    branchMap.put(b.getBranchID(), b);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading branches: " + e.getMessage());
        }
        return branchMap;
    }

    // --- INVENTORY (ENVANTER) READ/WRITE ---
    public static void saveInventory(Collection<Branch> branches) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INVENTORY_FILE))) {
            writer.println("ItemID,Name,BranchID,CurrentQuantity,ThresholdQuantity");
            for (Branch b : branches) {
                for (InventoryItem item : b.getInventory()) {
                    writer.println(item.getItemID() + "," + item.getItemName() + "," + item.getBranchID() + "," +
                            item.getCurrentQuantity() + "," + item.getThresholdQuantity());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
        }
    }

    public static void loadInventory(Map<String, Branch> branchMap) {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Başlığı atla
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    String branchId = data[2];
                    Branch b = branchMap.get(branchId);
                    if (b != null) { // Yabancı anahtar kontrolü (Şube gerçekten var mı?)
                        InventoryItem item = new InventoryItem(data[0], data[1], branchId, Integer.parseInt(data[3]), Integer.parseInt(data[4]));
                        b.getInventory().add(item);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }
}