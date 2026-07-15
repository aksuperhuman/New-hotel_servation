package com.hotel.hotel_system.service;

import com.hotel.hotel_system.exception.ResourceNotFoundException;
import com.hotel.hotel_system.model.Hotel;
import com.hotel.hotel_system.repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelService.class);

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel addHotel(Hotel hotel) {
        Hotel saved = hotelRepository.save(hotel);
        log.info("Hotel created id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<Hotel> searchByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city);
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }

    public Hotel updateHotel(Long id, Hotel newHotel) {
        Hotel existing = getHotelById(id);
        existing.setName(newHotel.getName());
        existing.setAddress(newHotel.getAddress());
        existing.setCity(newHotel.getCity());
        existing.setCountry(newHotel.getCountry());
        existing.setDescription(newHotel.getDescription());
        existing.setStarRating(newHotel.getStarRating());
        existing.setPhoneNumber(newHotel.getPhoneNumber());
        existing.setEmail(newHotel.getEmail());
        return hotelRepository.save(existing);
    }

    public void deleteHotel(Long id) {
        Hotel existing = getHotelById(id);
        hotelRepository.delete(existing);
        log.info("Hotel deleted id={}", id);
    }
}
