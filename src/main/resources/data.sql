INSERT INTO training_types (id, training_type_name) VALUES
                                                        (1, 'Cardio'),
                                                        (2, 'Strength'),
                                                        (3, 'Yoga'),
                                                        (4, 'HIIT'),
                                                        (5, 'Pilates')
    ON DUPLICATE KEY UPDATE training_type_name = VALUES(training_type_name);