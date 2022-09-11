package haneki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Config {
    private String mysql_ip;
    private int mysql_port;
    private String mysql_user;
    private String mysql_password;
    private HashMap<Long,GroupConfig> groupConfigList = new HashMap<>();
    private List<Long> operator = new ArrayList<>();
    private List<Long> superadmin = new ArrayList<>();
    private List<Long> blackList = new ArrayList<>();
    public Config(){

    }

    public String getMysql_ip() {
        return mysql_ip;
    }

    public void setMysql_ip(String mysql_ip) {
        this.mysql_ip = mysql_ip;
    }

    public int getMysql_port() {
        return mysql_port;
    }

    public void setMysql_port(int mysql_port) {
        this.mysql_port = mysql_port;
    }

    public String getMysql_user() {
        return mysql_user;
    }

    public void setMysql_user(String mysql_user) {
        this.mysql_user = mysql_user;
    }

    public String getMysql_password() {
        return mysql_password;
    }

    public void setMysql_password(String mysql_password) {
        this.mysql_password = mysql_password;
    }

    public HashMap<Long, GroupConfig> getGroupConfigList() {
        return groupConfigList;
    }

    public void setGroupConfigList(HashMap<Long, GroupConfig> groupConfigList) {
        this.groupConfigList = groupConfigList;
    }

    public List<Long> getOperator() {
        return operator;
    }

    public void setOperator(List<Long> operator) {
        this.operator = operator;
    }

    public List<Long> getSuperadmin() {
        return superadmin;
    }

    public void setSuperadmin(List<Long> superadmin) {
        this.superadmin = superadmin;
    }

    public List<Long> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<Long> blackList) {
        this.blackList = blackList;
    }
}
