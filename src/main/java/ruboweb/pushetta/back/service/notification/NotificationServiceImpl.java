package ruboweb.pushetta.back.service.notification;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ruboweb.pushetta.back.model.Notification;
import ruboweb.pushetta.back.model.User;
import ruboweb.pushetta.back.repository.NotificationRepository;
import ruboweb.pushetta.back.repository.UserRepository;
import ruboweb.pushetta.exception.NotificatorException;

import com.codahale.metrics.annotation.Timed;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

	private static final Logger logger = LoggerFactory
			.getLogger(NotificationServiceImpl.class);

	@Value("${pushetta.host}")
	private String host;

	@Value("${pushetta.api}")
	private String api;

	@Value("${pushetta.user.token}")
	private String token;

	@Value("${pushetta.user.channel}")
	private String channel;

	@Value("${pushetta.cfg.days_available}")
	private String daysAvailable;

	@Value("${pushetta.cfg.enable}")
	private String enable;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserRepository userRepository;

	@Timed
	@Override
	public Notification createNotification(Notification n) {
		if (n == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#createNotification n must not be null");
		}

		if (n.getUser() == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#createNotification n.user must not be null");
		}

		if (n.getUser().getId() == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#createNotification n.user.id must not be null");
		}

		User usr = this.userRepository.findOne(n.getUser().getId());
		if (usr == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#createNotification user not exist in data base");
		}

		return this.notificationRepository.save(n);
	}

	@Timed
	@Override
	public Notification createNotificationAndSend(Notification n) {
		n = this.createNotification(n);
		n = this.notificationRepository.findOne(n.getId());
		this.send(n);
		return n;
	}

	@Timed
	@Override
	@Scheduled(cron = "${cron.send.notifications}")
	public void sendNotifications() {
		List<Notification> notificatios = this.notificationRepository
				.findNotificationsToSend();
		if (notificatios != null && notificatios.size() > 0) {
			for (Notification n : notificatios) {
				this.send(n);
			}
		}
	}

	@Timed
	@Override
	public void sendNotificationsWithError() {
		List<Notification> notificatios = this.notificationRepository
				.findNotificationsWithError();
		if (notificatios != null && notificatios.size() > 0) {
			for (Notification n : notificatios) {
				this.send(n);
			}
		}
	}

	@Timed
	@Override
	public void deleteNotification(Long id) {
		if (id == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#deleteNotification id must not be null");
		}

		Notification n = this.notificationRepository.findOne(id);

		if (n == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#deleteNotification id not exist");
		}

		this.notificationRepository.delete(id);
	}

	@Timed
	@Override
	public Notification findOneNotification(Long id) {
		if (id == null) {
			throw new IllegalArgumentException(
					"NotificationServiceImpl#findOneNotification id must not be null");
		}
		return this.notificationRepository.findOne(id);
	}

	@Timed
	@Override
	public List<Notification> getListNotifications() {
		return this.notificationRepository.findAll();
	}

	@Timed
	@Override
	public List<Notification> getListNotificationsPending() {
		return this.notificationRepository.findNotificationsToSend();
	}

	@Timed
	@Override
	public List<Notification> getListNotificationsError() {
		return this.notificationRepository.findNotificationsWithError();
	}

	@Timed
	@Override
	public List<Notification> getListNotificationsSent() {
		return this.notificationRepository.findNotificationsSent();
	}

	private void send(Notification n) {
		try {
			this.sendPushetta(n);
			n.setStatus("SENT");
			n.setTrySend(new Date(System.currentTimeMillis()));
			this.notificationRepository.save(n);
		} catch (NotificatorException e) {
			n.setStatus("ERROR: " + e.getMessage());
			n.setTrySend(new Date(System.currentTimeMillis()));
			this.notificationRepository.save(n);
			logger.error("Error trying to send notification id (" + n.getId()
					+ "). " + e.getMessage());
		}
	}

	private void sendPushetta(Notification n) throws NotificatorException {
		try {
			if (!Boolean.parseBoolean(this.enable)) {
				logger.error("pushetta.cfg.enable is not enabled");
				throw new NotificatorException(
						"pushetta.cfg.enable is not enabled");
			}
		} catch (Exception e) {
			logger.error("Invalid boolean value ${pushetta.cfg.enable}");
			throw new NotificatorException(
					"Invalid boolean value ${pushetta.cfg.enable}");
		}

		try {
			String dateAvailable = this.getDate(this.daysAvailable);
			String url = this.api + this.channel + "/";

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// set reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Host", this.host);
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Authorization", "Token " + this.token);
			con.setRequestProperty("Accept-Language", "es-ES,en;q=0.5");
			con.setRequestProperty("Content-Type", "application/json");

			// API parameters
			String urlParameters = "{ \"body\" : \"" + n.getText()
					+ "\", \"message_type\" : \"text/plain\", \"expire\" : \""
					+ dateAvailable + "\"  }";

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			con.getResponseCode();
			con.getResponseMessage();
		} catch (Exception e) {
			throw new NotificatorException(e.getMessage());
		}
	}

	private String getDate(String days) {
		int d = 1;
		try {
			d = Integer.parseInt(days);
		} catch (Exception e) {
			d = 1;
			logger.error("NumberFormatException: For input string: ${pushetta.cfg.days_available}");
		}

		Calendar today = GregorianCalendar.getInstance();
		today.add(Calendar.DATE, d);

		String date = today.get(Calendar.YEAR) + "-"
				+ (today.get(Calendar.MONTH) + 1) + "-"
				+ today.get(Calendar.DAY_OF_MONTH);

		return date;
	}

}
