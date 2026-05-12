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
import java.util.List;

public class GymCrmApp {

    private static final Logger log = LoggerFactory.getLogger(GymCrmApp.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            // 1. Create Trainee profile (no auth required)
            Trainee trainee = facade.createTrainee("Alice", "Johnson",
                    LocalDate.of(1998, 3, 25), "456 Elm St");
            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();
            log.info("Created trainee: {} (password: {})", traineeUsername, traineePassword);

            // 2. Create Trainer profile (no auth required)
            Trainer trainer = facade.createTrainer("Bob", "Williams", 2L);
            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();
            log.info("Created trainer: {} (password: {})", trainerUsername, trainerPassword);

            // 3. Trainee credentials matching
            boolean traineeMatch = facade.matchTraineeCredentials(
                    traineeUsername, traineePassword);
            log.info("Trainee credentials match: {}", traineeMatch);

            // 4. Trainer credentials matching
            boolean trainerMatch = facade.matchTrainerCredentials(
                    trainerUsername, trainerPassword);
            log.info("Trainer credentials match: {}", trainerMatch);

            // 5. Select Trainer by username
            Trainer selectedTrainer = facade.getTrainerByUsername(
                    trainerUsername, trainerPassword);
            log.info("Selected trainer: {}", selectedTrainer);

            // 6. Select Trainee by username
            Trainee selectedTrainee = facade.getTraineeByUsername(
                    traineeUsername, traineePassword);
            log.info("Selected trainee: {}", selectedTrainee);

            // 7. Change trainee password
            facade.changeTraineePassword(traineeUsername, traineePassword, "NewPass1234");
            traineePassword = "NewPass1234";
            log.info("Trainee password changed");

            // 8. Change trainer password
            facade.changeTrainerPassword(trainerUsername, trainerPassword, "NewPass5678");
            trainerPassword = "NewPass5678";
            log.info("Trainer password changed");

            // 9. Update trainer profile
            Trainer updatedTrainer = facade.updateTrainer(trainerUsername, trainerPassword,
                    "Robert", "Williams", 1L, true);
            log.info("Updated trainer: {}", updatedTrainer);

            // 10. Update trainee profile
            Trainee updatedTrainee = facade.updateTrainee(traineeUsername, traineePassword,
                    "Alice", "Johnson", LocalDate.of(1998, 3, 25),
                    "999 New Address", true);
            log.info("Updated trainee: {}", updatedTrainee);

            // 16. Add training
            Training training = facade.addTraining(traineeUsername, traineePassword,
                    traineeUsername, trainerUsername,
                    "Morning Cardio", 1L, LocalDate.of(2024, 4, 1), 60);
            log.info("Created training: {}", training);

            // 18. Update trainee's trainers list
            facade.updateTraineeTrainers(traineeUsername, traineePassword,
                    List.of(trainerUsername));
            log.info("Trainee trainers list updated");

            // 17. Get unassigned trainers
            List<Trainer> unassigned = facade.getUnassignedTrainers(
                    traineeUsername, traineePassword);
            log.info("Unassigned trainers: {}", unassigned.size());

            // 14. Get trainee trainings
            List<Training> traineeTrainings = facade.getTraineeTrainings(
                    traineeUsername, traineePassword,
                    null, null, null, null);
            log.info("Trainee trainings count: {}", traineeTrainings.size());

            // 15. Get trainer trainings
            List<Training> trainerTrainings = facade.getTrainerTrainings(
                    trainerUsername, trainerPassword,
                    null, null, null);
            log.info("Trainer trainings count: {}", trainerTrainings.size());

            // 11. Deactivate trainee
            facade.deactivateTrainee(traineeUsername, traineePassword);
            log.info("Trainee deactivated");

            // 12. Deactivate trainer
            facade.deactivateTrainer(trainerUsername, trainerPassword);
            log.info("Trainer deactivated");

            // 13. Delete trainee
            facade.activateTrainee(traineeUsername, traineePassword);
            facade.deleteTrainee(traineeUsername, traineePassword);
            log.info("Trainee deleted");
        }

        log.info("Gym CRM Application finished");
    }
}