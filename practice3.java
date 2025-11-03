package com.example.springhibernatebank;

import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

// --- Entity: Account ---
@Entity
@Table(name = "accounts")
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private double balance;

    public Account() {}

    public Account(String holderName, double balance) {
        this.holderName = holderName;
        this.balance = balance;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    @Override
    public String toString() {
        return "Account [id=" + id + ", holderName=" + holderName + ", balance=" + balance + "]";
    }
}

// --- Entity: TransactionRecord ---
@Entity
@Table(name = "transactions")
class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column private int fromAccountId;
    @Column private int toAccountId;
    @Column private double amount;

    public TransactionRecord() {}

    public TransactionRecord(int fromAccountId, int toAccountId, double amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id + ", from=" + fromAccountId + ", to=" + toAccountId + ", amount=" + amount + "]";
    }
}

// --- Hibernate Configuration Utility ---
class HibernateUtil {
    private static final SessionFactory sessionFactory = new Configuration()
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(Account.class)
            .addAnnotatedClass(TransactionRecord.class)
            .buildSessionFactory();

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

// --- DAO Layer ---
@Repository
class AccountDAO {

    public Account getAccount(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Account.class, id);
        }
    }

    public void updateAccount(Account account) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void saveTransaction(TransactionRecord record) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(record);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}

// --- Service Layer ---
@Service
class BankingService {

    @Autowired
    private AccountDAO accountDAO;

    // @Transactional ensures atomicity — if any step fails, everything rolls back
    @Transactional
    public void transferMoney(int fromId, int toId, double amount) {
        Account fromAcc = accountDAO.getAccount(fromId);
        Account toAcc = accountDAO.getAccount(toId);

        if (fromAcc == null || toAcc == null)
            throw new RuntimeException("Invalid account ID");

        if (fromAcc.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        fromAcc.setBalance(fromAcc.getBalance() - amount);
        toAcc.setBalance(toAcc.getBalance() + amount);

        // Update accounts
        accountDAO.updateAccount(fromAcc);
        accountDAO.updateAccount(toAcc);

        // Record transaction
        TransactionRecord record = new TransactionRecord(fromId, toId, amount);
        accountDAO.saveTransaction(record);

        System.out.println("✅ Transaction Successful: " + record);
    }
}

// --- Configuration Class ---
@Configuration
@ComponentScan("com.example.springhibernatebank")
@EnableTransactionManagement
class AppConfig {

    @Bean
    public AccountDAO accountDAO() {
        return new AccountDAO();
    }

    @Bean
    public BankingService bankingService() {
        return new BankingService();
    }
}

// --- Main Class ---
public class MainApp {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        BankingService service = context.getBean(BankingService.class);
        AccountDAO dao = context.getBean(AccountDAO.class);

        // --- Setup Sample Accounts ---
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(new Account("Alice", 5000));
            session.persist(new Account("Bob", 2000));
            tx.commit();
        }

        // --- Perform Transaction ---
        try {
            service.transferMoney(1, 2, 1500);
        } catch (Exception e) {
            System.out.println("❌ Transaction failed: " + e.getMessage());
        }

        // Display final balances
        System.out.println("\nFinal Account States:");
        System.out.println(dao.getAccount(1));
        System.out.println(dao.getAccount(2));

        context.close();
        HibernateUtil.getSessionFactory().close();
    }
}


<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
"-//Hibernate/Hibernate Configuration DTD 3.0//EN"
"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">

<hibernate-configuration>
    <session-factory>

        <!-- Database Connection -->
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/bankdb</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">your_password</property>

        <!-- Hibernate Settings -->
        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="show_sql">true</property>
        <property name="hbm2ddl.auto">update</property>

    </session-factory>
</hibernate-configuration>
