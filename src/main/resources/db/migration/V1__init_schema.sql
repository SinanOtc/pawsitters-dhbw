CREATE TABLE users (
                       id              BIGSERIAL    PRIMARY KEY,
                       email           VARCHAR(254) NOT NULL UNIQUE,
                       password_hashed VARCHAR(60)  NOT NULL,
                       role            VARCHAR(16)  NOT NULL
);

CREATE TABLE owner_profiles (
                                id         BIGSERIAL    PRIMARY KEY,
                                user_id    BIGINT       NOT NULL UNIQUE,
                                first_name VARCHAR(100) NOT NULL,
                                last_name  VARCHAR(100) NOT NULL,
                                address    VARCHAR(256) NOT NULL,
                                CONSTRAINT fk_owner_profiles_user FOREIGN KEY
                                    (user_id) REFERENCES users(id)
);

CREATE TABLE host_profiles (
                               id              BIGSERIAL     PRIMARY KEY,
                               user_id         BIGINT        NOT NULL UNIQUE,
                               first_name      VARCHAR(100)  NOT NULL,
                               last_name       VARCHAR(100)  NOT NULL,
                               address         VARCHAR(256)  NOT NULL,
                               available_from  DATE          NOT NULL,
                               available_until DATE          NOT NULL,
                               price_per_week  NUMERIC(8, 2) NOT NULL,
                               CONSTRAINT fk_host_profiles_user FOREIGN KEY (user_id)
                                   REFERENCES users(id)
);

CREATE TABLE host_accepted_species (
                                       host_profile_id BIGINT      NOT NULL,
                                       species         VARCHAR(32) NOT NULL,
                                       PRIMARY KEY (host_profile_id, species),
                                       CONSTRAINT fk_host_accepted_species_host FOREIGN KEY
                                           (host_profile_id) REFERENCES host_profiles(id)
);

CREATE TABLE pets (
                      id          BIGSERIAL    PRIMARY KEY,
                      owner_id    BIGINT       NOT NULL,
                      name        VARCHAR(255) NOT NULL,
                      species     VARCHAR(255) NOT NULL,
                      gender      VARCHAR(255) NOT NULL,
                      breed       VARCHAR(100),
                      birth_year  INTEGER,
                      chipped     BOOLEAN      NOT NULL,
                      chip_number VARCHAR(20),
                      vaccinated  BOOLEAN      NOT NULL,
                      neutered    BOOLEAN      NOT NULL,
                      description VARCHAR(500),
                      CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id)
                          REFERENCES owner_profiles(id)
);

CREATE TABLE care_requests (
                               id         BIGSERIAL   PRIMARY KEY,
                               owner_id   BIGINT      NOT NULL,
                               pet_id     BIGINT      NOT NULL,
                               start_date DATE        NOT NULL,
                               end_date   DATE        NOT NULL,
                               status     VARCHAR(16) NOT NULL,
                               CONSTRAINT fk_care_requests_owner FOREIGN KEY
                                   (owner_id) REFERENCES owner_profiles(id),
                               CONSTRAINT fk_care_requests_pet   FOREIGN KEY (pet_id)
                                   REFERENCES pets(id)
);

CREATE TABLE offers (
                        id              BIGSERIAL     PRIMARY KEY,
                        host_id         BIGINT        NOT NULL,
                        care_request_id BIGINT        NOT NULL,
                        weekly_price    NUMERIC(8, 2) NOT NULL,
                        status          VARCHAR(16)   NOT NULL,
                        CONSTRAINT fk_offers_host         FOREIGN KEY
                            (host_id)         REFERENCES host_profiles(id),
                        CONSTRAINT fk_offers_care_request FOREIGN KEY
                            (care_request_id) REFERENCES care_requests(id)
);