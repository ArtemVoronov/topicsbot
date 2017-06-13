package com.topicsbot.rest

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

/**
 * Обработчик exception-ов, которые произошли при вызове REST-сервисов и не были корректно отловлены
 */
@CompileStatic
class DefaultExceptionMapper implements ExceptionMapper<Exception> {

  @Override
  Response toResponse(Exception e) {
    final int sc = e instanceof WebApplicationException ? (e as WebApplicationException).response.status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR

    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw, true));

    Response.status(sc)
        .entity(sw.toString())
        .type(MediaType.TEXT_PLAIN_TYPE)
        .build()
  }

}