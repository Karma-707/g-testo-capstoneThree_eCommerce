package org.yearup.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
@RestController
@RequestMapping("cart")
@CrossOrigin
// only logged in users should have access to these actions
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController
{
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    //create an Autowired controller to inject the shoppingCartDao, userDao and productDao
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    // each method in this controller requires a Principal object as a parameter
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            logger.info("Fetching shopping cart for user: {}", userName);

            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            // use the shoppingcartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            logger.error("Failed to fetch cart", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("products/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCart addProductToCart(@PathVariable int id, Principal principal) {

        try {
            // get the currently logged in username
            String userName = principal.getName();
            logger.info("Adding product {} to cart for user: {}", id, userName);

            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            //add product to cart
            shoppingCartDao.addProductToCart(userId, id);

            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception e) {
            logger.error("Failed to add product to cart", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProductQuantity(@PathVariable int id, @RequestBody ShoppingCartItem shoppingCartItem, Principal principal) {
        try {
            // get the currently logged in username
            String userName = principal.getName();
            logger.info("Updating quantity of product {} in cart for user: {}", id, userName);

            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            logger.info("Updated product {} quantity to {} for user {}", id, shoppingCartItem.getQuantity(), userName);
            shoppingCartDao.updateProductQuantity(userId, id, shoppingCartItem.getQuantity());
        }
        catch (Exception e) {
            logger.error("Failed to update product quantity for user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not update quantity.");
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCart deleteCart(Principal principal) {
        try {
            // get the currently logged in username
            String userName = principal.getName();
            logger.info("Clearing cart for user: {}", userName);

            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            shoppingCartDao.clearCart(userId);
            logger.info("Cleared cart for user {}", userName);

            return shoppingCartDao.getByUserId(userId); //return now empty cart
        }
        catch (Exception e) {
            logger.error("Failed to clear cart for user", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not clear cart.");
        }
    }
}
