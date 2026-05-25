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






        ConsoleUI ui = new ConsoleUI(applicationService, branchManagementService, facade, financeService, inventoryService, menuService);
        ui.start();
    }
}