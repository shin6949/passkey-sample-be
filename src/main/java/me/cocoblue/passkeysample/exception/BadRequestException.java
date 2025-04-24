package me.cocoblue.passkeysample.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException() {
    super("The request is invalid.");
  }
}
