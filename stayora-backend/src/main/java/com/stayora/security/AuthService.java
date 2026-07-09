package com.stayora.security;

import com.stayora.dto.LoginDto;
import com.stayora.dto.SignUpRequestDto;
import com.stayora.dto.UserDto;
import com.stayora.entity.User;
import com.stayora.entity.enums.Role;
import com.stayora.exception.ResourceNotFoundException;
import com.stayora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService{

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDto signUp(SignUpRequestDto  signUpRequestDto){

        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if(user != null)
        {
            throw new RuntimeException("User already exists");
        }
        User newUser=modelMapper.map(signUpRequestDto,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser=userRepository.save(newUser);
        return modelMapper.map(newUser,UserDto.class);
    }

    public String[] login(LoginDto loginDto){

       Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()
        ));

       User user=(User) authentication.getPrincipal();
       String arr[]=new String[2];
       arr[0]= jwtService.generateAccessToken(user);
       arr[1]= jwtService.generateRefreshToken(user);

        return arr;
    }


    public String refreshToken(String refreshToken){
        Long id=jwtService.getUserIdFromToken(refreshToken);

        User user=userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        return jwtService.generateAccessToken(user);
    }

}
