package ui;

import domain.*;
import service.*;

import java.util.*;

public class ConsoleUI {
    private ApplicationService applicationService;
    private BranchManagementService branchManagementService;
    private FranchiseFacade facade;
    private FinanceAndReportService financeService;
    private InventoryManagementService inventoryService;
    private MenuManagementService menuService;

    private Map<String, User> userDatabase = new HashMap<>();
    private Scanner scanner;
    private User loggedInUser;

    public ConsoleUI(ApplicationService appService, BranchManagementService branchService,
                     FranchiseFacade facade, FinanceAndReportService financeService,
                     InventoryManagementService inventoryService, MenuManagementService menuService) {
        this.applicationService = appService;
        this.branchManagementService = branchService;
        this.facade = facade;
        this.financeService = financeService;
        this.inventoryService = inventoryService;
        this.menuService = menuService;
        this.scanner = new Scanner(System.in);

        // 1. Kullanıcıları Yükle
        this.userDatabase = repository.CsvDatabaseManager.loadUsers();

        // 2. Menüyü Yükle
        List<MenuItem> loadedMenu = repository.CsvDatabaseManager.loadMenu();
        for(MenuItem item : loadedMenu) {
            this.menuService.addMenuItem(item);
        }

        // 3. Şubeleri Yükle
        Map<String, Branch> loadedBranches = repository.CsvDatabaseManager.loadBranches();
        for (Branch b : loadedBranches.values()) {
            this.branchManagementService.addBranch(b);
        }

        // 4. Envanterleri Yükle (ve otomatik olarak ilgili şubelere dağıt)
        repository.CsvDatabaseManager.loadInventory(loadedBranches);
    }



    public void start() {
        System.out.println("=== Welcome to Franchise Management System ===");

        while (true) {
            if (loggedInUser == null) {
                showLoginScreen();
            } else {
                showRoleBasedMenu();
            }
        }
    }

    private void showLoginScreen() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1- Login");
        System.out.println("2- Register New Applicant");
        System.out.print("Your choice: ");
        String choice = scanner.nextLine().trim(); // TRIM EKLENDİ

        if (choice.equals("1")) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            User user = userDatabase.get(username);
            if (user != null && user.login(username, password)) {
                loggedInUser = user;
                System.out.println("Login successful! Welcome, " + user.getUsername());
            } else {
                System.out.println("Error: Invalid username or password.");
            }
        } else if (choice.equals("2")) {
            System.out.print("Enter new Username: ");
            String newUsername = scanner.nextLine().trim();

            if (userDatabase.containsKey(newUsername)) {
                System.out.println("Error: This username is already taken.");
                return;
            }

            System.out.print("Enter new Password: ");
            String newPassword = scanner.nextLine().trim();

            String newId = "APP-" + System.currentTimeMillis();
            User newApplicant = UserFactory.createUser(UserRole.APPLICANT, newId, newUsername, newPassword);
            userDatabase.put(newUsername, newApplicant);
            repository.CsvDatabaseManager.saveUsers(userDatabase.values());
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Error: Invalid choice.");
        }
    }

    private void showRoleBasedMenu() {
        if (loggedInUser instanceof HQAdmin) {
            showAdminMenu();
        } else if (loggedInUser instanceof BranchManager) {
            showManagerMenu();
        } else if (loggedInUser instanceof Applicant) {
            showApplicantMenu();
        }
    }

    private void showAdminMenu() {
        while (true) {
            System.out.println("\n[HQ ADMIN MENU]");
            System.out.println("1- Review and Assess The Application");
            System.out.println("2- Manage Restock Requests");
            System.out.println("3- Menu Management");
            System.out.println("4- View Global Financial Reports");
            System.out.println("5- Log Out");

            System.out.print("Your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": {
                    applicationService.WriteAllPendingApplications();
                    System.out.print("Please enter the Application ID you wish to review: ");
                    String applicationID = scanner.nextLine().trim();
                    if (applicationService.getApplication(applicationID) != null) {
                        FranchiseApplication application = applicationService.getApplication(applicationID);
                        System.out.println("Current Application:");
                        System.out.println("Status: " + application.getStatus());
                        System.out.println("Financial status: " + application.getFinancialData());
                        System.out.println("Personal information: " + application.getPersonalData());
                        System.out.println("----Uploaded Documents----");
                        if (application.getDocuments().isEmpty()) {
                            System.out.println("No documents found.");
                        } else {
                            for (Document doc : application.getDocuments()) {
                                System.out.println("  -Type: " + doc.getDocumentType() + " | Content: " + doc.getContent());
                            }
                        }
                        System.out.println("What is your decision: ");
                        System.out.println("1- Approve");
                        System.out.println("2- Reject");
                        System.out.println("3- Request extra information");
                        String decision = scanner.nextLine().trim();
                        switch (decision) {
                            case "1": {
                                System.out.println("Please configure the royalty fee strategy for this branch:");
                                System.out.println("1- Fixed Amount");
                                System.out.println("2- Percentage");
                                String stratChoice = scanner.nextLine().trim();
                                String royaltyType = stratChoice.equals("1") ? "FIXED" : "PERCENTAGE";

                                // KALKAN DEVREDE: Hoca harf girerse çökmez, tekrar sorar!
                                double royaltyValue = getValidDoubleInput("Please enter the value: ");

                                String newBranchId = "BR-" + System.currentTimeMillis();
                                String applicantId = application.getApplicantID();
                                User applicantToPromote = null;
                                for (User u : userDatabase.values()) {
                                    if (u.getUserID().equals(applicantId)) {
                                        applicantToPromote = u;
                                        break;
                                    }
                                }
                                if (applicantToPromote != null) {
                                    facade.approveAndOpenBranch(applicationID, newBranchId, applicantToPromote.getUsername(), application.getLocation(), royaltyType, royaltyValue);
                                    User newManager = UserFactory.createUser(UserRole.BRANCH_MANAGER, "BRM-" + System.currentTimeMillis(),
                                            applicantToPromote.getUsername(), applicantToPromote.getPassword());
                                    ((BranchManager) newManager).setManagedBranchID(newBranchId);
                                    userDatabase.remove(applicantToPromote.getUsername());
                                    userDatabase.put(newManager.getUsername(), newManager);
                                }

                                System.out.println("Application approved successfully with " + royaltyType + " strategy!");
                                repository.CsvDatabaseManager.saveUsers(userDatabase.values());
                                repository.CsvDatabaseManager.saveBranches(branchManagementService.getAllBranches());
                                break;
                            }
                            case "2": {
                                applicationService.getApplication(applicationID).setStatus(ApplicationStatus.REJECTED);
                                System.out.println("Application is rejected.");
                                break;
                            }
                            case "3": {
                                applicationService.getApplication(applicationID).setStatus(ApplicationStatus.INFO_REQUESTED);
                                System.out.println("Please enter the specific information/document requested from the applicant:");
                                String requestedDocument = scanner.nextLine();
                                application.setAdminFeedback(requestedDocument);
                                System.out.println("Your request for extra information has been received!");
                                break;
                            }
                            default: {
                                System.out.println("Error: Invalid choice.");
                            }
                        }
                    } else {
                        System.out.println("Error: There is no such Application ID");
                    }
                    break;
                }
                case "2": {
                    boolean backToAdminMenu = false;
                    while (!backToAdminMenu) {
                        System.out.println("\n--- Pending Restock Requests ---");
                        boolean found = false;
                        for (RestockRequest rq : inventoryService.getAllRequests()) {
                            if (rq.getStatus() == RestockStatus.CREATED || rq.getStatus() == RestockStatus.PROCESSING) {
                                System.out.println("Request ID: " + rq.getRequestId() + " | Branch ID: " + rq.getBranchId() + " | Status: " + rq.getStatus());
                                System.out.println("Requested items:");
                                for (InventoryItem reqItem : rq.getRequestedItems()) {
                                    System.out.println("  -> Name: " + reqItem.getItemName() + " | Quantity: " + reqItem.getCurrentQuantity());
                                }
                                found = true;
                            }
                        }
                        if (!found) {
                            System.out.println("No pending restock requests found.");
                            break;
                        }
                        System.out.print("Enter Request ID to process (or press Enter to go back): ");
                        String reqId = scanner.nextLine().trim();
                        if (reqId.isEmpty()) {
                            backToAdminMenu = true;
                            break;
                        }
                        RestockRequest rq = inventoryService.getRestockRequest(reqId);
                        if (rq != null) {
                            inventoryService.updateRestockStatus(reqId, RestockStatus.PROCESSING);
                            System.out.println("Request is now PROCESSING. What is your final decision?");
                            System.out.println("1- Fulfill (Ship Inventory)");
                            System.out.println("2- Cancel (Deny Request)");
                            System.out.print("Your choice: ");
                            String dec = scanner.nextLine().trim();
                            if (dec.equals("1")) {
                                inventoryService.updateRestockStatus(reqId, RestockStatus.FULFILLED);


                                Branch targetBranch = branchManagementService.getBranch(rq.getBranchId());
                                if (targetBranch != null) {
                                    for (InventoryItem reqItem : rq.getRequestedItems()) {
                                        boolean isItemAlreadyInBranch = false;


                                        for (InventoryItem branchItem : targetBranch.getInventory()) {
                                            if (branchItem.getItemID().equals(reqItem.getItemID())) {
                                                branchItem.setCurrentQuantity(branchItem.getCurrentQuantity() + reqItem.getCurrentQuantity());
                                                isItemAlreadyInBranch = true;
                                                break;
                                            }
                                        }


                                        if (!isItemAlreadyInBranch) {
                                            targetBranch.getInventory().add(reqItem);
                                        }
                                    }
                                }


                                System.out.println("Request FULFILLED. Items shipped and branch inventory updated.");
                                repository.CsvDatabaseManager.saveInventory(branchManagementService.getAllBranches());
                            } else if (dec.equals("2")) {
                                inventoryService.updateRestockStatus(reqId, RestockStatus.CANCELED);
                                System.out.println("Request CANCELED.");
                            } else {
                                System.out.println("Error: Invalid choice.");
                            }
                        } else {
                            System.out.println("Error: Invalid Request ID.");
                        }
                    }
                    break;
                }
                case "3": {
                    boolean backToAdminMenu = false;
                    while (!backToAdminMenu) {
                        System.out.println("\n--- Menu Management System ---");
                        System.out.println("1- Add new item");
                        System.out.println("2- Delete item");
                        System.out.println("3- Update the price of existing item");
                        System.out.println("4- View all menu");
                        System.out.println("5- Back to Main Menu");
                        System.out.print("Your choice: ");
                        String menuChoice = scanner.nextLine().trim();

                        switch (menuChoice) {
                            case "1": {
                                System.out.print("Item name: ");
                                String itemName = scanner.nextLine();
                                System.out.print("Item description: ");
                                String itemDescription = scanner.nextLine();

                                // KALKAN DEVREDE: Harf girilirse tekrar sorar
                                double price = getValidDoubleInput("Item price: ");

                                String itemId = "MNU-" + System.currentTimeMillis();
                                MenuItem newItem = new MenuItem(itemId, itemName, itemDescription, price);
                                menuService.addMenuItem(newItem);
                                repository.CsvDatabaseManager.saveMenu(menuService.getAllMenuItems());
                                System.out.println("New item successfully added to menu!");
                                repository.CsvDatabaseManager.saveMenu(menuService.getAllMenuItems());
                                break;
                            }
                            case "2": {
                                if (menuService.getAllMenuItems().isEmpty()) {
                                    System.out.println("Menu is empty! Nothing to delete.");
                                    break;
                                }
                                // UX İYİLEŞTİRMESİ: Silmeden önce menüyü gösterir
                                System.out.println("--- Current Menu Items ---");
                                for (MenuItem item : menuService.getAllMenuItems()) {
                                    System.out.println("ID: " + item.getMenuItemID() + " | Name: " + item.getName());
                                }
                                System.out.print("Enter the item ID that will be deleted: ");
                                String itemId = scanner.nextLine().trim();
                                if (menuService.getMenuItem(itemId) == null) {
                                    System.out.println("Error: Item does not exist!");
                                    break;
                                }
                                menuService.removeMenuItem(itemId);
                                System.out.println("Item successfully removed from menu!");
                                repository.CsvDatabaseManager.saveMenu(menuService.getAllMenuItems());
                                break;
                            }
                            case "3": {
                                if (menuService.getAllMenuItems().isEmpty()) {
                                    System.out.println("Menu is empty!");
                                    break;
                                }
                                for (MenuItem item : menuService.getAllMenuItems()) {
                                    System.out.println("ID: " + item.getMenuItemID() + " | Name: " + item.getName() + " | Price: " + item.getPrice() + " TL");
                                }
                                System.out.print("Enter the ID of the item you want to update its price: ");
                                String id = scanner.nextLine().trim();
                                if (menuService.getMenuItem(id) == null) {
                                    System.out.println("Error: Item does not exist!");
                                    break;
                                }

                                // KALKAN DEVREDE: Harf girilirse tekrar sorar
                                double newPrice = getValidDoubleInput("New price: ");

                                menuService.updatePrice(id, newPrice);
                                System.out.println("Price updated successfully!");
                                repository.CsvDatabaseManager.saveMenu(menuService.getAllMenuItems());
                                break;
                            }
                            case "4": {
                                if (menuService.getAllMenuItems().isEmpty()) {
                                    System.out.println("Menu is empty!");
                                } else {
                                    for (MenuItem item : menuService.getAllMenuItems()) {
                                        System.out.println("ID: " + item.getMenuItemID() + " | Name: " + item.getName() + " | Price: " + item.getPrice() + " TL | Description: " + item.getDescription());
                                    }
                                }
                                break;
                            }
                            case "5": {
                                backToAdminMenu = true;
                                break;
                            }
                            default: {
                                System.out.println("Error: Invalid choice.");
                            }
                        }
                    }
                    break;
                }
                case "4": {
                    Collection<Branch> branches = branchManagementService.getAllBranches();
                    financeService.printGlobalFinancialReport(branches);
                    break;
                }
                case "5": {
                    loggedInUser = null;
                    return;
                }
                default: {
                    System.out.println("Error: Invalid choice.");
                }
            }
        }
    }

    private void showManagerMenu() {
        BranchManager manager = (BranchManager) loggedInUser;
        String branchId = manager.getManagedBranchID();
        while (true) {
            System.out.println("\n[BRANCH MANAGER MENU]");
            System.out.println("1- View Branch Inventory");
            System.out.println("2- Request Restock");
            System.out.println("3- Pay Royalty Fee");
            System.out.println("4- View Financial Report");
            System.out.println("5- Log Out");

            System.out.print("Your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": {
                    Branch br = branchManagementService.getBranch(branchId);
                    if (br == null) {
                        System.out.println("Error: No active branch registered in the system was found.");
                        break;
                    }
                    if (br.getInventory().isEmpty()) {
                        System.out.println("Inventory is empty");
                    } else {
                        for (InventoryItem item : br.getInventory()) {
                            String alert = "";
                            if (item.getCurrentQuantity() < item.getThresholdQuantity()) {
                                alert = " [!! Critical Stock Level !!]";
                            }
                            System.out.println("Item ID: " + item.getItemID() + " | Name: " + item.getItemName() + " | Current Quantity: " + item.getCurrentQuantity() + alert);
                        }
                    }
                    break;
                }
                case "2": {
                    Branch br = branchManagementService.getBranch(branchId);
                    if (br == null) {
                        System.out.println("Error: No active branch found.");
                        break;
                    }

                    System.out.println("--- Request Restock ---");
                    List<InventoryItem> requestedItems = new ArrayList<>();
                    boolean ordering = true;

                    while (ordering) {
                        System.out.println("\n1- Restock an EXISTING item");
                        System.out.println("2- Request a completely NEW item");
                        System.out.println("3- Finish and Submit Request");
                        System.out.print("Your choice: ");
                        String subChoice = scanner.nextLine().trim();

                        switch (subChoice) {
                            case "1":
                                if (br.getInventory().isEmpty()) {
                                    System.out.println("Error: Your inventory is empty. Please request a NEW item first.");
                                    break;
                                }
                                System.out.println("--- Existing Inventory ---");
                                for (InventoryItem item : br.getInventory()) {
                                    System.out.println("ID: " + item.getItemID() + " | Name: " + item.getItemName());
                                }
                                System.out.print("Enter Item ID to restock: ");
                                String inputId = scanner.nextLine().trim();

                                InventoryItem existingItem = null;
                                for (InventoryItem item : br.getInventory()) {
                                    if (item.getItemID().equalsIgnoreCase(inputId)) {
                                        existingItem = item;
                                        break;
                                    }
                                }
                                if (existingItem == null) {
                                    System.out.println("Error: Item ID not found.");
                                    break;
                                }

                                int qty = getValidIntegerInput("Enter additional quantity for " + existingItem.getItemName() + ": ");
                                // Geçici bir nesne yaratarak isteği sepete ekliyoruz
                                InventoryItem reqExisting = new InventoryItem(existingItem.getItemID(), existingItem.getItemName(), branchId, qty, existingItem.getThresholdQuantity());
                                requestedItems.add(reqExisting);
                                System.out.println("Added to request cart.");
                                break;

                            case "2":
                                System.out.print("Enter the name of the NEW item (e.g., Napkin): ");
                                String newName = scanner.nextLine().trim();
                                int newQty = getValidIntegerInput("Enter initial quantity: ");
                                int newThreshold = getValidIntegerInput("Enter critical stock threshold for this item: ");

                                String newId = "INV-" + System.currentTimeMillis(); // YENİ ÜRÜN ID'Sİ
                                InventoryItem newItem = new InventoryItem(newId, newName, branchId, newQty, newThreshold);
                                requestedItems.add(newItem);
                                System.out.println("New item added to request cart.");
                                break;

                            case "3":
                                ordering = false;
                                break;

                            default:
                                System.out.println("Error: Invalid choice.");
                        }
                    }

                    if (requestedItems.isEmpty()) {
                        System.out.println("Restock request cancelled (No items added).");
                        break;
                    }

                    RestockRequest rs = new RestockRequest("REQ-" + System.currentTimeMillis(), branchId, requestedItems, RestockStatus.CREATED, "May 2026");
                    inventoryService.addRestockRequest(rs);
                    System.out.println("Your request has been forwarded to head office.");
                    break;
                }
                case "3": {
                    Branch br = branchManagementService.getBranch(branchId);
                    if (br == null) {
                        System.out.println("Error: No active branch registered in the system was found.");
                        break;
                    }
                    double rev = br.getMonthlyRevenue();
                    System.out.println("Your automatically calculated revenue for this month is: " + rev + " TL");

                    IRoyaltyCalculationStrategy strategy;
                    if (br.getRoyaltyType().equals("FIXED")) {
                        strategy = new FixedRoyaltyStrategy(br.getRoyaltyValue());
                    } else {
                        strategy = new PercentageRoyaltyStrategy(br.getRoyaltyValue());
                    }
                    RoyaltyPayment receipt = financeService.processRoyaltyFee(branchId, rev, strategy);
                    System.out.println("Payment is successful!");
                    System.out.println("Receipt ID: " + receipt.getPaymentId() + "\nReceipt Date: " + receipt.getPaymentDate() + "\nBranch ID: " + receipt.getBranchId() + "\nAmount: " + receipt.getAmount() + " TL");
                    break;
                }
                case "4": {
                    System.out.println("--- Local Financial Report ---");
                    Branch br = branchManagementService.getBranch(branchId);
                    if (br == null) {
                        System.out.println("Error: No active branch found.");
                        break;
                    }
                    FinancialReport report = financeService.generateBranchReport(br, "May 2026");
                    System.out.println("Report ID: " + report.getReportId());
                    System.out.println("Period: " + report.getPeriod());
                    System.out.println(String.format("Total Revenue: %.2f TL", report.getTotalRevenue()));
                    System.out.println(String.format("Total Expenses: %.2f TL", report.getTotalExpenses()));
                    System.out.println("------------------------------");
                    System.out.println(String.format("Net Profit: %.2f TL", report.getNetProfit()));
                    break;
                }
                case "5": {
                    loggedInUser = null;
                    return;
                }
                default: {
                    System.out.println("Error: Invalid choice.");
                }
            }
        }
    }

    private void showApplicantMenu() {
        while (true) {
            System.out.println("\n[APPLICANT MENU]");
            System.out.println("1- Apply for a new Franchise");
            System.out.println("2- Check the status of application");
            System.out.println("3- Log Out");

            System.out.print("Your choice: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": {
                    System.out.print("Please enter your financial status: ");
                    String financialState = scanner.nextLine();
                    System.out.print("Please enter your personal experience: ");
                    String experience = scanner.nextLine();
                    System.out.print("Please enter the location for your branch: ");
                    String location = scanner.nextLine();
                    String applicationID = "APP-" + System.currentTimeMillis();
                    FranchiseApplication application = new FranchiseApplication(applicationID, loggedInUser.getUserID(), ApplicationStatus.PENDING, financialState, experience, location);
                    applicationService.submitApplication(application);
                    System.out.print("Please enter the name of the document to upload: ");
                    String docName = scanner.nextLine();
                    Document doc = new Document("DOC-" + System.currentTimeMillis(), applicationID, "Official", docName);
                    application.getDocuments().add(doc);
                    System.out.println("Your application has been submitted.");
                    System.out.println("Please save your Application ID: " + applicationID);
                    break;
                }
                case "2": {
                    System.out.print("Please enter the ID of application that you want to question: ");
                    String applicationID = scanner.nextLine().trim();
                    if (applicationService.getApplication(applicationID) == null) {
                        System.out.println("Error: No such application was found.");
                    } else {
                        FranchiseApplication application = applicationService.getApplication(applicationID);
                        System.out.println("Your application status is: " + application.getStatus());

                        if (application.getStatus().equals(ApplicationStatus.INFO_REQUESTED)) {
                            System.out.println("HQ admin feedback: " + application.getAdminFeedback());
                            System.out.print("Would you like to upload the requested document now? (Y/N): ");
                            String answer = scanner.nextLine().trim().toUpperCase();
                            switch (answer) {
                                case "Y": {
                                    System.out.print("Please enter the name of the new document: ");
                                    String newDocName = scanner.nextLine();

                                    Document doc = new Document("DOC-" + System.currentTimeMillis(), applicationID, "RequestedInfo", newDocName);
                                    application.getDocuments().add(doc);

                                    application.setStatus(ApplicationStatus.PENDING);
                                    System.out.println("Your document has been added and your application is back under review.");
                                    break;
                                }
                                case "N": {
                                    System.out.println("Your application is rejected due to lack of information.");
                                    application.setStatus(ApplicationStatus.REJECTED);
                                    break;
                                }
                                default: {
                                    System.out.println("Error: Invalid choice.");
                                }
                            }
                        }
                    }
                    break;
                }
                case "3": {
                    loggedInUser = null;
                    return;
                }
                default: {
                    System.out.println("Error: Invalid choice.");
                }
            }
        }
    }

    // --- YARDIMCI METOTLAR (HOCA KALKANI - DEFENSIVE PROGRAMMING) ---
    private double getValidDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid number (e.g., 10.5 or 100).");
            }
        }
    }

    private int getValidIntegerInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid integer (e.g., 5).");
            }
        }
    }
}