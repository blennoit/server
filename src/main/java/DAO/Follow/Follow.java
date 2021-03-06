package DAO.Follow;

import DAO.User.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by jns on 24/11/15.
 */
@Entity
@Table(name = "follow")
public class Follow {

    private int id_follow;
    private User id_user;
    private User target_id_user;
    private Date start_follow_date;

    public Follow(User id_user, User target_id_user, Date start_follow_date) {
        this.id_user = id_user;
        this.target_id_user = target_id_user;
        this.start_follow_date = start_follow_date;
    }

    public Follow() {
    }

    @Type(type = "date")
    public Date getStart_follow_date() {
        return start_follow_date;
    }

    public void setStart_follow_date(Date start_follow_date) {
        this.start_follow_date = start_follow_date;
    }

    @Id
    @GeneratedValue
    public int getId_follow() {
        return id_follow;
    }

    public void setId_follow(int id_follow) {
        this.id_follow = id_follow;
    }

    @ManyToOne
    @JoinColumn(name = "id_user")
    public User getId_user() {
        return id_user;
    }

    public void setId_user(User id_user) {
        this.id_user = id_user;
    }

    @ManyToOne
    @JoinColumn(name = "target_id_user")
    public User getTarget_id_user() {
        return target_id_user;
    }

    public void setTarget_id_user(User target_id_user) {
        this.target_id_user = target_id_user;
    }
}
