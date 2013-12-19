package org.cloudgraph.web.model.common;

import java.util.Iterator;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
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

@ManagedBean(name="EmailBean")
@SessionScoped
public class EmailBean extends ModelBean {
	private static final long serialVersionUID = 1L;

	private static Log log =LogFactory.getLog(EmailBean.class);

	private String subject;
	private String message;
	private String emailAddress;
	
	// credentials for 1and1 cloudgraph.org
	final String username = "scott-cinnamond@cloudgraph.org";
	final String password = "p1hoenix";
	
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
			
			//gmail
			//props.put("mail.smtp.auth", "true");
			//props.put("mail.smtp.starttls.enable", "true");
			//props.put("mail.smtp.host", "smtp.gmail.com");
			//props.put("mail.smtp.port", "587");
			
			// 1and1 IMAP over POP3
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.1and1.com");
			props.put("mail.smtp.port", "587");

			//props.put("mail.store.protocol", "pop3s");
			//props.put("mail.pop3.host", "imap.1and1.com");
			//props.put("mail.pop3.port", "143");
			
			log.info("username: " + this.username);
			Iterator iter = props.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String)iter.next();
				String value = (String)props.get(key);
				log.info(key + ": " + value);				
			}
	 
			Session session = Session.getInstance(props,
					  new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password);
						}
					  });
			 	 
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
				
		} catch (com.sun.mail.smtp.SMTPSendFailedException e) {
			log.error(e.getMessage(), e);
			if (e.getMessage() != null && e.getMessage().contains("Address syntax")) {
 	            FacesMessage msg = new FacesMessage(e.getMessage());  	       
	            FacesContext.getCurrentInstance().addMessage(null, msg);          			
			}
			else {
	 	        FacesMessage msg = new FacesMessage("Internal Error");  	       
		        FacesContext.getCurrentInstance().addMessage(null, msg);          	
			}
		} catch (MessagingException e) {
			log.error(e.getMessage(), e);
 	        FacesMessage msg = new FacesMessage("Internal Error");  	       
	        FacesContext.getCurrentInstance().addMessage(null, msg);          	
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
    	if (!((String)value).contains("@")) {
            String msg = "Invalid (From) Email Eddress";
            throw new ValidatorException(
                		new FacesMessage(msg, msg));
    	}
    }
	
	
}
