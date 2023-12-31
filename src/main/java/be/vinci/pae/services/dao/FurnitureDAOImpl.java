package be.vinci.pae.services.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import be.vinci.pae.domain.interfaces.FurnitureDTO;
import be.vinci.pae.domain.interfaces.FurnitureDTO.Condition;
import be.vinci.pae.domain.interfaces.OptionDTO;
import be.vinci.pae.domain.interfaces.OptionDTO.State;
import be.vinci.pae.domain.interfaces.PhotoDTO;
import be.vinci.pae.domain.interfaces.TypeOfFurnitureDTO;
import be.vinci.pae.exceptions.FatalException;
import be.vinci.pae.factories.interfaces.FurnitureFactory;
import be.vinci.pae.factories.interfaces.OptionFactory;
import be.vinci.pae.factories.interfaces.PhotoFactory;
import be.vinci.pae.factories.interfaces.TypeOfFurnitureFactory;
import be.vinci.pae.services.dal.DalBackendServices;
import be.vinci.pae.services.dao.interfaces.FurnitureDAO;
import be.vinci.pae.services.dao.interfaces.UserDAO;
import jakarta.inject.Inject;

public class FurnitureDAOImpl implements FurnitureDAO {

  @Inject
  private DalBackendServices dalBackendService;
  @Inject
  private FurnitureFactory furnitureFactory;
  @Inject
  private OptionFactory optionFactory;
  @Inject
  private TypeOfFurnitureFactory typeOfFurnitureFactory;
  @Inject
  private PhotoFactory photoFactory;

  @Inject
  private UserDAO userDao;

  PreparedStatement ps;

  @Override
  public FurnitureDTO getFurnitureById(int id) {
    FurnitureDTO furniture = null;
    try {
      String sql = "SELECT id_furniture, condition, description, purchase_price, pick_up_date, "
          + "store_deposit, deposit_date,"
          + " offered_selling_price, id_type, request_visit, seller, favorite_photo"
          + " FROM pae.furnitures WHERE id_furniture = ?;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        furniture = setFurniture(rs, furniture);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return furniture;
  }

  @Override
  public int getSumOfOptionDaysForAUserAboutAFurniture(int idFurniture, int idUser) {
    int number = 0;
    try {
      String sql =
          "SELECT SUM(option_term) FROM pae.options WHERE id_furniture = ? AND id_user = ?";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, idFurniture);
      ps.setInt(2, idUser);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        number = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return number;
  }

  private FurnitureDTO setFurniture(ResultSet rs, FurnitureDTO furniture) {
    try {
      furniture = furnitureFactory.getFurnitureDTO();
      furniture.setId(rs.getInt(1));
      furniture.setCondition(rs.getString(2));
      furniture.setDescription(rs.getString(3));
      furniture.setPurchasePrice(rs.getDouble(4));
      furniture
          .setPickUpDate(rs.getTimestamp(5) == null ? null : rs.getTimestamp(5).toLocalDateTime());
      furniture.setStoreDeposit(rs.getBoolean(6));
      furniture
          .setDepositDate(rs.getTimestamp(7) == null ? null : rs.getTimestamp(7).toLocalDateTime());
      furniture.setOfferedSellingPrice(rs.getDouble(8));
      furniture.setTypeId(rs.getInt(9));
      furniture.setType(getFurnitureTypeById(rs.getInt(9)));
      furniture.setRequestForVisitId(rs.getInt(10));
      furniture.setSellerId(rs.getInt(11));
      furniture.setSeller(userDao.getUserFromId(rs.getInt(11)));
      furniture.setFavouritePhotoId(rs.getInt(12));
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return furniture;
  }

  private OptionDTO setOption(ResultSet rs, OptionDTO option) {
    try {
      option = optionFactory.getOptionDTO();
      option.setId(rs.getInt(1));
      option.setDate(rs.getTimestamp(2) == null ? null : rs.getTimestamp(2).toLocalDateTime());
      option.setOptionTerm(rs.getInt(3));
      option.setCancellationReason(rs.getString(4));
      option.setCondition(rs.getString(5));
      option.setIdUser(rs.getInt(6));
      option.setIdFurniture(rs.getInt(7));
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return option;
  }

  @Override
  public void setFurnitureCondition(FurnitureDTO furniture, Condition condition) {
    try {
      String sql = "UPDATE pae.furnitures SET condition = ? WHERE id_furniture = ? ;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, condition.toString());
      ps.setInt(2, furniture.getId());
      ps.execute();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

  @Override
  public void introduceOption(int optionTerm, int idUser, int idFurniture) {
    try {
      String sql = "INSERT INTO pae.options VALUES(DEFAULT, ?, ?, null, ?, ?, ?);";
      ps = dalBackendService.getPreparedStatement(sql);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Timestamp date = Timestamp.valueOf(LocalDateTime.now().format(formatter));
      ps.setTimestamp(1, date);
      ps.setInt(2, optionTerm);
      ps.setString(3, "en cours");
      ps.setInt(4, idUser);
      ps.setInt(5, idFurniture);
      ps.execute();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

  @Override
  public void indicateFurnitureUnderOption(int id) {
    FurnitureDTO furniture = getFurnitureById(id);
    setFurnitureCondition(furniture, Condition.SOUS_OPTION);
  }

  @Override
  public int cancelOption(String cancellationReason, int idOption) {
    try {
      String sql = "UPDATE pae.options SET condition = ?, cancellation_reason = ? "
          + "WHERE id_option = ? RETURNING id_furniture;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, "annulée");
      ps.setString(2, cancellationReason == null ? "" : cancellationReason);
      ps.setInt(3, idOption);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return -1;
  }

  @Override
  public OptionDTO getOption(int idFurniture) {
    OptionDTO option = null;
    try {
      String sql = "SELECT id_option, date, option_term, cancellation_reason, "
          + "condition, id_user, id_furniture"
          + " FROM pae.options WHERE id_furniture = ? AND condition = ?;";

      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, idFurniture);
      ps.setString(2, State.EN_COURS.toString());
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        option = setOption(rs, option);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return option;
  }

  @Override
  public void indicateSentToWorkshop(int id) {
    FurnitureDTO furniture = getFurnitureById(id);
    setFurnitureCondition(furniture, Condition.EN_RESTAURATION);

  }

  @Override
  public void indicateDropInStore(int id) {
    FurnitureDTO furniture = getFurnitureById(id);
    try {
      String sql = "UPDATE pae.furnitures SET condition = ?, store_deposit = ?, "
          + "deposit_date = ? WHERE id_furniture = ? ;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.DEPOSE_EN_MAGASIN.toString());
      ps.setBoolean(2, true);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Timestamp date = Timestamp.valueOf(LocalDateTime.now().format(formatter));
      ps.setTimestamp(3, date);
      ps.setInt(4, furniture.getId());
      ps.execute();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    setFurnitureCondition(furniture, Condition.DEPOSE_EN_MAGASIN);
  }

  @Override
  public void indicateOfferedForSale(FurnitureDTO furniture, double price) {
    try {
      String sql = "UPDATE pae.furnitures SET condition = ?, offered_selling_price = ? "
          + "WHERE id_furniture = ? ;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.EN_VENTE.toString());
      ps.setDouble(2, price);
      ps.setInt(3, furniture.getId());
      ps.execute();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

  @Override
  public void withdrawSale(int id) {
    FurnitureDTO furniture = getFurnitureById(id);
    setFurnitureCondition(furniture, Condition.RETIRE);

  }

  @Override
  public List<FurnitureDTO> getFurnitureList() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo WHERE f.condition != ? AND f.condition != ?;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.EN_ATTENTE.toString());
      ps.setString(2, Condition.REFUSE.toString());
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }


  @Override
  public List<FurnitureDTO> getFurnitureListForResearch() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo";
      ps = dalBackendService.getPreparedStatement(sql);
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public List<FurnitureDTO> getPublicFurnitureList() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo WHERE f.condition = ? OR f.condition = ? ;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.EN_VENTE.toString());
      ps.setString(2, Condition.SOUS_OPTION.toString());
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public List<FurnitureDTO> getFurnitureListByType(int idType) {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo WHERE f.id_type = ?;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, idType);
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public List<FurnitureDTO> getPublicFurnitureListByType(int idType) {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo WHERE (f.condition = ? "
          + "OR f.condition = ?) AND f.id_type = ? ;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.EN_VENTE.toString());
      ps.setString(2, Condition.SOUS_OPTION.toString());
      ps.setInt(3, idType);
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public String getFurnitureTypeById(int id) {
    String label = "";
    try {
      String sql = "SELECT label FROM pae.types_of_furnitures WHERE id_type = ?;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        label = rs.getString(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return label;
  }

  @Override
  public String getFavouritePhotoById(int id) {
    String favouritePhoto = "";
    try {
      String sql = "SELECT photo FROM pae.photos WHERE id_photo = ?";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        favouritePhoto = rs.getString(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return favouritePhoto;
  }

  @Override
  public void cancelOvertimedOptions() {
    try {
      String sql =
          "UPDATE pae.options" + " SET condition = ?, cancellation_reason = 'Temps dépassé'"
              + " WHERE (date + interval '1' day * option_term) < NOW();";

      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, State.ANNULEE.toString());
      int val = ps.executeUpdate();
      if (val > 0) {
        sql = "UPDATE pae.furnitures" + " SET condition = ?"
            + " WHERE condition = ? AND id_furniture NOT IN "
            + " (SELECT o.id_furniture FROM pae.options o WHERE o.condition = ?);";

        ps = dalBackendService.getPreparedStatement(sql);
        ps.setString(1, Condition.EN_VENTE.toString());
        ps.setString(2, Condition.SOUS_OPTION.toString());
        ps.setString(3, State.EN_COURS.toString());
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

  @Override
  public List<TypeOfFurnitureDTO> getTypesOfFurnitureList() {
    List<TypeOfFurnitureDTO> list = new ArrayList<TypeOfFurnitureDTO>();
    try {
      String sql = "SELECT id_type, label FROM pae.types_of_furnitures;";
      ps = dalBackendService.getPreparedStatement(sql);
      ResultSet rs = ps.executeQuery();
      TypeOfFurnitureDTO type = null;
      while (rs.next()) {
        TypeOfFurnitureDTO typeDTO = setTypeOfFurniture(rs, type);
        list.add(typeDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  /**
   * Method to set a type of furniture from a resultset.
   * 
   * @param rs the resultset
   * @param type a null typeOfFurniture
   * @return a typeOfFurnitureDTO
   */
  public TypeOfFurnitureDTO setTypeOfFurniture(ResultSet rs, TypeOfFurnitureDTO type) {
    try {
      type = typeOfFurnitureFactory.getTypeOfFurnitureDTO();
      type.setId(rs.getInt(1));
      type.setLabel(rs.getString(2));
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return type;
  }

  @Override
  public int addFurniture(FurnitureDTO furniture, int idRequestForVisit, int idSeller) {
    int key = 0;
    try {
      String sql = "INSERT INTO pae.furnitures VALUES(default, ?, ?, "
          + "null, null, null, null, null, ?, ?, ?, null);";
      ps = dalBackendService.getPreparedStatementWithGeneratedReturn(sql);
      ps.setString(1, Condition.EN_ATTENTE.toString());
      ps.setString(2, furniture.getDescription());
      ps.setInt(3, furniture.getTypeId());
      ps.setInt(4, idRequestForVisit);
      ps.setInt(5, idSeller);
      ps.execute();
      ResultSet rs = ps.getGeneratedKeys();
      if (rs.next()) {
        key = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return key;
  }

  @Override
  public void addClientPhoto(PhotoDTO photo, int idFurniture) {
    try {
      String sql = "INSERT INTO pae.photos VALUES (default, ?, false, true, ?);";
      PreparedStatement ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, photo.getPhoto());
      ps.setInt(2, idFurniture);
      ps.execute();
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

  @Override
  public int addAdminPhoto(PhotoDTO photo, int idFurniture) {
    int response = -1;
    try {
      String sql =
          "INSERT INTO pae.photos VALUES (default, ?, false, false, ?) RETURNING id_furniture;";
      PreparedStatement ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, photo.getPhoto());
      ps.setInt(2, idFurniture);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        response = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return response;
  }

  @Override
  public List<PhotoDTO> getFurniturePhotos(int idFurniture) {
    List<PhotoDTO> list = new ArrayList<PhotoDTO>();
    try {
      String sql = "SELECT id_photo, photo, is_visible, is_a_client_photo, "
          + "id_furniture FROM pae.photos WHERE id_furniture = ?;";

      PreparedStatement ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, idFurniture);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        PhotoDTO p = photoFactory.getPhotoDTO();
        p.setId(rs.getInt(1));
        p.setPhoto(rs.getString(2));
        p.setIdFurniture(idFurniture);
        p.setIsAClientPhoto(rs.getBoolean(4));
        p.setVisible(rs.getBoolean(3));

        list.add(p);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public boolean edit(int id, String description, int idType, double offeredSellingPrice,
      int favouritePhoto) {
    try {
      String sql = "UPDATE pae.furnitures SET description = ?, id_type = ?, "
          + "offered_selling_price = ?, favorite_photo = ? WHERE id_furniture = ?;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, description);
      ps.setInt(2, idType);
      ps.setDouble(3, offeredSellingPrice);
      ps.setInt(4, favouritePhoto);
      ps.setInt(5, id);
      ps.execute();

    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return true;
  }

  @Override
  public int deletePhoto(int id) {
    int response = -1;
    try {
      String sql = "DELETE FROM pae.photos WHERE id_photo = ? RETURNING id_furniture;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        response = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return response;
  }

  @Override
  public int displayPhoto(int id) {
    int response = -1;
    try {
      String sql =
          "UPDATE pae.photos SET is_visible = TRUE WHERE id_photo = ? RETURNING id_furniture;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        response = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return response;
  }

  @Override
  public int hidePhoto(int id) {
    int response = -1;
    try {
      String sql =
          "UPDATE pae.photos SET is_visible = FALSE WHERE id_photo = ? RETURNING id_furniture;";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        response = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return response;
  }

  @Override
  public void processFurniture(int id, String condition, double purchasePrice,
      LocalDateTime pickUpDate) {
    try {
      if (condition.equals(Condition.ACHETE.toString())) {
        String sql = "UPDATE pae.furnitures SET condition = ? , purchase_price = ? "
            + ", pick_up_date = ? WHERE id_furniture = ?;";
        ps = dalBackendService.getPreparedStatement(sql);
        ps.setString(1, condition);
        ps.setDouble(2, purchasePrice);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Timestamp date = Timestamp.valueOf(pickUpDate.format(formatter));
        ps.setTimestamp(3, date);
        ps.setInt(4, id);
        ps.execute();
      } else if (condition.equals(Condition.REFUSE.toString())) {
        String sql = "UPDATE pae.furnitures SET condition = ? WHERE id_furniture = ?;";
        ps = dalBackendService.getPreparedStatement(sql);
        ps.setString(1, condition);
        ps.setInt(2, id);
        ps.execute();
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }

  @Override
  public List<FurnitureDTO> getSliderFurnitureList(int limit) {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f, pae.photos p "
          + "WHERE p.id_photo = f.favorite_photo AND (f.condition = ? "
          + "OR f.condition = ? OR f.condition = ?)  ORDER BY random() LIMIT ?";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.EN_VENTE.toString());
      ps.setString(2, Condition.SOUS_OPTION.toString());
      ps.setString(3, Condition.VENDU.toString());
      ps.setInt(4, limit);
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public List<FurnitureDTO> getSliderFurnitureListByType(int limit, int idType) {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo WHERE (f.condition = ? "
          + "OR f.condition = ? OR f.condition = ?) AND f.id_type = ?"
          + " ORDER BY random() LIMIT ?";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setString(1, Condition.EN_VENTE.toString());
      ps.setString(2, Condition.SOUS_OPTION.toString());
      ps.setString(3, Condition.VENDU.toString());
      ps.setInt(5, limit);
      ps.setInt(4, idType);
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }

  @Override
  public List<FurnitureDTO> getTransactionsSeller(int id) {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    try {
      String sql = "SELECT f.id_furniture, f.condition, f.description, f.purchase_price, "
          + "f.pick_up_date, f.store_deposit, f.deposit_date, "
          + "f.offered_selling_price, f.id_type, f.request_visit, f.seller, f.favorite_photo, "
          + "p.photo FROM pae.furnitures f LEFT OUTER JOIN pae.photos p "
          + "ON p.id_photo = f.favorite_photo WHERE f.seller = ? " + "AND f.condition!='refusé';";
      ps = dalBackendService.getPreparedStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      FurnitureDTO furniture = null;
      while (rs.next()) {
        FurnitureDTO furnitureDTO = setFurniture(rs, furniture);
        furnitureDTO.setFavouritePhoto(rs.getString(13));
        list.add(furnitureDTO);
      }
    } catch (SQLException e) {
      throw new FatalException(e);
    }
    return list;
  }
}
