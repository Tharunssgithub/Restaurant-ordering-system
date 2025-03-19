package com.edubridge_mini_project.restaurant_ordering_system;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.edubridge_mini_project.restaurant_ordering_system.entities.Bill;
import com.edubridge_mini_project.restaurant_ordering_system.entities.FoodItem;
import com.edubridge_mini_project.restaurant_ordering_system.entities.OrderItem;

import java.util.*;

public class Restaurant {
    private Scanner scanner;
    private Session session;
    private List<OrderItem> orderItems;
    private double totalBillAmount;

    public Restaurant(Session session) {
        this.scanner = new Scanner(System.in);
        this.session = session;
        this.orderItems = new ArrayList<>();
        this.totalBillAmount = 0.0;
    }

    public void run() {
        while (true) {
            displayMenu();
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    System.out.print("Enter quantity: ");
                    int quantity = scanner.nextInt();
                    placeOrder(choice, quantity);
                    break;  
                case 6:
                    viewBill();
                    break;  
                case 7:
                    System.out.println("Thank you for visiting!");
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n=== Restaurant Menu ===");
        Query<FoodItem> query = session.createQuery("FROM FoodItem", FoodItem.class);
        List<FoodItem> foodItems = query.list();
        for (int i = 0; i < foodItems.size(); i++) {
            System.out.println((i + 1) + ". " + foodItems.get(i).getName() + " = ₹" + foodItems.get(i).getPrice());
        }
        System.out.println("6. View Bill");
        System.out.println("7. Exit");
    }

    public void placeOrder(int choice, int quantity) {
        Transaction transaction = session.beginTransaction();
        FoodItem selectedFood = session.get(FoodItem.class, choice);
        if (selectedFood == null) {
            System.out.println("Invalid selection.");
            return;
        }
        Query<OrderItem> query = session.createQuery(
            "FROM OrderItem WHERE foodItem.id = :foodId", OrderItem.class);
        query.setParameter("foodId", selectedFood.getId());
        OrderItem existingOrder = query.uniqueResult();
        if (existingOrder != null) {
            existingOrder.setQuantity(existingOrder.getQuantity() + quantity);
            existingOrder.setTotalPrice(existingOrder.getTotalPrice() + (selectedFood.getPrice() * quantity));
            session.merge(existingOrder);
        } else {
            OrderItem newOrder = new OrderItem();
            newOrder.setFoodItem(selectedFood);
            newOrder.setQuantity(quantity);
            newOrder.setTotalPrice(selectedFood.getPrice() * quantity);
            session.persist(newOrder);
        }
        transaction.commit();
        System.out.println(quantity + " " + selectedFood.getName() + " added to your order. Total Price: ₹" + (selectedFood.getPrice() * quantity));
    }

    public void viewBill() {
        Transaction transaction = session.beginTransaction();

        List<OrderItem> orderItems = session.createQuery("FROM OrderItem", OrderItem.class).list();
        
        if (orderItems.isEmpty()) {
            System.out.println("No items ordered.");
        } else {
            double totalAmount = 0;
            System.out.println("\n=== Your Bill ===");
            for (OrderItem order : orderItems) {
                System.out.println(order.getQuantity() + " " + order.getFoodItem().getName() + " = ₹" + order.getTotalPrice());
                totalAmount += order.getTotalPrice();
            }
            System.out.println("Total: ₹" + totalAmount);
            Bill bill = new Bill();
            bill.setBillDate(new Date());  
            bill.setTotalAmount(totalAmount);
            session.persist(bill);
            session.createQuery("DELETE FROM OrderItem").executeUpdate();
            System.out.println("Bill saved. Thank you for visiting!");
        }
        transaction.commit();
    }

    private void saveOrderToDatabase() {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Bill bill = new Bill();
            bill.setBillDate(new Date());
            bill.setTotalAmount(totalBillAmount);
            session.persist(bill);
            for (OrderItem item : orderItems) {
                item.setBill(bill);
                session.merge(item);
            }
            transaction.commit();
            System.out.println("Order successfully saved!");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
