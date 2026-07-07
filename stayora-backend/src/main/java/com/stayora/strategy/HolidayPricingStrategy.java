package com.stayora.strategy;

import com.stayora.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isTodayHoliday=true; //CALL AN API OR CHECK WITH LOCALDATE
        if(isTodayHoliday){
            price=price.multiply(BigDecimal.valueOf(1.25));
        }

        return price;
    }
}
