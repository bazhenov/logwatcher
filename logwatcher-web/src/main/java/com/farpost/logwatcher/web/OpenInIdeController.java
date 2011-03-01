package com.farpost.logwatcher.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;

/**
 * Контроллер обработки события "Открыть в IDE".
 * При запросе на этот урл, контроллер пытается записать в сокет(адрес запрашивающего, порт 8091)
 * значение параметра fileName, пришедший из запроса.
 *
 * Если у клиента стоит соответсвующий плагин, то происходит открытие заданного файла в IDE клиента.
 *
 * Контроллер возвращает только статус коды. В случае успешной записи в сокет вернется - Status_OK
 * В случае ошибки отрытия сокета или записи в него - вернется InternalErrorStatusCode (500)
 */
@Controller
public class OpenInIdeController {

	@RequestMapping("/openinide")
	public void openInIdea(@RequestParam String fileName, HttpServletRequest request,
												 HttpServletResponse response) {
		Socket socket = null;
		try {
			socket = new Socket(request.getRemoteAddr(), 8091);
			socket.getOutputStream().write(fileName.getBytes());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
			}
		}
	}
}
