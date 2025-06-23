package org.yearup.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("products")
@CrossOrigin
public class ProductsController
{
    private static final Logger logger = LoggerFactory.getLogger(ProductsController.class);

    private ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Product> search(@RequestParam(name="cat", required = false) Integer categoryId,
                                @RequestParam(name="minPrice", required = false) BigDecimal minPrice,
                                @RequestParam(name="maxPrice", required = false) BigDecimal maxPrice,
                                @RequestParam(name="color", required = false) String color
                                )
    {
        logger.info("Searching products with filters - categoryId: {}, minPrice: {}, maxPrice: {}, color: {}",
                categoryId, minPrice, maxPrice, color);

        try
        {
            return productDao.search(categoryId, minPrice, maxPrice, color);
        }
        catch(Exception ex)
        {
            logger.error("Error searching products", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    @ResponseStatus(HttpStatus.OK)
    public Product getById(@PathVariable int id )
    {
        logger.info("Fetching product with ID: {}", id);

        try
        {
            var product = productDao.getById(id);

            if(product == null) {
                logger.warn("Product with ID {} not found", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }
            return product;
        }
        catch(Exception ex)
        {
            logger.error("Error fetching product with ID: " + id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@RequestBody Product product)
    {
        logger.info("Adding new product: {}", product);

        try
        {
            return productDao.create(product);
        }
        catch(Exception ex)
        {
            logger.error("Error adding product: {}", product, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        logger.info("Updating product with ID {}: {}", id, product);

        try
        {
            productDao.update(id, product);
        }
        catch(Exception ex)
        {
            logger.error("Error updating product with ID {}: {}", id, product, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable int id)
    {
        logger.info("Deleting product with ID: {}", id);

        try
        {
            var product = productDao.getById(id);

            if(product == null) {
                logger.warn("Product with ID {} not found for deletion", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }

            productDao.delete(id);
        }
        catch(Exception ex)
        {
            logger.error("Error deleting product with ID: " + id, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
