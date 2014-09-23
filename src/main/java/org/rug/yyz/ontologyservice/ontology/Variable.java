package org.rug.yyz.ontologyservice.ontology;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-11-25
 * Time: 下午4:42
 * To change this template use File | Settings | File Templates.
 */

/**
 * The class to describe an environment variable in terms of a smart environment and an individual in terms of an
 * ontology
 */
public class Variable {
    private int id;
    private String siname;
    private String name;
    private String location;
    private boolean isControllable;
    private String domain;
    private boolean isSlow;

    public Variable(int id, String siname, String name, String location, boolean controllable, String domain, boolean slow) {
        this.id = id;
        this.siname = siname;
        this.name = name;
        this.location = location;
        this.isControllable = controllable;
        this.domain = domain;
        this.isSlow = slow;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSiname() {
        return siname;
    }

    public void setSiname(String siname) {
        this.siname = siname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isControllable() {
        return isControllable;
    }

    public void setControllable(boolean controllable) {
        isControllable = controllable;
    }

    public boolean isSlow() {
        return isSlow;
    }

    public void setSlow(boolean slow) {
        isSlow = slow;
    }
}