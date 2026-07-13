package com.stayora.service;

import com.stayora.dto.ProfileUpdateRequestDto;
import com.stayora.dto.UserDto;
import com.stayora.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyBookings();
}
