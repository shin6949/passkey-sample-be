package me.cocoblue.passkeysample.exception.user;

public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException() {
    super("The requested email is already in use.");
  }
}
