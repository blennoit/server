package DAO.Message;

import DAO.User.User;

import java.util.List;

public interface MessageDAO {

    public void sendMessage(Message message);
    public void setRead(User receiver, User sender);
    public List<Message> getMessageByUser(User user);
    public List<User> getAllUsersWithMessage(int id_user);
    public List<Message> getMessageByTwoUser(User user,User me);
    public int number_messages_not_read(User user);

}
