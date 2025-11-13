package ru.cs.vsu.social_network.user_profile_service.exceptions.profile;

public class ProfileCityTooLongException extends RuntimeException {
  public ProfileCityTooLongException(String message) {
    super(message);
  }
}
