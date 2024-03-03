package com.taltech.ecommerce.userservice.util;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.userservice.model.User;
import com.taltech.ecommerce.userservice.model.UserAddress;
import com.taltech.ecommerce.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() < 2) {
            //User 1
            UserAddress userAddress = new UserAddress();
            userAddress.setName("Work address");
            userAddress.setCountry("Estonia");
            userAddress.setCity("Tallinn");
            userAddress.setDistrict("Ulemiste");
            userAddress.setAddress("Random address memories");
            userAddress.setInsertDate(LocalDateTime.now());
            userAddress.setUpdateDate(LocalDateTime.now());

            User user = new User();
            user.setId(12345L);
            user.setName("Daft");
            user.setSurname("Punk");
            user.setEmailAddress("daft.punk@email.com");
            user.setUserAddresses(List.of(userAddress));
            user.setInsertDate(LocalDateTime.now());
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);

            //User 2
            UserAddress userAddress1 = new UserAddress();
            userAddress1.setName("Work address");
            userAddress1.setCountry("Turkey");
            userAddress1.setCity("Istanbul");
            userAddress1.setDistrict("Maslak");
            userAddress1.setAddress("Buyukdere caddesi");
            userAddress1.setInsertDate(LocalDateTime.now());
            userAddress1.setUpdateDate(LocalDateTime.now());

            User user2 = new User();
            user2.setId(99999L);
            user2.setName("Tarkan");
            user2.setSurname("Tevetoglu");
            user2.setEmailAddress("daft.punk@email.com");
            user2.setUserAddresses(List.of(userAddress1));
            user2.setInsertDate(LocalDateTime.now());
            user2.setUpdateDate(LocalDateTime.now());
            userRepository.save(user2);
        }
    }
}
