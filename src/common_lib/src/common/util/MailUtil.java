package common.util;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {

	public static void main(String... args) {
		sendMail("smtp.gmail.com", 465, true, true, "no_reply@gamez.net.br", "1.414sqrt2", "misterioso_matematico@hotmail.com", "Test", "Test", "utf-8", "text/plain");
		// sendMail("pro-cheats.com", 143, true, false,
		// "no_reply@pro-cheats.com", "2.71828e",
		// "misterioso_matematico@hotmail.com", "Test", "Test", "utf-8",
		// "text/plain");
	}

	public static void sendMail(String host, int port, boolean useAuth, boolean useSSL, final String username, final String password, String destination, String title, String message, String charset,
			String contentType) {
		try {
			// smtp
			String from = username;
			String sport = Integer.toString(port);

			Properties p = new Properties();
			p.put("mail.smtp.host", host);
			p.put("mail.smtp.port", sport);
			p.put("mail.smtp.auth", useAuth ? "true" : "false");

			if (useSSL) {
				p.put("mail.smtp.socketFactory.port", sport);
				p.put("mail.smtp.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
				p.put("mail.smtp.socketFactory.fallback", "false");
			}

			p.put("mail.debug", "true");
			p.put("mail.smtp.debug", "true");
			p.put("mail.mime.charset", charset);

			Session session = useAuth ? Session.getInstance(p, new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			}) : Session.getInstance(p);

			MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
			mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
			mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
			mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
			mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
			mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
			CommandMap.setDefaultCommandMap(mc);

			MimeMessage msg = new MimeMessage(session);

			// "de" e "para"!!
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, new InternetAddress[] { new InternetAddress(destination) });

			// nao esqueça da data!
			// ou ira 31/12/1969 !!!
			msg.setSentDate(new Date());

			msg.setSubject(title, charset);

			// Criar parte da mensagem
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			// Texto da mensagem
			messageBodyPart.setText(message);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			msg.setContent(message, contentType);

			// Enviar parte da mensagem
			msg.setContent(multipart);

			msg.setContent(message, contentType);

			// enviando mensagem (tentando)
			Transport.send(msg);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void sendMail(String host, String destination, String title, String message) {
		sendMail(host, 465, false, true, null, null, destination, title, message, "ISO-8859-1", "text/html");
	}

	public static void sendMail(String host, String username, String password, String destination, String title, String message) {
		sendMail(host, 465, true, true, username, password, destination, title, message, "ISO-8859-1", "text/html");
	}

}
