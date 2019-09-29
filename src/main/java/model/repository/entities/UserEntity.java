package model.repository.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Users")
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "SELECT u FROM  UserEntity u"),
        @NamedQuery(name = "User.findById", query = "SELECT u FROM  UserEntity u WHERE u.id = :id"),
        @NamedQuery(name = "User.findByName", query = "SELECT u FROM  UserEntity u WHERE u.name = :name"),
        @NamedQuery(name = "User.findByNameContains", query = "SELECT u FROM UserEntity u WHERE u.name LIKE :search"),
        @NamedQuery(name = "User.validateCredentials", query = "SELECT u FROM  UserEntity u WHERE u.name = :name and u.password = :password")
})
public class UserEntity implements EntityInt {

    @Id
    @GeneratedValue(generator = "incrementor")
    @Column(name = "user_id", unique = true)
    public int id;

    @Version
    public int version;

    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @Column(name = "pass", nullable = false)
    public String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="Access", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="access")
    public Set<String> accessRoles = new HashSet<>();

    public UserEntity() {}

    public UserEntity(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public void addAccess(String access) {
        if (access == null)
            return;
        if (!accessRoles.contains(access)) {
            accessRoles.add(access);
        }
    }

    public void removeAccess(String access) {
        accessRoles.remove(access);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void transferTo(EntityInt toEntity) {
        UserEntity dest = (UserEntity) toEntity;
        // Should only transfer user access roles...
        dest.name = this.name;
    }

    @Override
    public Query createVerifyIsUniqueQuery(EntityManager em) {
        return em.createNamedQuery("User.findByName").setParameter("name", name);
    }

    @Override
    public Query createFindByIdQuery(EntityManager em) {
        return em.createNamedQuery("User.findById")
                .setParameter("id", id);
    }

    public Query createValidateCredentials(EntityManager em) {
        return em.createNamedQuery("User.validateCredentials")
                .setParameter("name", name)
                .setParameter("password", password);
    }

    public Query createFindAll(EntityManager em) {
        return em.createNamedQuery("User.findAll");
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", accessRoles=" + accessRoles +
                '}';
    }
}