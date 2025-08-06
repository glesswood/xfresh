/*
package pl.piomin.services.department;

import com.xfresh.product.repository.OrderRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.util.Assert;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderRepositoryTest {

    private static final OrderRepository repository = new OrderRepository();

    @Test
    @Order(1)
    void testAddDepartment() {
        Product product = new Product(1L, "Test");
        product = repository.add(product);
        Assert.notNull(product, "Product is null.");
        Assert.isTrue(product.getId() == 1L, "Product bad id.");
    }

    @Test
    @Order(2)
    void testFindAll() {
        List<Product> products = repository.findAll();
        Assert.isTrue(products.size() == 1, "Departments size is wrong.");
        Assert.isTrue(products.get(0).getId() == 1L, "Product bad id.");
    }

    @Test
    @Order(3)
    void testFindByOrganization() {
        List<Product> products = repository.findByOrganization(1L);
        Assert.isTrue(products.size() == 1, "Departments size is wrong.");
        Assert.isTrue(products.get(0).getId() == 1L, "Product bad id.");
    }

    @Test
    @Order(4)
    void testFindById() {
        Product product = repository.findById(1L);
        Assert.notNull(product, "Product not found.");
        Assert.isTrue(product.getId() == 1L, "Product bad id.");
    }

}
*/
