package com.stayora.controller;

import com.stayora.dto.RoomDto;
import com.stayora.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto){
        RoomDto roomDto1 = roomService.createNewRoom(hotelId, roomDto);
        return new  ResponseEntity<>(roomDto1, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms(@PathVariable Long hotelId){
        List<RoomDto> roomDto = roomService.getAllRooms(hotelId);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long roomId){
        RoomDto roomDto = roomService.getRoomById(roomId);
        return  new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long roomId){
        roomService.deleteById(roomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable Long hotelId,@PathVariable Long roomId, @RequestBody RoomDto roomDto){
        return ResponseEntity.ok(roomService.updateRoomById(hotelId,roomId,roomDto));
    }
}
