package com.example.myproject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.myproject.MailUtils.MAIL_PROTOCOLS;

@SuppressWarnings("serial")
public class MailHandlerServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MailHandlerServlet.class.getName());
	private long timeToWait = 90000;
	
	public void doPost(HttpServletRequest req,
				HttpServletResponse resp)
			throws IOException {
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String info = RegexpUtils.regexGetMatchGroup(req.getRequestURI(), "mail/([\\s\\S]+)", 1, false);
		String mess = "It is not possible to get message";
		String account = info.split("/")[0];
		String subject = info.split("/")[1];
		MailUtils mu = new MailUtils(account, timeToWait);
		subject = subject.replace("%20", " ");
		subject = subject.replaceAll("%.+","");
		String firstMessage = "";
		if(info.split("/").length == 3){
			String textInMessage = info.split("/")[2];
			textInMessage = textInMessage.replace("%20", " ");
			mess  = mu.getMessageContent(subject, textInMessage);
			if(mess != null && mess.contains("There were no mails")){
				firstMessage = mu.getAllWarnings();
				mu.closeStore();
				mu = new MailUtils(account, timeToWait);
				mess  = mu.getMessageContent(subject, textInMessage);
			}
		}else{
			mess  = mu.getMessageContent(subject);
			if(mess != null && mess.contains("There were no mails")){
				firstMessage = mu.getAllWarnings();
				mu.closeStore();
				mu = new MailUtils(account, timeToWait);
				mess  = mu.getMessageContent(subject);
			}
		}
		log.warning("do get");
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		if (mess != null) {
			resp.getWriter().println(firstMessage + mu.getAllWarnings() + mess);
		} else {
			resp.getWriter().println(firstMessage + mu.getAllWarnings() + "mail is null fff):");
		}
		mu.closeStore();
	}
}
