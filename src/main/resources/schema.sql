DROP TABLE IF EXISTS drivers, destinations CASCADE;

CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    driver_telegram_id VARCHAR UNIQUE NOT NULL,
    driver_fio VARCHAR(255) NOT NULL,
    driver_telephone VARCHAR(11) UNIQUE NOT NULL,
    driver_type_car_body VARCHAR(100) NOT NULL,
    driver_car_body_dimensions VARCHAR NOT NULL,
    driver_load_opacity INT NOT NULL,
    driver_created_at TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS destinations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    destination_from VARCHAR NOT NULL,
    destination_to VARCHAR NOT NULL,
    destination_date_arrival VARCHAR NOT NULL,
    destination_created_at TIMESTAMP NOT NULL,
    driver_id BIGINT NOT NULL,
    CONSTRAINT fk_destinations_to_driver
    FOREIGN KEY(driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);
