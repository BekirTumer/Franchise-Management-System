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

        loadDummyUsers();
    }

    private void loadDummyUsers() {
        User admin = UserFactory.createUser(UserRole.HQ_ADMIN, "HQ-01", "admin", "1234");
        User manager = UserFactory.createUser(UserRole.BRANCH_MANAGER, "BR-01", "bekir_manager", "1234");
        User applicant = UserFactory.createUser(UserRole.APPLICANT, "AP-01", "ilker_aday", "1234");

        userDatabase.put(admin.getUsername(), admin);
        userDatabase.put(manager.getUsername(), manager);
        userDatabase.put(applicant.getUsername(), applicant);

        User bekir = userDatabase.get("bekir_manager");
        ((BranchManager) bekir).setManagedBranchID("BR-01");
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
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userDatabase.get(username);
            if (user != null && user.login(username, password)) {
                loggedInUser = user;
                System.out.println("Login successful! Welcome, " + user.getUsername());
            } else {
                System.out.println("Error: Invalid username or password.");
            }
        } else if (choice.equals("2")) {
            System.out.print("Enter new Username: ");
            String newUsername = scanner.nextLine();

            if (userDatabase.containsKey(newUsername)) {
                System.out.println("Error: This username is already taken.");
                return;
            }

            System.out.print("Enter new Password: ");
            String newPassword = scanner.nextLine();

            String newId = "APP-" + System.currentTimeMillis();
            User newApplicant = UserFactory.createUser(UserRole.APPLICANT, newId, newUsername, newPassword);
            userDatabase.put(newUsername, newApplicant);
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Invalid choice.");
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
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": {
                    applicationService.WriteAllPendingApplications();
                    System.out.print("Please enter the Application ID you wish to review: ");
                    String applicationID = scanner.nextLine();
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
                        String decision = scanner.nextLine();
                        switch (decision) {
                            case "1": {
                                System.out.println("Please configure the royalty fee strategy for this branch:");
                                System.out.println("1- Fixed Amount");
                                System.out.println("2- Percentage");
                                String stratChoice = scanner.nextLine();
                                String royaltyType = stratChoice.equals("1") ? "FIXED" : "PERCENTAGE";
                                System.out.print("Please enter the value: ");
                                double royaltyValue = Double.parseDouble(scanner.nextLine());

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
                        }
                    } else {
                        System.out.println("There is no such Application ID");
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
                                System.out.println("Requested items: " + rq.getRequestedItems());
                                found = true;
                            }
                        }
                        if (!found) {
                            System.out.println("No pending restock requests found.");
                            break;
                        }
                        System.out.print("Enter Request ID to process (or press Enter to go back): ");
                        String reqId = scanner.nextLine();
                        if (reqId.trim().isEmpty()) {
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
                            String dec = scanner.nextLine();
                            if (dec.equals("1")) {
                                inventoryService.updateRestockStatus(reqId, RestockStatus.FULFILLED);
                                System.out.println("Request FULFILLED. Items shipped to the branch.");
                            } else if (dec.equals("2")) {
                                inventoryService.updateRestockStatus(reqId, RestockStatus.CANCELED);
                                System.out.println("Request CANCELED.");
                            } else {
                                System.out.println("Invalid choice.");
                            }
                        } else {
                            System.out.println("Invalid Request ID.");
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
                        String menuChoice = scanner.nextLine();

                        switch (menuChoice) {
                            case "1": {
                                System.out.print("Item name: ");
                                String itemName = scanner.nextLine();
                                System.out.print("Item description: ");
                                String itemDescription = scanner.nextLine();
                                System.out.print("Item price: ");
                                double price;
                                try {
                                    price = Double.parseDouble(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Error: Price is not a number!");
                                    break;
                                }
                                String itemId = "MNU-" + System.currentTimeMillis();
                                MenuItem newItem = new MenuItem(itemId, itemName, itemDescription, price);
                                menuService.addMenuItem(newItem);
                                System.out.println("New item successfully added to menu!");
                                break;
                            }
                            case "2": {
                                System.out.print("Enter the item ID that will be deleted: ");
                                String itemId = scanner.nextLine();
                                if (menuService.getMenuItem(itemId) == null) {
                                    System.out.println("Error: Item does not exist!");
                                    break;
                                }
                                menuService.removeMenuItem(itemId);
                                System.out.println("Item successfully removed from menu!");
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
                                String id = scanner.nextLine();
                                if (menuService.getMenuItem(id) == null) {
                                    System.out.println("Error: Item does not exist!");
                                    break;
                                }
                                System.out.print("New price: ");
                                double newPrice;
                                try {
                                    newPrice = Double.parseDouble(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Error: Price is not a number!");
                                    break;
                                }
                                menuService.updatePrice(id, newPrice);
                                System.out.println("Price updated successfully!");
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
                                System.out.println("Invalid choice.");
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
                    System.out.println("Invalid choice.");
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
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": {
                    Branch br = branchManagementService.getBranch(branchId);
                    if (br == null) {
                        System.out.println("No active branch registered in the system was found.");
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
                    System.out.println("Please enter the items you want to restock with {,} between them: ");
                    String items = scanner.nextLine();
                    String[] itemArray = items.split(",");
                    List<String> itemList = Arrays.asList(itemArray);
                    RestockRequest rs = new RestockRequest("REQ-" + System.currentTimeMillis(), branchId, itemList, RestockStatus.CREATED, "05-06-2026");
                    inventoryService.addRestockRequest(rs);
                    System.out.println("Your request has been forwarded to head office.");
                    break;
                }
                case "3": {
                    Branch br = branchManagementService.getBranch(branchId);
                    if (br == null) {
                        System.out.println("No active branch registered in the system was found.");
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
                    System.out.println("Invalid choice.");
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
            String choice = scanner.nextLine();
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
                    String applicationID = scanner.nextLine();
                    if (applicationService.getApplication(applicationID) == null) {
                        System.out.println("No such application was found.");
                    } else {
                        FranchiseApplication application = applicationService.getApplication(applicationID);
                        System.out.println("Your application status is: " + application.getStatus());

                        if (application.getStatus().equals(ApplicationStatus.INFO_REQUESTED)) {
                            System.out.println("HQ admin feedback: " + application.getAdminFeedback());
                            System.out.print("Would you like to upload the requested document now? (Y/N): ");
                            String answer = scanner.nextLine().toUpperCase(Locale.ROOT);
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
                                    System.out.println("Invalid choice.");
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
                    System.out.println("Invalid choice.");
                }
            }
        }
    }
}