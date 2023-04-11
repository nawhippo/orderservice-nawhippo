package edu.iu.c322.orderservice.controller;

import edu.iu.c322.orderservice.Repository.CustomerRepository;
import edu.iu.c322.orderservice.Repository.InMemoryCustomerRepository;
import edu.iu.c322.orderservice.model.Customer;
import edu.iu.c322.orderservice.model.Item;
import edu.iu.c322.orderservice.model.Order;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    //get localhost:8080/customers
    private OrderRepository repository;

    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }


    //ORDER COMMANDS

    @GetMapping("/orders/{id}")
    //find a customer's order list
    public List<Order> getOrders(@PathVariable int id) {
        InMemoryCustomerRepository repository1 = (InMemoryCustomerRepository) repository;
        return repository1.getCustomerbyId(id).getOrders();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/orders/return/{id}/{orderid}/{itemid}")
    public void returnOrderItem(@PathVariable int id, @PathVariable int orderid, @PathVariable int itemid, @RequestBody String reason) {
        //return an item
        InMemoryCustomerRepository myrepo = (InMemoryCustomerRepository) repository;
        //alter an item's reason
        myrepo.getCustomerbyId(id).getOrders().get(orderid).getItems().get(itemid).setReason(reason);
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/orders/{id}/{orderId}")
    public void cancelOrder(@PathVariable int id, @PathVariable int orderId) {
        InMemoryCustomerRepository repository1 = (InMemoryCustomerRepository) repository;
        repository1.getCustomerbyId(id).getOrders().remove(orderId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public int create(@RequestBody Order order){

        for(int i = 0; i < order.getItems().size(); i++){
            OrderItem item = order.getItems().get(i);
            item.setOrder(order);
        }
        Order addedOrder = repository.save(order);
        return addedOrder.getId();
    }

