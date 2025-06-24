package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        List<ShoppingCartItem> items = new ArrayList<>();

        String query = "";
        return null;
    }

    @Override
    public void addProductToCart(int userId, int productId) {

    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {

    }

    @Override
    public void clearCart(int userId) {

    }

    private ShoppingCart mapRow(ResultSet row) throws SQLException {
        int userId = row.getInt("user_id");
        int productId = row.getInt("prodcut_id");
        int quantity = row.getInt("quantity");

        ShoppingCart shoppingCart = new ShoppingCart();

    }

}
