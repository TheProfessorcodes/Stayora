package com.stayora.service;

import com.stayora.entity.Booking;

public interface CheckOutService {
    String getCheckOutSession(Booking booking, String successUrl, String failureUrl);
}
