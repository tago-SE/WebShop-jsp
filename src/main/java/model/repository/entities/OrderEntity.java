package model.repository.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Deprecated
public class OrderEntity implements EntityInt {

    @Id
    public int id;

    @Version
    public int version;

    @Column
    public String status;

    @Column
    public Date dateRequest;

    @Column
    public Date dateSent;

    @Column
    public UserEntity user;

    //public Set<CartItemEntity> orderItems = new HashSet<>();

    @Override
    public boolean beforeInsert(EntityManager em) {
        return false;
    }

    @Override
    public boolean beforeDelete(EntityManager em) {
        return false;
    }

    @Override
    public void update(EntityManager em, EntityInt toEntity) {

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
    public Query createVerifyIsUniqueQuery(EntityManager em) {
        return null;
    }
}
