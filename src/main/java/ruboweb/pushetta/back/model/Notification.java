package ruboweb.pushetta.back.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "notification")
public class Notification extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 48611217163723666L;

	@Column(nullable = false)
	private Date creationDate;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;

	@Column(nullable = false, length = 300)
	private String text;

	@Column(nullable = false)
	@Type(type = "date")
	private Date scheduleDate;

	@Column
	private String status;

	@Column
	private Date trySend;

	public Notification() {
		this.status = null;
		this.trySend = null;
		this.creationDate = new Date(System.currentTimeMillis());
	}

	public Notification(User user, String text, String date) {

		try {
			this.scheduleDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Notification#new. date has wrong format. Expected: yyyy-MM-dd");
		}

		text = text.replaceAll("\r", "");
		text = text.replaceAll("\n", "");
		this.user = user;
		this.text = text;
		this.creationDate = new Date(System.currentTimeMillis());
		this.status = null;
		this.trySend = null;
	}

	public String getStatusName() {
		if (this.status == null) {
			return "Pending";
		}
		
		if (this.status.equals("")) {
			return "Pending";
		}
		
		if (this.status.startsWith("ERROR")) {
			return "Error";
		}
		
		if (this.status.startsWith("SENT")) {
			return "Send";
		}
		
		return "";
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getTrySend() {
		return trySend;
	}

	public void setTrySend(Date trySend) {
		this.trySend = trySend;
	}

}
