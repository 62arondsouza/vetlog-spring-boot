/*
Copyright 2017 José Luis De la Cruz Morales joseluis.delacruz@gmail.com

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

package com.jos.dem.vetlog.validator

import org.springframework.validation.Validator
import org.springframework.validation.Errors
import org.springframework.stereotype.Component

@Component
class PetValidator implements Validator {

  @Override
  boolean supports(Class<?> clazz) {
    PetCommand.class.equals(clazz)
  }

  @Override
  void validate(Object target, Errors errors) {
    PetCommand petCommand = (PetCommand) target
    validateBirthdate(errors, petCommand)
  }

  private void validateBirthdate(Errors errors, PetCommand petCommand){
    if(petCommand.birthDate.isEmpty())
      return
    Date birthDate = Date.parse('MM/dd/yyyy', petCommand.birthDate)
    if(birthDate.compareTo(new Date()) > 0){
      errors.rejectValue('birthDate', 'pet.error.birthDate.past')
    }
  }

}
