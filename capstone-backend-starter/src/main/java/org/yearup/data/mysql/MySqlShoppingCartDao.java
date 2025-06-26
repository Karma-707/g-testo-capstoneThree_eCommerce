package org.yearup.data.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    private static final Logger logger = LoggerFactory.getLogger(MySqlShoppingCartDao.class);

    private ProductDao productDao;

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao) {
        super(dataSource);
        this.productDao = productDao;
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();

        String query = "SELECT * FROM shopping_cart WHERE user_id = ?;";

        logger.info("Fetching shopping cart for userId: {}", userId);

        try(
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        )
        {
            preparedStatement.setInt(1, userId);

            try(
                    ResultSet resultSet = preparedStatement.executeQuery();
            )
            {
                if(resultSet.next()) {
                    do{
                        ShoppingCartItem item = mapRow(resultSet);
                        shoppingCart.add(item); //add item to cart

                        logger.debug("Added item to cart: productId={}, quantity={}",
                                item.getProductId(), item.getQuantity());

                    } while(resultSet.next());
                    logger.info("Finished building cart. Total items: {}", shoppingCart.getItems().size());

                }
                else {
//                    System.out.println("No cart found with that id");
                    logger.info("No cart items found for userId: {}", userId);

                }
            }

        } catch (SQLException e) {
//            e.printStackTrace();
            logger.error("SQL error while fetching cart for userId: {}", userId, e);
        }

        return shoppingCart;
    }

    @Override
    public void addProductToCart(int userId, int productId) {
        //add item to cart
        //item already in cart up 1??
        int quantityToAdd = 1;
        String updateQuery = "UPDATE shopping_cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?;";
        String insertQuery = "INSERT INTO shopping_cart(user_id, product_id, quantity) VALUES(?, ?, ?);";

        try (
                Connection connection = getConnection();
                PreparedStatement updatePS = connection.prepareStatement(updateQuery);
        )
        {
            updatePS.setInt(1, quantityToAdd);
            updatePS.setInt(2, userId);
            updatePS.setInt(3, productId);

            int rowsAffected = updatePS.executeUpdate();

            //its a new product add to cart
            if(rowsAffected == 0) {

                try (
                        PreparedStatement insertPS = connection.prepareStatement(insertQuery);
                )
                {
                    insertPS.setInt(1, userId);
                    insertPS.setInt(2, productId);
                    insertPS.setInt(3, quantityToAdd);
                    insertPS.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {
        //update quantity
        String query = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?;";

        try (
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        )
        {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3,productId);

            preparedStatement.executeUpdate();
            logger.info("Updated quantity to {} for userId={} and productId={}", quantity, userId, productId);

        } catch (SQLException e) {
            logger.error("Failed to update product quantity in cart for userId={} and productId={}", userId, productId, e);
//            e.printStackTrace();
        }
    }

    @Override
    public void clearCart(int userId) {
        String query = "DELETE FROM shopping_cart WHERE user_id = ?;";

        try (
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        )
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //check if there is product in cart
    @Override
    public boolean productInCart(int userId, int productId) {
        String query = "SELECT COUNT(*) FROM shopping_cart WHERE user_id = ? AND product_id = ?;";

        try(
                Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
            )
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);

            try(
                    ResultSet resultSet = preparedStatement.executeQuery();
                )
            {
                if(resultSet.next()) {
                    return resultSet.getInt(1) > 0; //get 1st col value
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private ShoppingCartItem mapRow(ResultSet row) throws SQLException {
        int userId = row.getInt("user_id");
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");

        //get whole product info
        Product product = productDao.getById(productId);

        //build the cart item
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(quantity);

        return item;
    }

}