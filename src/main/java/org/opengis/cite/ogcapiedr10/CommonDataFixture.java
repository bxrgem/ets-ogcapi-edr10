package org.opengis.cite.ogcapiedr10;

import static org.opengis.cite.ogcapiedr10.SuiteAttribute.NO_OF_COLLECTIONS;
import static org.opengis.cite.ogcapiedr10.SuiteAttribute.REQUIREMENTCLASSES;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.OpenAPI;

//import com.reprezen.kaizen.oasparser.OpenApiParser;
//import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import org.opengis.cite.ogcapiedr10.conformance.RequirementClass;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CommonDataFixture extends CommonFixture {

	private static final int DEFAULT_NUMBER_OF_COLLECTIONS = 3;

	private OpenAPI apiModel = null;

	public URI modelUri = null;

	private List<RequirementClass> requirementClasses;

	protected int noOfCollections = DEFAULT_NUMBER_OF_COLLECTIONS;

	public OpenAPI getModel() {
		if (this.apiModel == null) {
			String msg = "apiModel is null in CommonDataFixture";
			System.out.println(msg);
			throw new NullPointerException(msg);
		}
		return this.apiModel;
	}

	@BeforeClass
	public void requirementClasses(ITestContext testContext) {
		this.requirementClasses = (List<RequirementClass>) testContext.getSuite()
				.getAttribute(REQUIREMENTCLASSES.getName());
	}

	@BeforeClass
	public void noOfCollections(ITestContext testContext) {
		Object noOfCollections = testContext.getSuite().getAttribute(NO_OF_COLLECTIONS.getName());
		if (noOfCollections != null) {
			this.noOfCollections = (Integer) noOfCollections;
		}
	}

	private URI appendFormatToURI(URI input) {
		URI modelUri = null;
		try {

			if (input.toString().contains("?")) {
				modelUri = new URI(input.toString() + "f=application/json");
			} else {
				modelUri = new URI(input.toString() + "?f=application/json");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return modelUri;
	}

	@BeforeClass
	public void retrieveApiModel(ITestContext testContext) {
		OpenAPIV3Parser parser = new OpenAPIV3Parser();
		SwaggerParseResult result;

		modelUri = (URI) testContext.getSuite().getAttribute(SuiteAttribute.API_DEFINITION.getName());
		modelUri = appendFormatToURI(modelUri);

		try {
			System.err.println("retrieveApiModel (1): '" + modelUri.toURL().toString() + "'");
			result = parser.readLocation(modelUri.toURL().toString(), null, null);

			if (result.getOpenAPI() == null || result.getMessages() == null || result.getMessages().size() > 0) {
				modelUri = new URI(modelUri.toString().replace("application/json", "json"));
				System.err.println("retrieveApiModel (2): '" + modelUri.toURL().toString() + "'");
				result = parser.readLocation(modelUri.toURL().toString(), null, null);
			}

			this.apiModel = result.getOpenAPI();
		} catch (Exception ed) {
			try {
				modelUri = new URI(modelUri.toString().replace("application/json", "json"));
				System.err.println("retrieveApiModel (3): '" + modelUri.toURL().toString() + "'");
				result = parser.readLocation(modelUri.toURL().toString(), null, null);
				this.apiModel = result.getOpenAPI();
			} catch (Exception ignored) {
			}
		}

	}

	protected List<String> createListOfMediaTypesToSupportForOtherResources(Map<String, Object> linkToSelf) {
		if (this.requirementClasses == null)
			throw new SkipException("No requirement classes described in  resource /conformance available");
		List<String> mediaTypesToSupport = new ArrayList<>();
		for (RequirementClass requirementClass : this.requirementClasses)
			if (requirementClass.hasMediaTypeForOtherResources())
				mediaTypesToSupport.add(requirementClass.getMediaTypeOtherResources());
		if (linkToSelf != null)
			mediaTypesToSupport.remove(linkToSelf.get("type"));
		return mediaTypesToSupport;
	}

	protected List<String> createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures() {
		if (this.requirementClasses == null)
			throw new SkipException("No requirement classes described in  resource /conformance available");
		List<String> mediaTypesToSupport = new ArrayList<>();
		for (RequirementClass requirementClass : this.requirementClasses)
			if (requirementClass.hasMediaTypeForFeaturesAndCollections())
				mediaTypesToSupport.add(requirementClass.getMediaTypeFeaturesAndCollections());
		return mediaTypesToSupport;
	}

	protected List<String> createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures(
			Map<String, Object> linkToSelf) {
		List<String> mediaTypesToSupport = createListOfMediaTypesToSupportForFeatureCollectionsAndFeatures();
		if (linkToSelf != null)
			mediaTypesToSupport.remove(linkToSelf.get("type"));
		return mediaTypesToSupport;
	}
}
