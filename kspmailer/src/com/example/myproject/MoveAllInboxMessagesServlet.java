package com.example.myproject;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MoveAllInboxMessagesServlet extends HttpServlet {
	private long timeToWait= 60000;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String account = RegexpUtils.regexGetMatchGroup(req.getRequestURI(), "move/([\\s\\S]+)", 1, false);
		MailUtils mu = new MailUtils(account, timeToWait);
		resp.getWriter().println(mu.moveAllMessages("INBOX", "ARCHIVE"));
		mu.closeStore();
	}
}
