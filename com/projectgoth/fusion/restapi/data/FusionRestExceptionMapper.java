package com.projectgoth.fusion.restapi.data;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FusionRestExceptionMapper implements ExceptionMapper<FusionRestException> {
   public Response toResponse(FusionRestException e) {
      return Response.ok().entity(e.getEntity()).build();
   }
}
