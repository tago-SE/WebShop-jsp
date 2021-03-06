package model.repository.dao;

import model.handlers.exceptions.DatabaseException;
import model.repository.entities.CategoryEntity;
import model.repository.entities.UserEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.List;

import static utils.LogManager.getLogger;

public class CategoriesDao extends BasicDao {


    public static CategoryEntity findById(int id) throws Exception {
        return (CategoryEntity) findById(new CategoryEntity(id));
    }

    public static boolean delete(int id) throws Exception {
        return delete(new CategoryEntity(id));
    }

    public static List<CategoryEntity> findAll() throws DatabaseException {
        EntityManagerFactory factory = getEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        try {
            List<CategoryEntity> found = em.createNamedQuery("Category.findAll").getResultList();
            getLogger().info("found: " + found.size());
            return found;
        } catch (Exception e) {
            getLogger().severe(e.getCause() + " :" + e.getMessage());
            throw new DatabaseException(e);
        } finally {
            em.close();
        }
    }

    public static CategoryEntity findByName(String name) throws DatabaseException {
        EntityManagerFactory factory = getEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        try {
            em.getTransaction().begin();
            List<CategoryEntity> found = em.createNamedQuery("Category.findByName").setParameter("name", name).getResultList();
            em.getTransaction().commit();
            if (found.size() == 1) {
                getLogger().info("found: " + found.size());
                return found.get(0);
            }
            return null;
        } catch (Exception e) {
            em.getTransaction().rollback();
            getLogger().severe(e.getCause() + " :" + e.getMessage());
            throw new DatabaseException(e);
        } finally {
            em.close();
        }
    }
}
