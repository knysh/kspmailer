package com.example.myproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/** work with mail
 */
public class MailUtils {

	private String host, username, password;
	private Properties properties = new Properties();
	private MAIL_PROTOCOLS protocol;
	private long timeToWaitMail = 90000;
	private Store store;
	private List<String> warnMessages = new ArrayList<String>();
	
	
	public String getAllWarnings(){
		String warnings = "\r\n";
		for (String m : warnMessages) {
			warnings = warnings + m;
		}
		return warnings.replace("\r\n", "<br/>");
	}
	
	/** construct mail connector
	 * @param host host
	 * @param username username
	 * @param password password
	 * @param protocol protocol of mail server
	 */
	public MailUtils(String host, String username, String password, MAIL_PROTOCOLS protocol, long timeToWait){
		timeToWaitMail = timeToWait;
		this.host = host;
	    this.username = username;
	    this.password = password;
	    this.protocol = protocol;
	    store = connect();
	}

	public MailUtils(String account, long timeToWait){
		this(getHost(account), account, account.split("@")[0].replace(".", "") + "123", getProtocol(account), timeToWait);
	}
	
	/** returns server host name
	 * @param account user account
	 * @return server host name
	 */
	private static String getHost(String account){
		if(account.contains("mail.ru")){
			return "imap.mail.ru";
		}
		if(account.contains("inbox.ru")){
			return "pop.inbox.ru";
		}
		if(account.contains("qip.ru")){
			return "imap.qip.ru";
		}
		return "imap.gmail.com";
	}
	
	/** returns server host name
	 * @param account user account
	 * @return server host name
	 */
	private static MAIL_PROTOCOLS getProtocol(String account){
		if(account.contains("inbox.ru")){
			return MAIL_PROTOCOLS.POP3;
		}
		return MAIL_PROTOCOLS.IMAP;
	}

	/** construct mail connector
	 * @param host host
	 * @param username username
	 * @param password password
	 * @param protocol protocol of mail server
	 * @param properties properties
	 */
	public MailUtils(String host, String username, String password, MAIL_PROTOCOLS protocol, Properties properties, long timeToWait){
		this(host, username, password, protocol, timeToWait);
		this.properties = properties;
	}

	/** available protocols
	 */
	public enum MAIL_PROTOCOLS{
		POP3("pop3"), SMPT("smpt"), IMAP("imaps");

		private String protocol;

		/** constructor
		 * @param name mail protocol name
		 */
		MAIL_PROTOCOLS(String name){
			protocol = name;
		}

		@Override
		public String toString() {
			return protocol;
		}
	}

	/**
	 * @param folderName name of folder in mailbox
	 * @param permissions permissions for access to folder(user Folder.READ_ONLY and e.i.)
	 * @param folder folder
	 * @return messages
	 */
	public Message[] getMessage(String folderName, int permissions, Folder folder){
		// Get folder
		Message[] messages = null;
    	for (int i = 0; i < 2; i++) {
    		try {
    			folder.open(Folder.READ_ONLY);
    			messages = folder.getMessages();
    			break;
    		} catch (MessagingException e) {
    			warnMessages.add(getWarnMessage("Impossible to get messages", e));
    		}
		}
	    return messages;
	}

	/** Get link from the letter
	 * @param subject subject
	 * @return link
	 */
	public String getMessageContent(String subject){
		try {
			Multipart part = (Multipart) waitForLetter(subject).getContent();
			return (String) part.getBodyPart(0).getContent();
		} catch (IOException e) {
			warnMessages.add(getWarnMessage("It is impossible to get conetent of message", e));
		} catch (MessagingException e) {
			warnMessages.add(getWarnMessage("It is impossible to get conetent of message", e));
		} catch (NullPointerException e) {
			warnMessages.add(getWarnMessage(String.format("There were no mails for account activation or reset password in %1$s ms!", timeToWaitMail), e));
			return String.format(getCurrentDate() + "   There were no mails for account activation or reset password in %1$s ms!<br/> Error Message: %2$sr\nStackTrace:<br/>%3$s",timeToWaitMail, e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	/** Get link from the letter
	 * @param subject subject
	 * @param text text
	 * @return link
	 */
	public String getMessageContent(String subject, String text){
		try {
			Multipart part = (Multipart) waitForLetter(subject, text).getContent();
			return (String) part.getBodyPart(0).getContent();			
		} catch (IOException e) {
			warnMessages.add(getWarnMessage("It is impossible to get conetent of message", e));
		} catch (MessagingException e) {
			warnMessages.add(getWarnMessage(String.format("There were no mails for account activation or reset password in %1$s ms!", timeToWaitMail), e));
			return getCurrentDate() + String.format("   There were no mails for account activation or reset password in %1$s ms!<br/> Error Message: %2$sr\nStackTrace:<br/>%3$s",timeToWaitMail, e.getMessage(), e.getStackTrace());
		} catch (NullPointerException e) {
			warnMessages.add(getWarnMessage(String.format("There were no mails for account activation or reset password in %1$s ms!", timeToWaitMail), e));
			return String.format(getCurrentDate() + "   There were no mails for account activation or reset password in %1$s ms!<br/> Error Message: %2$sr\nStackTrace:<br/>%3$s",timeToWaitMail, e.getMessage(), e.getStackTrace());
		}
		return null;
	}

	/** wait for letter with necessary subject is present in mailbox
	 * @param subject subject of letter
	 * @return message
	 * @throws MessagingException MessagingException
	 */
	private Message waitForLetter(String subject) throws MessagingException{
		Message[] messages = null;
		// waiting
		long start = System.currentTimeMillis();
		do{
			Folder folder = store.getFolder("INBOX");
			messages = getMessage(folder);
			for (Message m : messages) {
				try {
					if(m.getSubject().contains(subject)){
						return m;
					}
				} catch (MessagingException e) {
					warnMessages.add(getWarnMessage("Cannot get message", e));
				}
			}
			try {
				folder.close(false);
			} catch (Exception e) {
				warnMessages.add(getWarnMessage("Cannot get message", e));
			}
		}while((start + timeToWaitMail) >= System.currentTimeMillis());
		warnMessages.add(getWarnMessage(String.format("Mailbox not contains letter with subject '%1$s'. There was waiting: %2$s mills", subject, timeToWaitMail)));
		return null;
	}
	
	/** create warn message in nessessary format
	 * @param message message
	 * @param exception exception
	 * @return warn message in nessessary format
	 */
	private String getWarnMessage(String message, Exception exception){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		String m = String.format("%1$s   %2$s<br/>Error message: %3$s<br/>StackTrace: %4$s<br/>", getCurrentDate("dd.MM.yyyy HH:mm:ss"), message, exception.getMessage(), sw.toString());
		System.out.println(m);
		return m;
	}
	
	/**
	 * get current date in the "dd.MM.yyyy" pattern
	 */
	public static String getCurrentDate() {
		return getCurrentDate("dd.MM.yyyy HH:mm:ss");
	}

	/**
	 * get current date in the custom pattern
	 */
	public static String getCurrentDate(String pattern) {
		return formatDate(new Date(), pattern);
	}
	
	/**
	 * Format date to string using custom pattern
	 * @param date - date to be formatted
	 * @param pattern - custom pattern of the date
	 */
	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		formatter.setTimeZone(TimeZone.getTimeZone("Etc/GMT-4"));
		return formatter.format(date);
	}
	
	
	/** create warn message in nessessary format
	 * @param message message
	 * @return warn message in nessessary format
	 */
	private String getWarnMessage(String message){
		String m = String.format("%1$s   %2$s<br/>", getCurrentDate("dd.MM.yyyy HH:mm:ss"), message);
		System.out.println(m);
		return m;
	}

	/** wait for letter with necessary subject and address is present in mailbox
	 * @param subject subject of letter
	 * @param text text that message contains
	 * @return message
	 * @throws MessagingException MessagingException
	 */
	private Message waitForLetter(String subject, String text) throws MessagingException{
		Message[] messages = null;
		// waiting
		long start = System.currentTimeMillis();
		do{
			Folder folder = store.getFolder("INBOX");
			messages = getMessage(folder);
			for (Message m : messages) {
				try {
					String content = (String) ((Multipart) m.getContent()).getBodyPart(0).getContent();
					if(m.getSubject().contains(subject) && content.contains(text)){
						return m;
					}
				} catch (Exception e) {
					warnMessages.add(getWarnMessage("It is impossible to get subject of message", e));
				}
			}
			try {
				folder.close(false);
			} catch (Exception e) {
				warnMessages.add(getWarnMessage("Cannot close folder", e));
			}
		}while((start + timeToWaitMail) >= System.currentTimeMillis());
		warnMessages.add(String.format("Mailbox not contains letter with subject '%1$s'. There was waiting: %2$s mills", subject, timeToWaitMail));
		return null;
	}

	/** by default folder "INBOX" and permissions Folder.READ_ONLY
	 * @param folder folder
	 * @return messages
	 */
	public Message[] getMessage(Folder folder){
		return getMessage("INBOX", Folder.READ_ONLY, folder);
	}

	/** connect to mailbox
	 * @return Store
	 */
	private Store connect(){
		for(int i = 0; i <= 10; i++){
			// Get session
			properties.setProperty("mail.store.protocol", "imaps");
			properties.setProperty("mail.imap.ssl.enable", "true");
			properties.put("mail.imap.port", "993");
			properties.put("mail.imap.starttls.enable","true");
			properties.put("mail.imap.socketFactory.port", 993);
			properties.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.imap.socketFactory.fallback", "false");
			
			if(getHost(username).contains("inbox.ru")){
				properties.setProperty("mail.store.protocol", "pop3");
				properties.setProperty("mail.pop3.ssl.enable", "true");
				properties.put("mail.pop3.port", "995");
				properties.put("mail.pop3.starttls.enable","true");
				properties.put("mail.pop3.socketFactory.port", 995);
				properties.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				properties.put("mail.pop3.socketFactory.fallback", "false");
			}
			Session session = Session.getDefaultInstance(properties, null);
		    // Get the store
		    try {
		    	warnMessages.add(getWarnMessage("Connect to mailbox"));
		    	store = session.getStore(protocol.toString());
		    	store.connect(host, username, password);
		    	break;
		    } catch (NoSuchProviderException e) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
		    	e.printStackTrace();
			} catch (MessagingException e) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return store;
	}

	/**
	 * перемещает все сообщения из папки одной папки в другую
	 * @param fromFolderName
	 * @param toFolderName
	 * @return
	 */
	public String moveAllMessages(String fromFolderName, String toFolderName){
		//Проверка, что соединение установлено
		try{
			Folder defaultFolder = store.getDefaultFolder();
			Folder toFolder = defaultFolder.getFolder(toFolderName);
			if(!toFolder.exists()){
				toFolder.create(Folder.READ_WRITE);
			}	
			Folder fromFolder = store.getFolder(fromFolderName);
			fromFolder.open(Folder.READ_WRITE);	
			int messageCount = fromFolder.getMessageCount();
			if(messageCount == 0){
				return String.format("%1$s is empty", fromFolderName);
			}
			//Получаем все сообщения из папки
			Message[] messages = fromFolder.getMessages(1, messageCount);
			fromFolder.copyMessages(messages, toFolder);
			fromFolder.close(true);
			//удаляем все мессаги из fromFolderName
			deleteAllMessages(fromFolderName);			
			return String.format("All messages were moved to %1$s form to %2$s successfull ", toFolderName, fromFolderName);
		}catch(MessagingException e){
			return "Messaging exception: " + e.getMessage();
		}
	}
	
	/** Удаляет все сообщения с ящика
	 * @param folderName название папки с письмами(например "INBOX")
	 */
	public String deleteAllMessages(String folderName){
		//Проверка, что соединение установлено
		try{
			Folder inbox = store.getFolder(folderName);
			inbox.open(Folder.READ_WRITE);
			//Получаем все сообщения из папки
			Message[] messages = inbox.getMessages();
			for(Message message:messages) {
				message.setFlag(Flags.Flag.DELETED, true);
			}
			inbox.close(true);
			return "All messages were deleted successfull from " + username;
		}catch(MessagingException e){
			return "Messaging exception: " + e.getMessage();
		}
	}
	
	/** Удаляет все сообщения с ящика
	 * @param folderName название папки с письмами(например "INBOX")
	 */
	public String deleteMessages(String folderName, String subject, String textInMessage){
		//Проверка, что соединение установлено
		try{
			Folder inbox = store.getFolder(folderName);
			inbox.open(Folder.READ_WRITE);
			//Получаем все сообщения из папки
			Message[] messages = inbox.getMessages();
			for(Message message:messages) {
				String content = "";
				try {
					content = (String) ((Multipart) message.getContent()).getBodyPart(0).getContent();
				} catch (IOException e) {
					content = "";
				}
				if(message.getSubject().contains(subject) && content.contains(textInMessage)){
					message.setFlag(Flags.Flag.DELETED, true);
				}
			}
			inbox.close(true);
			return "All messages were deleted successfull from " + username;
		}catch(MessagingException e){
			warnMessages.add(getWarnMessage("Cannot delete messages", e));
			return getCurrentDate() + "Messaging exception: " + e.getMessage();
		}
	}
	

	/** close store
	 */
	public void closeStore(){
		try {
			store.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
