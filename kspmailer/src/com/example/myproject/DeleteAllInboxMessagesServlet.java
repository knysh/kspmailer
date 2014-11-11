package com.example.myproject;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.myproject.MailUtils.MAIL_PROTOCOLS;

@SuppressWarnings("serial")
public class DeleteAllInboxMessagesServlet extends HttpServlet {
	private long timeToWait= 60000;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String url = RegexpUtils.regexGetMatchGroup(req.getRequestURI(), "delete/([\\s\\S]+)", 1, false);
		if(url.split("/").length == 3){
			String account = url.split("/")[0];
			String subject = url.split("/")[1].replace("%20", " ");
			String textInMessage = url.split("/")[2].replace("%20", " ");
			MailUtils mu = new MailUtils(account, timeToWait);
			resp.getWriter().println(mu.deleteMessages("INBOX", subject, textInMessage));
			mu.closeStore();
		}else{
			String account = url;
			MailUtils mu = new MailUtils(account, timeToWait);
			//MailUtils mu = new MailUtils("imap." + account.split("@")[1], account, account.split("@")[0].replace(".", "") + "123", MAIL_PROTOCOLS.IMAP);
			resp.getWriter().println(mu.deleteAllMessages("INBOX"));
			mu.closeStore();
		}
	}
}
