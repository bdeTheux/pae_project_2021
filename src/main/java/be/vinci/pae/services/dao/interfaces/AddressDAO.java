package be.vinci.pae.services.dao.interfaces;

import be.vinci.pae.domain.interfaces.AddressDTO;

public interface AddressDAO {

  int addAddress(AddressDTO address);

  AddressDTO getAddress(int id);
}