package com.dimazak.gym;

import com.dimazak.gym.config.AppConfig;
import com.dimazak.gym.facade.GymFacade;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymCrmApp {

    private static final Logger log = LoggerFactory.getLogger(GymCrmApp.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            // Create a new trainee
            Trainee trainee = facade.createTrainee("Alice", "Johnson",
                    LocalDate.of(1998, 3, 25), "456 Elm St", true);
            log.info("Created trainee: {}", trainee);

            // Create another trainee with same name (duplicate test)
            Trainee trainee2 = facade.createTrainee("John", "Smith",
                    LocalDate.of(2000, 1, 1), "789 Oak Ave", true);
            log.info("Created trainee (duplicate name test): {}", trainee2);

            // Create a trainer
            Trainer trainer = facade.createTrainer("Bob", "Williams", 2L, true);
            log.info("Created trainer: {}", trainer);

            // Create a training
            Training training = facade.createTraining(
                    trainee.getId(), trainer.getId(), "Evening Strength",
                    2L, LocalDate.of(2024, 4, 1), 90);
            log.info("Created training: {}", training);

            // Select
            facade.selectTrainee(trainee.getId())
                    .ifPresent(t -> log.info("Selected trainee: {}", t));

            // Update
            Trainee updated = facade.updateTrainee(trainee.getId(),
                    LocalDate.of(1998, 3, 25), "999 New Address");
            log.info("Updated trainee: {}", updated);

            // Delete
            facade.deleteTrainee(trainee.getId());
            log.info("Deleted trainee, exists now: {}",
                    facade.selectTrainee(trainee.getId()).isPresent());
        }

        log.info("Gym CRM Application finished");
    }
}