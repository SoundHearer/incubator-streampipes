package de.fzi.cep.sepa.rest.impl;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.fzi.cep.sepa.model.impl.graph.SecInvocation;
import de.fzi.cep.sepa.model.impl.graph.SepaInvocation;
import de.fzi.cep.sepa.rest.annotation.GsonWithIds;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import de.fzi.cep.sepa.messages.Notification;
import de.fzi.cep.sepa.messages.NotificationType;
import de.fzi.cep.sepa.messages.Notifications;
import de.fzi.cep.sepa.model.impl.graph.SepDescription;
import de.fzi.cep.sepa.rest.api.IPipelineElement;
import de.fzi.cep.sepa.storage.filter.Filter;

@Path("/v2/users/{username}/sources")
public class SemanticEventProducer extends AbstractRestInterface implements IPipelineElement {

	@Path("/{sourceId}/streams")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	public Response getStreamsBySource(@PathParam("username") String username, @PathParam("sourceId") String sourceId)
	{
		try {
			return ok(new SepDescription(requestor.getSEPById(sourceId)));
		} catch (URISyntaxException e) {
			return constructErrorMessage(new Notification(NotificationType.URIOFFLINE.title(), NotificationType.URIOFFLINE.description(), e.getMessage()));
		} catch (Exception e)
		{
			return constructErrorMessage(new Notification(NotificationType.UNKNOWN_ERROR.title(), NotificationType.UNKNOWN_ERROR.description(), e.getMessage()));
		}
		
	}
	
	@GET
	@Path("/available")
	@RequiresAuthentication
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response getAvailable(@PathParam("username") String username) {
		List<SepDescription> seps = Filter.byUri(requestor.getAllSEPs(), userService.getAvailableSourceUris(username));
		return ok(seps);
	}
	
	@GET
	@Path("/favorites")
	@RequiresAuthentication
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response getFavorites(@PathParam("username") String username) {
		List<SepDescription> seps = Filter.byUri(requestor.getAllSEPs(), userService.getFavoriteSourceUris(username));
		return ok(seps);
	}

	@GET
	@Path("/own")
	@RequiresAuthentication
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response getOwn(@PathParam("username") String username) {
		List<SepDescription> seps = Filter.byUri(requestor.getAllSEPs(), userService.getOwnSourceUris(username));
		List<SepDescription> si = seps.stream().map(s -> new SepDescription(s)).collect(Collectors.toList());

		return ok(si);
	}

	@POST
	@Path("/favorites")
	@RequiresAuthentication
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response addFavorite(@PathParam("username") String username, @FormParam("uri") String elementUri) {
		userService.addSourceAsFavorite(username, decode(elementUri));
		return statusMessage(Notifications.success(NotificationType.OPERATION_SUCCESS));
	}

	@DELETE
	@Path("/favorites/{elementUri}")
	@RequiresAuthentication
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response removeFavorite(@PathParam("username") String username, @PathParam("elementUri") String elementUri) {
		userService.removeSourceFromFavorites(username, decode(elementUri));
		return statusMessage(Notifications.success(NotificationType.OPERATION_SUCCESS));
	}
	
	@DELETE
	@Path("/own/{elementUri}")
	@RequiresAuthentication
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response removeOwn(@PathParam("username") String username, @PathParam("elementUri") String elementUri) {
		try {
			userService.deleteOwnSource(username, elementUri);
			requestor.deleteSEC(requestor.getSECById(elementUri));
		} catch (URISyntaxException e) {
			return constructErrorMessage(new Notification(NotificationType.STORAGE_ERROR.title(), NotificationType.STORAGE_ERROR.description(), e.getMessage()));
		}
		return constructSuccessMessage(NotificationType.STORAGE_SUCCESS.uiNotification());
	}

	@Path("/{elementUri}/jsonld")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@GsonWithIds
	@Override
	public Response getAsJsonLd(@PathParam("elementUri") String elementUri) {
		try {
			return ok(toJsonLd(requestor.getSECById(elementUri)));
		} catch (URISyntaxException e) {
			return statusMessage(Notifications.error(NotificationType.UNKNOWN_ERROR));
		}
	}

	
	@Path("/{elementUri}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@GsonWithIds
	@Override
	public Response getElement(@PathParam("username") String username, @PathParam("elementUri") String elementUri) {
		// TODO Access rights
		try {
			return ok(requestor.getSEPById(elementUri));
		} catch (URISyntaxException e) {
			return statusMessage(Notifications.error(NotificationType.UNKNOWN_ERROR, e.getMessage()));
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println(new SemanticEventProducer().getOwn("riemer@fzi.de"));
	}

}
