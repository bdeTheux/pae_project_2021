package be.vinci.pae.tests;

import be.vinci.pae.domain.user.User;
import be.vinci.pae.domain.user.UserDTO;
import be.vinci.pae.services.dao.UserDAO;

public class MockUserDAO implements UserDAO {

  @Override
  public UserDTO getUserFromEmail(String email) {
    UserDTO user = null;
    if (email.equals("test@test.com")) {
      user = UserDistributor.getGoodValidatedUser();
    }
    return user;
  }

  @Override
  public UserDTO getUserFromUsername(String username) {
    UserDTO user = null;
    if (username.equals("test@test.com")) {
      user = UserDistributor.getGoodValidatedUser();
    }
    return user;
  }

  @Override
  public void addUser(User user) {

  }

}
