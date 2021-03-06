package model.repository.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "T_Order")
@NamedQueries({
        @NamedQuery(name = "Order.findAll", query = "SELECT o FROM OrderEntity o")
})
public class OrderEntity implements EntityInt {

    @Id
    @GeneratedValue(generator = "incrementor")
    @Column(name = "order_id", unique = true)
    public int id;

    @Version
    public int version;

    @Column
    public State status;

    @Column
    public Date sent;

    @Column
    public Date delivered;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id", nullable = false)
    public UserEntity user;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "order")
    public Set<OrderItemEntity> orderItems = new HashSet<>();

    public enum State {
        SENT, DELIVERED, KILLED;
    }

    public OrderEntity() { }

    public OrderEntity(UserEntity user) {
        setUser(user);
    }

    public void send() {
        sent = new Date();
        status = State.SENT;
    }

    public void deliver() {
        delivered = new Date();
        status = State.DELIVERED;
    }

    public void kill() {
        status = State.KILLED;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        if (this.user != null)
            this.user.orders.remove(this);
        this.user = user;
        this.user.orders.add(this);
    }

    public boolean isDelivered() {
        return delivered != null;
    }

    @Override
    public boolean beforeInsert(EntityManager em) {
        return false;
    }

    @Override
    public boolean beforeDelete(EntityManager em) {
        return false;
    }

    @Override
    public void update(EntityManager em, EntityInt fromEntity) {

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

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id=" + id +
                ", version=" + version +
                ", status=" + status +
                ", sent=" + sent +
                ", delivered=" + delivered +
                ", user.name=" + user.name +
                ", orderItems=" + orderItems +
                '}';
    }
}
