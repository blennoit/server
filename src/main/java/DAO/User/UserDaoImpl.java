package DAO.User;


import Hibernate.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * UserDaoImpl si occupa di creare le query definite nell'interfaccia UserDao
 */
public class UserDaoImpl implements UserDAO {
    // attributi necessari per creare le query tramite hibernate
    private SessionFactory sessionFactory;//strettamente collegato alla classe HibernateUtil che gestisce le sessioni con il db
    private Session session;
    // costruttore della classe
    public UserDaoImpl(){
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.session= sessionFactory.openSession();
    }
    /**
     * 1: query per inserire un nuovo user
     */
    public void addUser(User user) {
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();
        session.save(user);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * 2: query per selezionare tutti gli user presenti nel db
     * @return
     */

    public List<User> getAllUsers(String email) {
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();


        List<User> userList = session.createQuery("from User WHERE email!=:email and role is not null order by rand()").setMaxResults(7)

                .setParameter("email", email).list();
        session.getTransaction().commit();
        session.close();
        return userList;

    }

    /**
     * 3: query per cancellare uno user dal db utilizzando il suo id
     * @param id_user
     */

    public void deleteUser(Integer id_user) {
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();
        Query query = session.createQuery("from User where id_user=:id_user");
        query.setParameter("id_user", id_user);
        User user = (User)query.uniqueResult();
        session.delete(user);
        session.getTransaction().commit();
        session.close();

    }

    /**
     * //4: query per selezionare un singolo user data un email
     * @param email
     * @return
     */

    public User getUser(String email) {
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();

        Query query = session.createQuery("from User where email=:email");
        query.setParameter("email", email);
        User user = (User)query.uniqueResult();

        session.getTransaction().commit();
        session.close();

        if(null != user){
            return user;
        } else{ return null;}
    }

    /**
     * //5: query per selezionare una lista di user dati uno o piu' parametri utilizzando Criteria per la creazione della query
     * @param name
     * @param city
     * @param rate
     * @param role
     * @return
     */

    public List<User> getUserByAttributes(String name, String city, double rate, String role) {
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();

        List<User> listUser = session.createCriteria(User.class)
                .add(Restrictions.like("name", check(name)))
                .add(Restrictions.like("city", check(city)))
                .add(Restrictions.lt("rate", checkDouble(rate)))
                .add(Restrictions.like("role", check(role))).list();

        session.getTransaction().commit();
        session.close();
        return listUser;
    }

    /**
     * //6: query utilizzata per il login che restituisce un user se la coppia email-password e' corretta
     * @param email
     * @param password
     * @return
     */
    public User getUserIfExist(String email, String password){
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();

        Query query=session.createQuery("from User where (email=:email and password=:password)");
        query.setParameter("email", email);
        query.setParameter("password", password);
        User user = (User)query.uniqueResult();

        session.getTransaction().commit();
        session.close();
        return user;

    }

    /**
     * 7: query che resituisce vero o false se la coppia email-password e' corretta
     */

    public boolean authentication(String email, String password){
        if(!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();
        Query query=session.createQuery("from User where (email=:email and password=:password)");
        query.setParameter("email", email);
        query.setParameter("password", password);
        User user = (User)query.uniqueResult();
        session.getTransaction().commit();
        session.close();
        if (user==null){
            return false;
        }return true;
    }

    /**
     * 8: query che permette di modificare uno user
     */
    public void updateUser(int id_user, String name, String surname, String email, String role, String city,
                           double rate, boolean status, String description){
        if (!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();

        Object object = session.load(User.class, id_user);
        User user = (User) object;

        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setRole(role);
        user.setCity(city);
        user.setRate(rate);
        user.setActive(status);
        user.setDescription(description);
        session.getTransaction().commit();
        session.close();

    }


    @Override
    public List<User> getAllUsersWithMessage(int id_user) {
        if (!session.isOpen()) {
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();
        List<User> userList = session.createQuery(" FROM User WHERE id_user in (" +
                "SELECT id_sender FROM Message WHERE id_receiver=" + id_user + ")" +
                " or " +
                "id_user in(SELECT id_receiver FROM Message WHERE id_sender=" + id_user + ")").list();
        session.getTransaction().commit();
        session.close();
        return userList;
    }

    @Override
    public void updateStatus(User user) {
        if (!session.isOpen()){
            session = sessionFactory.openSession();
        }
        session.getTransaction().begin();

        Object object = session.load(User.class, user.getId_user());
        User updateduser = (User) object;
        updateduser.setActive(user.isActive());
        session.getTransaction().commit();
        session.close();
    }


    //metodo che setta la rate ad un valore alto se non viene inserita un valore nella richiesta
    private double checkDouble(double rate) {
        if (rate==0){
            return Double.MAX_VALUE;
        }return rate;
    }

    //metodo per prendere ogni tipo di nome se nella chiamata non vengono inseriti nomi
    private String check(String name) {
        if(name == null){
            return "%";
        }return name + "%";
    }

}
