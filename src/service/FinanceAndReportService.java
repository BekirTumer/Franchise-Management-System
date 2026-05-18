package service;


import com.sun.security.jgss.GSSUtil;
import domain.Branch;
import domain.FinancialReport;
import domain.IPaymentGateway;
import domain.RoyaltyPayment;

import java.util.Collection;

public class FinanceAndReportService {
    private IPaymentGateway paymentGateway;
    public FinanceAndReportService(IPaymentGateway paymentGateway){
        this.paymentGateway = paymentGateway;
    }
    public RoyaltyPayment processRoyaltyFee(String branchId, double branchRevenue, IRoyaltyCalculationStrategy calculationStrategy){
        double fee=calculationStrategy.calculateRoyalty(branchRevenue);
        boolean isSuccess= paymentGateway.executeTransaction(fee, branchId);
        String paymentId="PAY-"+ System.currentTimeMillis();
        return new RoyaltyPayment(paymentId,branchId,fee,"05-06-2005",isSuccess);
    }

    public FinancialReport generateBranchReport(Branch branch, String period) {
        double actualRevenue = branch.getMonthlyRevenue();
        double simulatedExpenses = actualRevenue * 0.60;
        double netProfit = actualRevenue - simulatedExpenses;

        return new FinancialReport("REP-" + System.currentTimeMillis() , branch.getBranchID(), period,actualRevenue,simulatedExpenses,netProfit);
    }

    public void printGlobalFinancialReport(Collection<Branch> allBranches){
        System.out.println("---Global Financial Report---");
        double totalSystemProfit = 0;

        if (allBranches.isEmpty()) {
            System.out.println("No active branches in the system yet.");
            return;
        }
        for (Branch b: allBranches) {
            FinancialReport rep= generateBranchReport(b,"May 2026");
            totalSystemProfit += rep.getNetProfit();
            System.out.println(String.format("Branch ID: %s | Manager: %s | Revenue %.2f TL | Profit %.2f TL ", b.getBranchID(), b.getManagerID(),rep.getTotalRevenue(),rep.getNetProfit()));
        }
        System.out.println("-------------------------------");
        System.out.println(String.format("Total System Net Profit: %.2f TL", totalSystemProfit));
    }

}
