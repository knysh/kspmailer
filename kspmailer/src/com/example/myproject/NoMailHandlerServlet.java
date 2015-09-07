package com.example.myproject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** сервлет ждет письма в течении 15 сек
 */
@SuppressWarnings("serial")
public class NoMailHandlerServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(NoMailHandlerServlet.class.getName());
	private long timeToWait = 15000;
	
	public void doPost(HttpServletRequest req,
				HttpServletResponse resp)
			throws IOException {
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String info = RegexpUtils.regexGetMatchGroup(req.getRequestURI(), "mailmissed/([\\s\\S]+)", 1, false);
		String mess = "It is not possible to get message";
		String account = info.split("/")[0];
		String subject = info.split("/")[1];
		MailUtils mu = new MailUtils(account, timeToWait);
		subject = subject.replace("%20", " ");
		String firstMessage = "";
		if(info.split("/").length == 3){
			String textInMessage = info.split("/")[2];
			textInMessage = textInMessage.replace("%20", " ");			
			mess  = Charset.forName("UTF-8").encode(mu.getMessageContent(subject, textInMessage)).toString();
		}else{
			mess  = mu.getMessageContent(subject);
		}
		log.warning("do get");
		resp.setContentType("text/html");
		if (mess != null) {
			resp.getWriter().println(firstMessage + mu.getAllWarnings() + mess);
		} else {
			resp.getWriter().println(firstMessage + mu.getAllWarnings() + "mail is null fff):");
		}
		mu.closeStore();
	}
}
