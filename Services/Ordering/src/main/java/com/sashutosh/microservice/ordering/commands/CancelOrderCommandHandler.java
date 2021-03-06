package com.sashutosh.microservice.ordering.commands;

import com.sashutosh.microservice.ordering.exception.StatusChangeException;
import com.sashutosh.microservice.ordering.model.Order;
import com.sashutosh.microservice.ordering.model.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CancelOrderCommandHandler implements IRequestHandler<CancelOrderCommand, Boolean>{

    @Autowired
    OrderRepository orderRepository;

    @Override
    public Boolean handle(CancelOrderCommand cmd)
    {
        Optional<Order> order= orderRepository.findById(cmd.getOrderNumber());
        if(order.isPresent()) {
            Order currentOrder= order.get();
            try {
                currentOrder.setCancelledStatus();
            } catch (StatusChangeException e) {
                e.printStackTrace();
            }
            orderRepository.save(currentOrder);
            return true;
        }
        return false;


    }
}
