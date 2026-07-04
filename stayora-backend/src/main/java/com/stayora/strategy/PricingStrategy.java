package com.stayora.strategy;

import com.stayora.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


public interface PricingStrategy {


    BigDecimal calculatePrice(Inventory inventory);

}
