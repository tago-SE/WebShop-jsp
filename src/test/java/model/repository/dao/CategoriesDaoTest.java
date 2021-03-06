package model.repository.dao;

import model.repository.entities.CategoryEntity;
import model.repository.entities.ItemEntity;
import org.junit.Test;
import view.viewmodels.Category;

import java.util.List;

import static org.junit.Assert.*;

public class CategoriesDaoTest {

    @Test
    public void insert() throws Exception {
        String name0 = "" + Math.random();
        // Should be able to insert a entity with a unique name
        CategoryEntity inserted = (CategoryEntity) CategoriesDao.insert(new CategoryEntity(name0));
        assertTrue(inserted != null);

        // Should not be able to insert a entity with the same name
        assertFalse(CategoriesDao.insert(new CategoryEntity(name0)) != null);

        CategoriesDao.delete(new CategoryEntity(inserted.id));
        // Should not be able to insert an entity with a id
        assertThrows(Exception.class, ()-> {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.name = "hello";
            categoryEntity.id = 5;
            CategoriesDao.insert(categoryEntity);
        });
    }

    @Test
    public void update() throws Exception {
        String name0 = "" + Math.random();
        CategoryEntity category = new CategoryEntity(name0);
        category = (CategoryEntity) CategoriesDao.insert(category);
        name0 = "" + Math.random();
        category.name = name0;
        category = (CategoryEntity) CategoriesDao.update(category);
        // Successful update
        assertNotNull(category);
        assertEquals(1, category.version);
        int id = category.id;

        category.version = 0;
        String name1 = "" + Math.random();
        category.name = name1;
        // Failed update due to version being out of date
        category = (CategoryEntity) CategoriesDao.update(category);
        assertNull(category);
        category = CategoriesDao.findById(id);
        // Delete test entity
        assertTrue(CategoriesDao.delete(category));

        // Assert that version can start from any number
        name0 = "" + Math.random();
        category = new CategoryEntity(name0);
        category.version = 5;
        category = (CategoryEntity) CategoriesDao.insert(category);
        name0 = "" + Math.random();
        category.name = name0;
        category = (CategoryEntity) CategoriesDao.update(category);
        assertNotNull(category);
        assertEquals(6, category.version);
        CategoriesDao.delete(category);
    }

    @Test
    public void delete() throws Exception {
        String name0 = "" + Math.random();
        CategoryEntity category = new CategoryEntity(name0);
        CategoryEntity inserted = (CategoryEntity) CategoriesDao.insert(category);
        List<CategoryEntity> resultList = CategoriesDao.findAll();

        CategoryEntity toDelete = new CategoryEntity(inserted.id);
        CategoriesDao.delete(toDelete);
        boolean foundDeleted = false;
        resultList = CategoriesDao.findAll();
        for (CategoryEntity c : resultList) {
            if (c.name.equals(category.name)) {
                foundDeleted = true;
                break;
            }
        }
        assertFalse(foundDeleted);
        // Double deletions
    }

    @Test
    public void findByName() throws Exception {
        String name ="Pants";
       // CategoryEntity category = new CategoryEntity(name);
        //category = (CategoryEntity) CategoriesDao.insert(category);
        //assertNotNull(category);
        CategoryEntity found = CategoriesDao.findByName(name);
        assertNotNull(found);
        System.out.println("ITEMS: " + found.items.toString());
        found = CategoriesDao.findByName(name + "asdlasd");
        assertNull(found);
        //CategoriesDao.delete(category);
    }

    @Test
    public void categoryItemsMapping() throws Exception {


        CategoryEntity category = new CategoryEntity("Pants" + Math.random());
        ItemEntity i1 = new ItemEntity("Red", 0, 0);
        ItemEntity i2 = new ItemEntity("Blue", 0, 0);
        category.items.add(i1);
        category.items.add(i2);
        i1.categories.add(category);
        i2.categories.add(category);
        CategoryEntity inserted = (CategoryEntity) CategoriesDao.insert(category);

        System.out.println("Inserted Category: " + inserted.id);
        System.out.println("Inserted items: " + i1.id + " " + i2.id);

        assertNotNull(inserted);

        CategoriesDao.delete(inserted);
    }

    @Test
    public void findAll() throws Exception {
        List<CategoryEntity> categories = CategoriesDao.findAll();
        System.out.println("NUM CATEGORIES = " + categories.size());
    }
}