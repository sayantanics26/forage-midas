package com.jpmc.midascore.component;

import org.springframework.kafka.annotation.KafkaListener;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Component;
@Component


public class KafkaTransactionListener {

    private final DatabaseConduit databaseConduit;

    public KafkaTransactionListener(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

   @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "transaction-listener"
    )
    public void listen(Transaction transaction) {
    System.out.println("received");


       databaseConduit.processTransaction(
               transaction.getSenderId(),
               transaction.getRecipientId(),
               transaction.getAmount()
       );

    }



}

