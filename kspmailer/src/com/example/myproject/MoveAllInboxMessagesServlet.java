package com.example.myproject;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.myproject.MailUtils.MAIL_PROTOCOLS;

@SuppressWarnings("serial")
public class MoveAllInboxMessagesServlet extends HttpServlet {
	
	private long timeToWait= 60000; 
	private final static String imapHost = "imap.mail.ru";
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String account = RegexpUtils.regexGetMatchGroup(req.getRequestURI(), "move/([\\s\\S]+)", 1, false);
		MailUtils mu = new MailUtils(imapHost, account, MailUtils.getPassword(account), MAIL_PROTOCOLS.IMAP, timeToWait);
		resp.getWriter().println(mu.moveAllMessages("INBOX", "ARCHIVE"));
		mu.closeStore();
	}
}
