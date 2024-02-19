package org.opengis.cite.ogcapiedr10.openapi3;

import static org.opengis.cite.ogcapiedr10.openapi3.OpenApiUtils.PATH.COLLECTIONS;
import static org.opengis.cite.ogcapiedr10.openapi3.OpenApiUtils.PATH.CONFORMANCE;
import static org.opengis.cite.ogcapiedr10.util.URIUtils.appendFormatToURI;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//swagger imports
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;

import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.api.uri.UriTemplateParser;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class OpenApiUtils {

	// as described in
	// https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#fixed-fields
	private static final String DEFAULT_SERVER_URL = "/";

	private static String baseUri = null;

	synchronized public static void setBaseUri(OpenAPI openapi, URI iut) {
		if (baseUri != null) {
			return;
		}

		List<Server> servers = openapi.getServers();
		if (servers == null || servers.isEmpty()) {
			baseUri = "";
			return;
		}

		Server server = servers.get(0);
		String iutPath = iut.getPath();
		String serverPath = URI.create(server.getUrl()).getPath();

		baseUri = "";
		if (iutPath.startsWith(serverPath)) {
			baseUri = iutPath.substring(serverPath.length());
		}
	}

	synchronized public static String getBaseUri() {
		return baseUri;
	}

	@FunctionalInterface
	private interface PathMatcherFunction<A, B, C> {
		A apply(B b, C c);
	}

	public static List<Path> getPaths(OpenAPI openApi) {
		LinkedList<Path> paths = new LinkedList<>();
		for (Map.Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
			paths.add(new Path(entry.getKey(), entry.getValue()));
		}
		return paths;
	}

	public static Path getPath(OpenAPI openApi, String path) {
		Paths paths = openApi.getPaths();
		if (paths == null) {
			return null;
		}

		PathItem pathItem = paths.get(path);
		if (pathItem != null) {
			return new Path(path, pathItem);
		}
		return null;
	}

	enum PATH {
		CONFORMANCE("conformance"),
		COLLECTIONS("collections");

		private String pathItem;

		PATH(String pathItem) {
			this.pathItem = pathItem;
		}

		public String getPathItem() {
			String base = getBaseUri();
			if (base == null || base.isEmpty()) {
				return pathItem;
			}
			return base + "/" + pathItem;
		}

	}

	private static class PathMatcher implements PathMatcherFunction<Boolean, String, String> {
		private PathMatcher() {
		}

		@Override
		public Boolean apply(String pathUnderTest, String pathToMatch) {
			// System.err.println(" PathMatcher.apply: pathUnderTest = " + pathUnderTest +
			// ", pathToMatch = " + pathToMatch);
			UriTemplateParser parser = new UriTemplateParser(pathUnderTest);
			// System.err.println(" template: " + parser.getTemplate());
			Matcher matcher = parser.getPattern().matcher(pathToMatch);
			return matcher.matches();
		}
	}

	private static class ExactMatchFilter implements Predicate<TestPoint> {
		private final String requestedPath;

		ExactMatchFilter(String requestedPath) {
			this.requestedPath = requestedPath;
		}

		@Override
		public boolean test(TestPoint testPoint) {
			UriTemplate uriTemplate = new UriTemplate(testPoint.getPath());
			Map<String, String> templateReplacement = new HashMap<>(testPoint.getPredefinedTemplateReplacement());
			List<String> templateVariables = uriTemplate.getTemplateVariables();
			for (String templateVariable : templateVariables) {
				if (!templateReplacement.containsKey(templateVariable))
					templateReplacement.put(templateVariable, ".*");
			}
			String uri = uriTemplate.createURI(templateReplacement);
			Pattern pattern = Pattern.compile(uri);
			return pattern.matcher(requestedPath).matches();
		}
	}

	private OpenApiUtils() {
	}

	static public OpenAPI retrieveApiModel(URI modelUri) {
		OpenAPIV3Parser parser = new OpenAPIV3Parser();
		SwaggerParseResult result;
		OpenAPI apiModel = null;

		modelUri = appendFormatToURI(modelUri);

		try {

			result = parser.readLocation(modelUri.toURL().toString(), null, null);

			if (result.getOpenAPI() == null || result.getMessages() == null || result.getMessages().size() > 0) {
				modelUri = new URI(modelUri.toString().replace("application/json", "json"));

				if (result.getOpenAPI() == null) {
					System.err.println(" API Definition: parse error: '" + modelUri.toURL().toString() + "'");
					System.err.println(" API Definition: retry with 'Content-Type: json'" + modelUri.toURL().toString() + "'");
				} else {
					System.err.println(" API Definition: parse warnings: '" + modelUri.toURL().toString() + "'");
				}
				if (result.getMessages() != null) {
					for (String mesage : result.getMessages()) {
						System.err.println(" -- " + mesage);
					}
				}

				result = parser.readLocation(modelUri.toURL().toString(), null, null);
			}

			apiModel = result.getOpenAPI();

			if (apiModel == null) {
				System.err.println("API Definition: parse errors '" + modelUri.toURL().toString() + "'");
				if (result.getMessages() != null && result.getMessages().size() > 0) {
					for (String message : result.getMessages()) {
						System.err.println("  -- " + message);
					}
				}
			} else if (result.getMessages() != null && result.getMessages().size() > 0) {
				System.err.println("API Definition: parse warnings '" + modelUri.toURL().toString() + "'");
				for (String message : result.getMessages()) {
					System.err.println("  -- " + message);
				}
			}
		} catch (Exception ed) {
			try {
				modelUri = new URI(modelUri.toString().replace("application/json", "json"));
				// System.err.println("retrieveApiModel (3): '" + modelUri.toURL().toString() +
				// "'");
				result = parser.readLocation(modelUri.toURL().toString(), null, null);
				apiModel = result.getOpenAPI();
			} catch (Exception ignored) {
			}
		}
		return apiModel;
	}

	/**
	 * Parse all test points from the passed OpenAPI document as described in
	 * A.4.3. Identify the Test Points.
	 *
	 * @param apiModel never <code>null</code>
	 * @param iut      the url of the instance under test, never <code>null</code>
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	static List<TestPoint> retrieveTestPoints(OpenAPI apiModel, URI iut) {
		List<Path> pathItemObjects = identifyTestPoints(apiModel);
		List<PathItemAndServer> pathItemAndServers = identifyServerUrls(apiModel, iut, pathItemObjects);

		return processServerObjects(pathItemAndServers, true);
	}

	/**
	 * Parse the CONFORMANCE test points from the passed OpenAPI document as
	 * described in A.4.3. Identify the Test Points.
	 *
	 * @param apiModel never <code>null</code>
	 * @param iut      the url of the instance under test, never <code>null</code>
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	public static List<TestPoint> retrieveTestPointsForConformance(OpenAPI apiModel, URI iut) {
		return retrieveTestPoints(apiModel, iut, CONFORMANCE, false);
	}

	/**
	 * Parse the COLLECTIONS METADATA test points from the passed OpenAPI document
	 * as described in A.4.3. Identify the Test Points.
	 *
	 * @param apiModel never <code>null</code>
	 * @param iut      the url of the instance under test, never <code>null</code>
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	public static List<TestPoint> retrieveTestPointsForCollectionsMetadata(OpenAPI apiModel, URI iut) {
		// System.err.println(
		// " @@@@@@ retrieveTestPointsForCollectionsMetadata: iut:" + iut.toString() + "
		// PATH: " + COLLECTIONS.getPathItem());
		return retrieveTestPoints(apiModel, iut, COLLECTIONS, false);
	}

	/**
	 * Parse the COLLECTION METADATA test points for the passed collectionName
	 * including the extended path from the passed OpenAPI document as described in
	 * A.4.3. Identify the Test Points.
	 *
	 * @param apiModel       never <code>null</code>
	 * @param iut            the url of the instance under test, never
	 *                       <code>null</code>
	 * @param collectionName the extended path, may be <code>null</code>
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	public static List<TestPoint> retrieveTestPointsForCollectionMetadata(OpenAPI apiModel, URI iut,
			String collectionName) {
		StringBuilder requestedPath = new StringBuilder();
		requestedPath.append("/");
		requestedPath.append(COLLECTIONS.getPathItem());
		requestedPath.append("/");
		requestedPath.append(collectionName);

		List<TestPoint> testPoints = retrieveTestPoints(apiModel, iut, requestedPath.toString(), true);
		return testPoints.stream().filter(new ExactMatchFilter(requestedPath.toString())).collect(Collectors.toList());
	}

	/**
	 * Parse the COLLECTIONS test points from the passed OpenAPI document as
	 * described in A.4.3. Identify the Test Points.
	 *
	 * @param apiModel       never <code>null</code>
	 * @param iut            the url of the instance under test, never
	 *                       <code>null</code>
	 * @param noOfCollection the number of collections to return test points for (-1
	 *                       means the test points of all collections should be
	 *                       returned)
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	public static List<TestPoint> retrieveTestPointsForCollections(OpenAPI apiModel, URI iut, int noOfCollection) {

		String[] resources = { "locations", "position", "radius", "area", "trajectory" };

		List<TestPoint> allTestPoints = new ArrayList<TestPoint>();

		try {
			for (String res : resources) {
				StringBuilder requestedPath = new StringBuilder();
				requestedPath.append("/");
				requestedPath.append(COLLECTIONS.getPathItem());
				requestedPath.append("/.*/" + res);

				boolean r = allTestPoints
						.addAll(retrieveTestPoints(apiModel, iut, requestedPath.toString(), (a, b) -> a.matches(b), true));

			}
		} catch (Exception er) {

			er.printStackTrace();
		}

		if (noOfCollection < 0 || allTestPoints.size() <= noOfCollection) {
			return allTestPoints;
		}

		return allTestPoints.subList(0, noOfCollection);
	}

	/**
	 * Parse the test points with the passed path including the extended path from
	 * the passed OpenAPI document as described in A.4.3. Identify the Test Points.
	 *
	 * @param apiModel       never <code>null</code>
	 * @param iut            the url of the instance under test, never
	 *                       <code>null</code>
	 * @param collectionName the extended path, may be <code>null</code>
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	public static List<TestPoint> retrieveTestPointsForCollection(OpenAPI apiModel, URI iut, String collectionName) {
		String requestedPath = createCollectionPath(collectionName);

		List<TestPoint> testPoints = retrieveTestPoints(apiModel, iut, requestedPath, true);
		return testPoints.stream().filter(new ExactMatchFilter(requestedPath)).collect(Collectors.toList());
	}

	/**
	 * Parse the test points with the passed path including the extended path from
	 * the passed OpenAPI document as described in A.4.3. Identify the Test Points.
	 *
	 * @param apiModel       never <code>null</code>
	 * @param iut            the url of the instance under test, never
	 *                       <code>null</code>
	 * @param collectionName the extended path, may be <code>null</code>
	 * @param featureId      the id of the feature, never <code>null</code>
	 * @return the parsed test points, may be empty but never <code>null</code>
	 */
	public static List<TestPoint> retrieveTestPointsForFeature(OpenAPI apiModel, URI iut, String collectionName,
			String featureId) {
		StringBuilder requestedPath = new StringBuilder();
		requestedPath.append("/");
		requestedPath.append(COLLECTIONS.getPathItem());
		requestedPath.append("/");
		requestedPath.append(collectionName);
		requestedPath.append("/items/");
		requestedPath.append(featureId);

		List<TestPoint> testPoints = retrieveTestPoints(apiModel, iut, requestedPath.toString(), true);
		return testPoints.stream().filter(new ExactMatchFilter(requestedPath.toString())).collect(Collectors.toList());
	}

	public static Parameter retrieveParameterByName(String collectionItemPath, OpenAPI apiModel, String name) {
		Path path = getPath(apiModel, collectionItemPath);

		if (path == null) {
			return null;
		}

		List<Parameter> params = path.pathItem().getParameters();

		if (params != null) {
			for (Parameter parameter : params) {
				if (name.equals(parameter.getName())) {
					return parameter;
				}
			}
		}

		Operation get = path.pathItem().getGet();
		if (get == null) {
			return null;
		}

		params = get.getParameters();
		if (params != null) {
			for (Parameter parameter : params) {
				if (name.equals(parameter.getName())) {
					return parameter;
				}
			}
		}

		return null;
	}

	public static boolean isFreeFormParameterSupportedForCollection(OpenAPI apiModel, String collectionName) {
		String requestedPath = createCollectionPath(collectionName);

		List<Path> paths = identifyTestPoints(apiModel, requestedPath, new PathMatcher());
		for (Path path : paths) {
			Operation get = path.pathItem().getGet();
			if (get == null) {
				continue;
			}

			Collection<Parameter> parameters = get.getParameters();
			for (Parameter parameter : parameters) {
				// TODO: check if getAdditionalProperties() != null is enough to determine if
				// there is not defined additional properties.
				if (parameter.getSchema() != null && parameter.getSchema().getAdditionalProperties() != null) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isParameterSupportedForCollection(OpenAPI apiModel, String collectionName,
			String queryParam) {
		String requestedPath = createCollectionPath(collectionName);

		List<Path> paths = identifyTestPoints(apiModel, requestedPath, new PathMatcher());
		for (Path path : paths) {
			Operation get = path.pathItem().getGet();
			if (get == null) {
				continue;
			}

			Collection<Parameter> parameters = get.getParameters();
			for (Parameter parameter : parameters) {
				if (queryParam.equalsIgnoreCase(parameter.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	private static String createCollectionPath(String collectionName) {
		StringBuilder requestedPath = new StringBuilder();
		requestedPath.append("/");
		requestedPath.append(COLLECTIONS.getPathItem());
		requestedPath.append("/");
		requestedPath.append(collectionName);
		requestedPath.append("/items");
		return requestedPath.toString();
	}

	private static List<TestPoint> retrieveTestPoints(OpenAPI apiModel, URI iut, PATH path,
			boolean allowEmptyTemplateReplacements) {
		String requestedPath = path.getPathItem();
		return retrieveTestPoints(apiModel, iut, requestedPath, allowEmptyTemplateReplacements);
	}

	private static List<TestPoint> retrieveTestPoints(OpenAPI apiModel, URI iut, String requestedPath,
			boolean allowEmptyTemplateReplacements) {
		return retrieveTestPoints(apiModel, iut, requestedPath, new PathMatcher(), allowEmptyTemplateReplacements);
	}

	private static List<TestPoint> retrieveTestPoints(OpenAPI apiModel, URI iut, String requestedPath,
			PathMatcherFunction<Boolean, String, String> pathMatcher, boolean allowEmptyTemplateReplacements) {
		List<Path> pathItemObjects = identifyTestPoints(apiModel, requestedPath, pathMatcher);

		// System.err.println(" @@@@@ retrieveTestPoints (" + requestedPath + "): paths:
		// size: " + pathItemObjects.size());
		for (Path p : pathItemObjects) {
			System.err.println("       path: " + p.getPathString());
		}
		List<PathItemAndServer> pathItemAndServers = identifyServerUrls(apiModel, iut, pathItemObjects);
		return processServerObjects(pathItemAndServers, allowEmptyTemplateReplacements);
	}

	/**
	 * A.4.3.1. Identify Test Points:
	 *
	 * a) Purpose: To identify the test points associated with each Path in the
	 * OpenAPI document
	 *
	 * b) Pre-conditions:
	 *
	 * An OpenAPI document has been obtained
	 *
	 * A list of URLs for the servers to be included in the compliance test has been
	 * provided
	 *
	 * A list of the paths specified in the WFS 3.0 specification
	 *
	 * c) Method:
	 *
	 * FOR EACH paths property in the OpenAPI document If the path name is one of
	 * those specified in the WFS 3.0 specification Retrieve the Server URIs using
	 * A.4.3.2. FOR EACH Server URI Concatenate the Server URI with the path name to
	 * form a test point. Add that test point to the list.
	 *
	 * d) References: None
	 *
	 * @param apiModel never <code>null</code>
	 */
	private static List<Path> identifyTestPoints(OpenAPI apiModel) {
		List<Path> allTestPoints = new LinkedList<>();
		for (PATH path : PATH.values())
			allTestPoints.addAll(identifyTestPoints(apiModel, "/" + path.getPathItem(), new PathMatcher()));
		return allTestPoints;
	}

	private static List<Path> identifyTestPoints(OpenAPI apiModel, String path,
			PathMatcherFunction<Boolean, String, String> pathMatch) {
		List<Path> pathItems = new LinkedList<>();
		List<Path> paths = getPaths(apiModel);
		// System.err.println(" @@@@@ identifyTestPoints (" + path + "): paths: size: "
		// + paths.size());
		for (Path pathItem : paths) {
			// System.err.println(" pathItem: " + pathItem.getPathString());
			if (pathMatch.apply(pathItem.path(), path)) {
				// System.err.println(" pathItem (add): " + pathItem.getPathString());
				pathItems.add(pathItem);
			}
		}
		return pathItems;
	}

	/**
	 * A.4.3.2. Identify Server URIs:
	 *
	 * a) Purpose: To identify all server URIs applicable to an OpenAPI Operation
	 * Object
	 *
	 * b) Pre-conditions:
	 *
	 * Server Objects from the root level of the OpenAPI document have been obtained
	 *
	 * A Path Item Object has been retrieved
	 *
	 * An Operation Object has been retrieved
	 *
	 * The Operation Object is associated with the Path Item Object
	 *
	 * A list of URLs for the servers to be included in the compliance test has been
	 * provided
	 *
	 * c) Method:
	 *
	 * 1) Identify the Server Objects which are in-scope for this operationObject
	 *
	 * IF Server Objects are defined at the Operation level, then those and only
	 * those Server Objects apply to that Operation.
	 *
	 * IF Server Objects are defined at the Path Item level, then those and only
	 * those Server Objects apply to that Path Item.
	 *
	 * IF Server Objects are not defined at the Operation level, then the Server
	 * Objects defined for the parent Path Item apply to that Operation.
	 *
	 * IF Server Objects are not defined at the Path Item level, then the Server
	 * Objects defined for the root level apply to that Path.
	 *
	 * IF no Server Objects are defined at the root level, then the default server
	 * object is assumed as described in the OpenAPI specification.
	 *
	 * 2) Process each Server Object using A.4.3.3.
	 *
	 * 3) Delete any Server URI which does not reference a server on the list of
	 * servers to test.
	 *
	 * d) References: None
	 *
	 * @param apiModel        never <code>null</code>
	 * @param iut             never <code>null</code>
	 * @param pathItemObjects never <code>null</code>
	 */

	private static List<PathItemAndServer> identifyServerUrls(OpenAPI apiModel,
			URI iut, List<Path> pathItemObjects) {
		List<PathItemAndServer> pathItemAndServers = new ArrayList<>();

		for (Path pathItemObject : pathItemObjects) {
			for (Operation operationObject : pathItemObject.getOperations()) {
				List<String> serverUrls = identifyServerObjects(apiModel, pathItemObject,
						operationObject);
				for (String serverUrl : serverUrls) {
					if (DEFAULT_SERVER_URL.equalsIgnoreCase(serverUrl)) {
						serverUrl = iut.toString();
					} else if (serverUrl.startsWith("/")) {
						URI resolvedUri = iut.resolve(serverUrl);
						serverUrl = resolvedUri.toString();
					}
					PathItemAndServer pathItemAndServer = new PathItemAndServer(pathItemObject,
							operationObject,
							serverUrl);
					pathItemAndServers.add(pathItemAndServer);
				}
			}
		}
		return pathItemAndServers;
	}

	/**
	 * A.4.3.3. Process Server Object:
	 *
	 * a) Purpose: To expand the contents of a Server Object into a set of absolute
	 * URIs.
	 *
	 * b) Pre-conditions: A Server Object has been retrieved
	 *
	 * c) Method:
	 *
	 * Processing the Server Object results in a set of absolute URIs. This set
	 * contains all of the URIs that can be created given the URI template and
	 * variables defined in that Server Object.
	 *
	 * If there are no variables in the URI template, then add the URI to the return
	 * set.
	 *
	 * For each variable in the URI template which does not have an enumerated set
	 * of valid values:
	 *
	 * generate a URI using the default value,
	 *
	 * add this URI to the return set,
	 *
	 * flag this URI as non-exhaustive
	 *
	 * For each variable in the URI template which has an enumerated set of valid
	 * values:
	 *
	 * generate a URI for each value in the enumerated set,
	 *
	 * add each generated URI to the return set.
	 *
	 * Perform this processing in an iterative manner so that there is a unique URI
	 * for all possible combinations of enumerated and default values.
	 *
	 * Convert all relative URIs to absolute URIs by rooting them on the URI to the
	 * server hosting the OpenAPI document.
	 *
	 * d) References: None
	 *
	 * @param pathItemAndServers never <code>null</code>
	 */
	private static List<TestPoint> processServerObjects(List<PathItemAndServer> pathItemAndServers,
			boolean allowEmptyTemplateReplacements) {
		List<TestPoint> uris = new ArrayList<>();
		for (PathItemAndServer pathItemAndServer : pathItemAndServers) {
			processServerObject(uris, pathItemAndServer, allowEmptyTemplateReplacements);
		}
		return uris;
	}

	private static void processServerObject(List<TestPoint> uris, PathItemAndServer pathItemAndServer,
			boolean allowEmptyTemplateReplacements) {
		String pathString = pathItemAndServer.pathItemObject.getPathString();
		ApiResponse response = getResponse(pathItemAndServer);
		if (response == null)
			return;
		Map<String, MediaType> contentMediaTypes = response.getContent();

		UriTemplate uriTemplate = new UriTemplate(pathItemAndServer.serverUrl + pathString);
		if (uriTemplate.getNumberOfTemplateVariables() == 0) {
			TestPoint testPoint = new TestPoint(pathItemAndServer.serverUrl, pathString, contentMediaTypes);
			uris.add(testPoint);
		} else {
			List<Map<String, String>> templateReplacements = collectTemplateReplacements(pathItemAndServer,
					uriTemplate);

			if (templateReplacements.isEmpty() && allowEmptyTemplateReplacements) {
				TestPoint testPoint = new TestPoint(pathItemAndServer.serverUrl, pathString, contentMediaTypes);
				uris.add(testPoint);
			} else {
				for (Map<String, String> templateReplacement : templateReplacements) {
					TestPoint testPoint = new TestPoint(pathItemAndServer.serverUrl, pathString, templateReplacement,
							contentMediaTypes);
					uris.add(testPoint);
				}
			}
		}
	}

	private static ApiResponse getResponse(PathItemAndServer pathItemAndServer) {
		ApiResponse response = pathItemAndServer.hasResponse("200");
		if (response != null)
			return response;
		response = pathItemAndServer.hasResponse("default");
		if (response != null)
			return response;
		return null;
	}

	private static List<Map<String, String>> collectTemplateReplacements(PathItemAndServer pathItemAndServer,
			UriTemplate uriTemplate) {
		List<Map<String, String>> templateReplacements = new ArrayList<>();
		Collection<Parameter> parameters = pathItemAndServer.operationObject.getParameters();
		for (String templateVariable : uriTemplate.getTemplateVariables()) {
			for (Parameter parameter : parameters) {
				if (templateVariable.equals(parameter.getName())) {
					Schema schema = parameter.getSchema();
					if (schema.getEnum() != null) {
						addEnumTemplateValues(templateReplacements, templateVariable, schema);
					} else if (schema.getDefault() != null) {
						addDefaultTemplateValue(templateReplacements, templateVariable, schema);
					} else {
						// TODO: What should be done if the parameter does not have a default value and
						// no
						// enumerated set of valid values?
					}
				}
			}
		}
		return templateReplacements;
	}

	private static void addEnumTemplateValues(List<Map<String, String>> templateReplacements, String templateVariable,
			Schema schema) {
		Collection<Object> enums = schema.getEnum();
		if (enums.size() == 1) {
			for (Object enumValue : enums) {
				Map<String, String> replacement = new HashMap<>();
				replacement.put(templateVariable, enumValue.toString());
				templateReplacements.add(replacement);
			}
		} else {
			if (templateReplacements.isEmpty()) {
				Map<String, String> replacement = new HashMap<>();
				templateReplacements.add(replacement);
			}
			List<Map<String, String>> templateReplacementsToAdd = new ArrayList<>();
			for (Map<String, String> templateReplacement : templateReplacements) {
				for (Object enumValue : enums) {
					Map<String, String> newTemplateReplacement = new HashMap<>();
					newTemplateReplacement.putAll(templateReplacement);
					newTemplateReplacement.put(templateVariable, enumValue.toString());
					templateReplacementsToAdd.add(newTemplateReplacement);
				}
			}
			templateReplacements.clear();
			templateReplacements.addAll(templateReplacementsToAdd);
		}
	}

	private static void addDefaultTemplateValue(List<Map<String, String>> templateReplacements, String templateVariable,
			Schema schema) {
		if (templateReplacements.isEmpty()) {
			Map<String, String> replacement = new HashMap<>();
			templateReplacements.add(replacement);
		}
		for (Map<String, String> templateReplacement : templateReplacements) {
			templateReplacement.put(templateVariable, schema.getDefault().toString());
		}
	}

	private static List<String> identifyServerObjects(OpenAPI apiModel, Path pathItemObject,
			Operation operationObject) {
		List<Server> servers = operationObject.getServers();
		if (servers != null && !servers.isEmpty())
			return parseUrls(servers);

		servers = pathItemObject.pathItem().getServers();
		if (servers != null && !servers.isEmpty())
			return parseUrls(servers);

		servers = apiModel.getServers();
		if (servers != null && !servers.isEmpty())
			return parseUrls(servers);

		return Collections.singletonList(DEFAULT_SERVER_URL);
	}

	private static List<String> parseUrls(Collection<Server> servers) {
		List<String> urls = new ArrayList<>();
		for (Server server : servers)
			urls.add(server.getUrl());
		return urls;
	}

	private static class PathItemAndServer {
		private final Path pathItemObject;
		private Operation operationObject;

		// TODO: must be a server object to consider server variables
		private String serverUrl;

		ApiResponse hasResponse(String responseName) {
			return operationObject.getResponses().get(responseName);
		}

		private PathItemAndServer(Path pathItemObject, Operation operationObject, String serverUrl) {
			this.pathItemObject = pathItemObject;
			this.operationObject = operationObject;
			this.serverUrl = serverUrl;
		}

	}

}
