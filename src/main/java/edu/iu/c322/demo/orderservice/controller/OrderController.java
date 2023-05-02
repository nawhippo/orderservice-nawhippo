package edu.iu.c322.demo.orderservice.controller;

import edu.iu.c322.demo.orderservice.model.Order;
import edu.iu.c322.demo.orderservice.model.OrderItem;
import edu.iu.c322.demo.orderservice.model.TrackingUpdateRequest;
import edu.iu.c322.demo.orderservice.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final WebClient invoicingService;
    private final WebClient trackingService;

    private OrderRepository repository;

    public OrderController(OrderRepository repository, WebClient.Builder webClientBuilder) {
        this.repository = repository;
        this.invoicingService = webClientBuilder.baseUrl("http://localhost:8084").build();
        this.trackingService = webClientBuilder.baseUrl("http://localhost:8087").build();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public int create(@RequestBody Order order){

        //check if address fields are valid
        if (order.getShippingAddress().getCity() == null || order.getShippingAddress().getCity().isEmpty() || order.getShippingAddress().getState() == null || order.getShippingAddress().getState().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address fields");
        }

        order.setStatus("created");
        Order addedOrder = order;
        addedOrder.setItems(order.getItems());
        int orderId = addedOrder.getId();
        System.out.println("New order created with ID: " + orderId);

       //create the invoice through a post request in the invoice service
        invoicingService.post().uri("/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(addedOrder)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> Mono.error(new RuntimeException("Failed to create invoice")))
                .bodyToMono(Integer.class);



        for(int i = 0; i < order.getItems().size(); i++){
            OrderItem item = order.getItems().get(i);
            System.out.println("Item created with ID: " + item.getId());
            item.setOrder(order);

            // Create a tracking update request from the order item
            TrackingUpdateRequest trackingRequest = new TrackingUpdateRequest();
            trackingRequest.setItemIds(Collections.singletonList(item.getId()));
            trackingRequest.setStatus("ordered");

            // Create a tracking for each item in the order.
            trackingService.post().uri("/trackings/{orderId}", order.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(trackingRequest)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> Mono.error(new RuntimeException("Failed to create tracking")))
                    .bodyToMono(Integer.class)
                    .subscribe();
        }

        System.out.println("Received Order: " + order.toString());
        repository.save(addedOrder);
        return addedOrder.getId();
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> findByCustomer(@PathVariable int customerId){
        return repository.findByCustomerId(customerId);
    }

    @GetMapping("/order/{orderId}")
    public Optional<Order> findByOrderId(@PathVariable int orderId){
        return repository.findById(orderId);
    }

    @PutMapping("/return")
    public Order returnOrderItem(@RequestBody Map<String, Object> requestBody) {
        int orderId = (Integer) requestBody.get("orderId");
        int itemId = (Integer) requestBody.get("itemId");
        String reason = (String) requestBody.get("reason");

        return repository.findById(orderId)
                .map(order -> {
                    if (repository.findByCustomerId(order.getCustomerId()).isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer with this ID does not exist in the system");
                    }
                    OrderItem returnItem = order.getItems().stream()
                            .filter(item -> item.getId() == itemId)
                            .findFirst()
                            .orElse(null);

                    if (returnItem == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid item ID");
                    }

                    returnItem.setReturnReason(reason);

                    return repository.save(order);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid order ID"));
    }


    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable int orderId) {
     Optional<Order> orderOpt = repository.findById(orderId);
        if (orderOpt.isPresent()) {
            //set to cancelled as opposed to removing to handle data inconsistencies
            Order order = orderOpt.get();
            order.setStatus("cancelled");
            repository.save(order);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid order ID");
        }
    }



}
