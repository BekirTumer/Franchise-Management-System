package service;
import domain.IPaymentGateway;


public class ExternalPaymentService implements IPaymentGateway{

    private static  ExternalPaymentService instance;
    private ExternalPaymentService(){}
    public static ExternalPaymentService getInstance(){
        if(instance==null){
            instance = new ExternalPaymentService();
        }
        return instance;
    }

    @Override
    public boolean executeTransaction(double amount, String accountDetails) {
        System.out.println("Connected to the external payment system. Transaction successful: " + amount);
        return true;
    }
}
