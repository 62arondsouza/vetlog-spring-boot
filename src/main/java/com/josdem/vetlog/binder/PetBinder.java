/*
  Copyright 2025 Jose Morales contact@josdem.io

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.josdem.vetlog.binder;

import com.josdem.vetlog.command.Command;
import com.josdem.vetlog.command.PetCommand;
import com.josdem.vetlog.enums.VaccinationStatus;
import com.josdem.vetlog.exception.BusinessException;
import com.josdem.vetlog.model.Breed;
import com.josdem.vetlog.model.Pet;
import com.josdem.vetlog.model.Vaccination;
import com.josdem.vetlog.repository.BreedRepository;
import com.josdem.vetlog.repository.VaccinationRepository;
import com.josdem.vetlog.util.UuidGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PetBinder {

    private final BreedRepository breedRepository;
    private final VaccinationRepository vaccinationRepository;

    private static final String RABIES_VACCINE = "Rabies";

    public Pet bindPet(Command command) {
        PetCommand petCommand = (PetCommand) command;
        Pet pet = new Pet();
        pet.setId(petCommand.getId());
        pet.setUuid(UuidGenerator.generateUuid());
        if (petCommand.getUuid() != null) {
            pet.setUuid(petCommand.getUuid());
        }
        pet.setName(petCommand.getName());
        if (petCommand.getBirthDate().isEmpty()) {
            pet.setBirthDate(LocalDate.now());
        } else {
            pet.setBirthDate(LocalDate.parse(petCommand.getBirthDate()));
        }
        pet.setSterilized(petCommand.getSterilized());
        pet.setImages(petCommand.getImages());
        pet.setStatus(petCommand.getStatus());

        // Load previous vaccines from the database using the original Pet object (if updating)
        List<Vaccination> previousVaccines = List.of();
        if (petCommand.getId() != null) {
            Pet existingPet = new Pet();
            existingPet.setId(petCommand.getId());
            previousVaccines = vaccinationRepository.findAllByPet(existingPet);
        }

        // Check if Rabies vaccine was changed from PENDING to APPLIED
        for (Vaccination newVaccine : petCommand.getVaccines()) {
            if (RABIES_VACCINE.equalsIgnoreCase(newVaccine.getName())
                    && newVaccine.getStatus() == VaccinationStatus.APPLIED) {
                previousVaccines.stream()
                        .filter(v -> RABIES_VACCINE.equalsIgnoreCase(v.getName()))
                        .findFirst()
                        .ifPresent(oldVaccine -> {
                            if (oldVaccine.getStatus() == VaccinationStatus.PENDING) {
                                // Create a new Rabies vaccine for one year later
                                Vaccination futureRabies = new Vaccination(
                                        null, RABIES_VACCINE, LocalDate.now().plusYears(1), VaccinationStatus.NEW, pet);
                                vaccinationRepository.save(futureRabies);
                            }
                        });
            }
        }

        /// Save updated vaccines
        pet.setVaccines(petCommand.getVaccines());
        petCommand.getVaccines().forEach(vaccine -> {
            vaccine.setDate(LocalDate.now());
            vaccinationRepository.save(vaccine);
        });

        Optional<Breed> breed = breedRepository.findById(petCommand.getBreed());
        if (breed.isEmpty()) {
            throw new BusinessException("Breed was not found for pet: " + pet.getName());
        }
        pet.setBreed(breed.get());
        return pet;
    }

    public PetCommand bindPet(Pet pet) {
        PetCommand command = new PetCommand();
        command.setId(pet.getId());
        command.setUuid(pet.getUuid());
        command.setName(pet.getName());
        command.setBirthDate(pet.getBirthDate().toString());
        command.setSterilized(pet.getSterilized());
        command.setStatus(pet.getStatus());
        command.setImages(pet.getImages());
        command.setBreed(pet.getBreed().getId());
        command.setUser(pet.getUser().getId());
        command.setType(pet.getBreed().getType());
        var vaccines = vaccinationRepository.findAllByPet(pet).stream()
                .filter(vaccine -> vaccine.getStatus().equals(VaccinationStatus.PENDING))
                .toList();
        command.setVaccines(vaccines);
        return command;
    }
}
