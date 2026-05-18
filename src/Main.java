import domain.*;
import service.*;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        // 1. Servisleri Ayaga Kaldir
        ApplicationService applicationService = new ApplicationService();
        BranchManagementService branchManagementService = new BranchManagementService();
        FranchiseFacade facade = new FranchiseFacade(applicationService, branchManagementService);
        IPaymentGateway paymentGateway = ExternalPaymentService.getInstance();
        FinanceAndReportService financeService = new FinanceAndReportService(paymentGateway);
        InventoryManagementService inventoryService = new InventoryManagementService();
        MenuManagementService menuService = new MenuManagementService();




        Branch defaultBranch = branchManagementService.createBranch("BR-01", "bekir_manager", "Izmir Bornova", "PERCENTAGE", 5.0);


        InventoryItem item1 = new InventoryItem("INV-101", "Kahve Cekirdegi (kg)", "BR-01", 5, 20);
        InventoryItem item2 = new InventoryItem("INV-102", "Karton Bardak", "BR-01", 500, 100);

        defaultBranch.getInventory().add(item1);
        defaultBranch.getInventory().add(item2);


        menuService.addMenuItem(new MenuItem("MNU-01", "Filtre Kahve", "Taze demlenmis Kenya kahvesi", 60.0));
        menuService.addMenuItem(new MenuItem("MNU-02", "Latte", "Bol sutlu espresso", 85.0));




        ConsoleUI ui = new ConsoleUI(applicationService, branchManagementService, facade, financeService, inventoryService, menuService);
        ui.start();
    }
}