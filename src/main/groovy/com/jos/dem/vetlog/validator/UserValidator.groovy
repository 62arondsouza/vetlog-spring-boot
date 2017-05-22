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
import org.springframework.beans.factory.annotation.Autowired

import com.jos.dem.vetlog.service.LocaleService
import com.jos.dem.vetlog.service.UserService
import com.jos.dem.vetlog.command.UserCommand

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class UserValidator implements Validator {

  @Autowired
  LocaleService localeService
  @Autowired
  UserService userService

  Logger log = LoggerFactory.getLogger(this.class)

  @Override
  boolean supports(Class<?> clazz) {
    UserCommand.class.equals(clazz)
  }

  @Override
  void validate(Object target, Errors errors) {
    UserCommand userCommand = (UserCommand) target
    validatePasswords(errors, userCommand)
    validateUsername(errors, userCommand)
    validateEmail(errors, userCommand)
  }

  def validatePasswords(Errors errors, UserCommand command) {
    if (!command.password.equals(command.passwordConfirmation)){
      errors.rejectValue('password', 'error.password', localeService.getMessage('user.validation.password.equals'))
    }
  }

  def validateUsername(Errors errors, UserCommand command) {
    if (userService.getByUsername(command.username)){
       errors.rejectValue("username", 'error.username', localeService.getMessage('user.validation.duplicated.username'))
    }
  }

  def validateEmail(Errors errors, UserCommand command) {
    if (userService.getByEmail(command.email)){
       errors.rejectValue("email", 'error.email', localeService.getMessage('user.validation.duplicated.email'))
    }
  }

}
