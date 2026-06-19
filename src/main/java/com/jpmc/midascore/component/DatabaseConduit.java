package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;

    public DatabaseConduit(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }


    public void processTransaction(
            long senderId,
            long recipientId,
            float amount
    ) {

        UserRecord sender = userRepository.findById(senderId);
        UserRecord recipient = userRepository.findById(recipientId);

        if(sender == null || recipient == null) {
            return;
        }
        if(sender.getBalance()<amount) {
            return;
        }
        Transaction transaction =
                new Transaction(senderId, recipientId, amount);

        Incentive incentive = null;

        try {
            ResponseEntity<Incentive> response =
                    restTemplate.postForEntity(
                            "http://localhost:8080/incentive",
                            transaction,
                            Incentive.class
                    );

            incentive = response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
        }

        float incentiveAmount = 0;

        if (incentive != null) {
            incentiveAmount = incentive.getAmount();
        }

        sender.setBalance(sender.getBalance() - amount);

        recipient.setBalance(
                recipient.getBalance()
                        + amount
                        + incentiveAmount
        );

        userRepository.save(sender);
        userRepository.save(recipient);
    }
    public UserRecord getUser(long id) {
        return userRepository.findById(id);
    }

    private final RestTemplate restTemplate = new RestTemplate();
}
