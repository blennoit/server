package Spark;

import DAO.Feedback.Feedback;
import DAO.Feedback.FeedbackDAOImpl;
import DAO.Follow.Follow;
import DAO.Follow.FollowDaoImpl;
import DAO.Message.Message;
import DAO.Message.MessageDAOImpl;
import DAO.Notice.Notice;
import DAO.Notice.NoticeDAOImpl;
import DAO.Token.TokenManager;
import DAO.User.User;
import DAO.UserManager.UserManager;
import DAO.UserManager.UserManagerImpl;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static Json.JsonUtil.json;
import static spark.Spark.*;

/**
 * UserController e' la classe che si occupa di gestire tutte le richieste ad esclusione di quelle di login.
 */

public class UserController {

    public UserController(final UserManagerImpl userManager, final TokenManager tokenManager) {

        /**
         * Imponiamo di farci dare il token di accesso prima di ogni richiesti /api. In tal modo possiamo gestire le sessioni e far riloggare se i token sono scaduti
         */
        before("/api/*", (request, response) -> {
            if (!tokenManager.isATokenActive(request.headers("Authorization"))) {
                halt(403, "403");// torniamo un errore se il token di accesso e' scaduto. Si richiede in questo caso di rieffettuare login con il refresh token per ottenere un nuovo token access
            }
        });

        /**
         * /users :url per ricevere la lista completa degli user presenti in db
         */

        post("/users", (request, response) -> {
            List<User> userList = userManager.getAllUsers(request.queryParams("email"));
            return userList;
        }, json());

        after((req, res) -> res.type("application/json"));

        /**
         * /add: url per inserire un nuovo user in db. La chiamata viene effettuata in fase di registrazione.
         */
        post("/add", (request, response) -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date bday = formatter.parse(request.queryParams("bday"));
            double rate = Double.parseDouble(request.queryParams("rate"));
            String name = request.queryParams("name");
            String surname = request.queryParams("surname");
            String email = request.queryParams("email");
            String password = request.queryParams("password");
            String role = request.queryParams("role");
            String city = request.queryParams("city");
            String description = "Inserisci descrizione";
            User user = new User(name, surname, email, password, bday, role, city, rate, true, description);
            if (userManager.getUser(request.queryParams("email")) == null) {
                userManager.addUser(user);
                return "OK";
            } else {
                return "NO";
            }
        });
        get("/add", ((request, response) -> "Empty"));


        /**
         * /getuser url per ricevere i dati di un utente tramite il parametro email
         */
        post("/getuser", ((request, response) -> {
            User user = userManager.getUser(request.queryParams("email"));
            if (user != null) {
                return user;
            } else {
                return "No user have this email";
            }
        }), json());

        /**
         * /delete url per cancellare un utente. Sara' effettuata nel profilo utente se si vuole deletare il proprio account
         */
        post("/delete", ((request, response) -> {
            userManager.deleteUser(Integer.parseInt(request.queryParams(("id_user"))));
            return "";
        }));

        /**
         * /getFiltered url che gestisce tutte le ricerche che si possono effetuare tramite i parametri di: name surname city rate role
         */
        post("/getFiltered", (request, response) -> {

            List<User> list = userManager.getUserByAttributes(request.queryParams("name"), request.queryParams("city"), check(request.queryParams("rate")), request.queryParams("role"));
            return list;

        }, json());
        /**
         * /update url che gestisce tutte le modifiche rispetto ad un utente. ricevendo come parametri tutti gli attributi tranne l'id
         */
        post("/update", ((request, response) -> {
            //TODO sistemare i parametri che riceve dato che non sono ancora completi.

            int id_user = Integer.parseInt(request.queryParams(("id_user")));
            String name = request.queryParams("name");
            String surname = request.queryParams("surname");
            String email = request.queryParams("email");
            String role = request.queryParams("role");
            String city = request.queryParams("city");
            double rate = Double.parseDouble(request.queryParams("rate"));
            boolean status = Boolean.parseBoolean(request.queryParams("status"));
            String description = request.queryParams("description");
            User user = userManager.getUser(email);
            String notice = request.queryParams("notice");
            Notice notice1 =  new Notice(user,notice,new Date());
            new NoticeDAOImpl().insert_notice(notice1);
            userManager.updateUser(id_user, name, surname, email, role, city,rate, status, description);
            return "ok";
        }));

        post("/updateStatus", ((request, response) -> {
            //TODO sistemare i parametri che riceve dato che non sono ancora completi.


            String email = request.queryParams("email");
            boolean status = Boolean.parseBoolean(request.queryParams("status"));
            User user = userManager.getUser(email);
 ;          user.setActive(status);
            userManager.updateStatus(user);
            return "ok";
        }));

        /**
         * /getFollowers url che dato un id_utente andra' ad individuare tutti gli users che seguono questo utente.
         */
        post("/getFollowers", ((request, response) -> {
            User user = userManager.getUser(request.queryParams("email"));
            List<User> userList = new FollowDaoImpl().getFollowersByUser(user);
            return userList;
        }), json());


        post("/getinContactUsers", (request, response) -> {
            return userManager.getAllUsersWithMessage(Integer.parseInt(request.queryParams("id_user")));
        }, json());


        post("/save", (request, response) -> {

            String name = request.queryParams("nome");
            String file = request.queryParams("file");

            InputStream image = new ByteArrayInputStream(Base64.getDecoder().decode(file.getBytes()));
            BufferedImage bufferedImage = ImageIO.read(image);
            File file_image = new File(name+".png");
            ImageIO.write(bufferedImage, "png", file_image);

           /* MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/home/jns/Documents/server");
            request.raw().setAttribute("org.eclipse.multipartConfig", multipartConfigElement);

            return request.raw().getPart("file").getName();//image.getName();*/
            return "OK";
        });
    }


    //metodo utilizzato per gestire le chiamate con un parametro rate all'interno. in particolare se rate non viene compilato questo viene impostato come max value nelle ricerche
    private double check(String rate) {
        if (rate == null) {
            return Double.MAX_VALUE;
        }
        return Double.parseDouble(rate);
    }

}
