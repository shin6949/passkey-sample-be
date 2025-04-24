package me.cocoblue.passkeysample.exception.user;

public class PasswordNotEqualException extends RuntimeException {

  public PasswordNotEqualException() {
    super("The password and passwordConfirm are not equal.");
  }
}
