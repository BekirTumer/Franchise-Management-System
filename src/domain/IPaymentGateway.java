package domain;

public interface IPaymentGateway {

    boolean executeTransaction(double amount, String accountDetails);
}
