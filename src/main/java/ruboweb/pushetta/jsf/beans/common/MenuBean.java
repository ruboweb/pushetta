package ruboweb.pushetta.jsf.beans.common;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class MenuBean implements Serializable {

	private static final long serialVersionUID = 1651519768277677012L;

	private String CSS_ID = "current";
	private String users;
	private String createOne;
	private String listReports;
	private String sendNotifications;
	private String api;

	public MenuBean() {

	}

	public void activeUsers() {
		this.users = this.CSS_ID;
		this.createOne = "";
		this.listReports = "";
		this.sendNotifications = "";
		this.api = "";
	}

	public void activeCreationOne() {
		this.users = "";
		this.createOne = this.CSS_ID;
		this.listReports = "";
		this.sendNotifications = "";
		this.api = "";
	}

	public void activeListReports() {
		this.users = "";
		this.createOne = "";
		this.listReports = this.CSS_ID;
		this.sendNotifications = "";
		this.api = "";
	}

	public void activeSendNotifications() {
		this.users = "";
		this.createOne = "";
		this.listReports = "";
		this.sendNotifications = this.CSS_ID;
		this.api = "";
	}

	public void activeApi() {
		this.users = "";
		this.createOne = "";
		this.listReports = "";
		this.sendNotifications = "";
		this.api = this.CSS_ID;
	}

	public String getCSS_ID() {
		return CSS_ID;
	}

	public String getUsers() {
		return users;
	}

	public String getCreateOne() {
		return createOne;
	}

	public String getListReports() {
		return listReports;
	}

	public String getSendNotifications() {
		return sendNotifications;
	}

	public String getApi() {
		return api;
	}

}
