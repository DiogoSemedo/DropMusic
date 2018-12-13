package action;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Map;

public class RegistAction extends ActionSupport implements SessionAware, ModelDriven {
    private User user = new User();
    private Map<String,Object> session;
    @Override
    public void validate(){
        if(StringUtils.isEmpty(user.getUsername())){
            addFieldError("username","User ID cannot be blank");
        }
        if(StringUtils.isEmpty(user.getPassword())){
            addFieldError("password","Password cannot be blank");
        }
    }

    public String execute(){
        if(user.getUsername().equals("ola") && user.getPassword().equals("adeus"))
            return SUCCESS;
        return LOGIN;
    }

    @Override
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Object getModel() {
        return user;
    }
}
