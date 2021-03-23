package be.vinci.pae.domain.furniture;

import java.util.List;
import java.util.Map;

import be.vinci.pae.domain.address.Address;

public interface FurnitureUCC {

  // settingPurchasePrice(double price);
  // settingSellingPrice(double price);
  // indicateTheCollectionOfTheFurniture();
  // indicateThatTheFurnitureIsDelivered();
  // indicate
  // indiquer qu'un meuble est deposé + livré + emporté
  // + fixer un prix d'achat + indiquer un prix de vente

  void introduceOption(int optionTerm, int idUser, int idFurniture);

  void cancelOption(String cancellationReason, int idOption);

  void indicateSentToWorkshop(int id);

  void indicateDropOfStore(int id);

  void indicateOfferedForSale(int id);

  void withdrawSale(int id);

  // pas encore pour le livrable
  void introduceRequestForVisite(String timeSlot, Address address,
      Map<Integer, List<String>> furnitures);

  List<FurnitureDTO> SeeFurnitureList();
}
