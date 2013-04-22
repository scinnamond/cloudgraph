package org.cloudgraph.web.model.common;

import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudgraph.web.model.ModelBean;

public class EmailBean extends ModelBean {
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(EmailBean.class);

	private String subject;
	private String message;
	private String emailAddress;
	
	final String username = "scinnamond@gmail.com";
	final String password = "p3hoenix";
	
	private void clear() {
		this.subject = null;
		this.message = null;
		this.emailAddress = null;
	}
	
	public String cancel() {
		clear();
		return null;
	}
	
	public String send() {
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
	 
			Session session = Session.getInstance(props,
					  new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password);
						}
					  });
			 	 
			try {
	 
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(this.emailAddress));
				message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(username));
				message.setSubject(this.getSubject());
				
				StringBuilder buf = new StringBuilder(); 
				buf.append(this.getMessage());
				buf.append("\n");
				buf.append("FROM: ");
				buf.append(this.emailAddress);
				message.setText(buf.toString());
	 
				Transport.send(message);
				
				log.info("sent mail: " + subject);
				
				clear();
				
			} catch (MessagingException e) {
				log.error(e.getMessage(), e);
			}
		}
		catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		return null;
	}	
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
    public void validateSubject(FacesContext facesContext,
            UIComponent component, Object value) {
    	if (value == null || ((String)value).trim().length() == 0) {
            String msg = "Subject is a required field";
            throw new ValidatorException(
                		new FacesMessage(msg, msg));
    	}
    }
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
    public void validateMessage(FacesContext facesContext,
            UIComponent component, Object value) {
    	if (value == null || ((String)value).trim().length() == 0) {
            String msg = "Message is a required field";
            throw new ValidatorException(
                		new FacesMessage(msg, msg));
    	}
    }
	
    public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
    public void validateEmailAddress(FacesContext facesContext,
            UIComponent component, Object value) {
    	if (value == null || ((String)value).trim().length() == 0) {
            String msg = "Email Address is a required field";
            throw new ValidatorException(
                		new FacesMessage(msg, msg));
    	}
    }
	
	
}
