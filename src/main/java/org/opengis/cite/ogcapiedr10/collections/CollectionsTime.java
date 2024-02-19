package org.opengis.cite.ogcapiedr10.collections;
import java.util.List;

import static org.opengis.cite.ogcapiedr10.EtsAssert.assertFalse;
import static org.opengis.cite.ogcapiedr10.EtsAssert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.opengis.cite.ogcapiedr10.openapi3.TestPoint;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;;

/**
 * /collections/{collectionId}/
 *
 */
public class CollectionsTime {

	protected boolean isRequired(Parameter param) {
		return param.getRequired() != null && param.getRequired();
	}

	protected Boolean isExplode(Parameter param) {
		return param.getExplode() != null && param.getExplode();
	}

	private Parameter findParameter(Paths paths, String paramName, TestPoint testPoint) {
		//System.err.println("findParameter ("+paramName+"): " + testPoint.getPath());
		Parameter foundParam = null;
		for (String path : paths.keySet()) {
			if (path.contains("collections") && path.endsWith(testPoint.getPath())) {
				PathItem pathItem = paths.get(path);
				for (Operation op : pathItem.readOperations()) {
					List<Parameter> parameters = op.getParameters();
			
					if( parameters==null){
						continue;
					}
					
					for (Parameter param : parameters) {
						if (hasName(param)) {
							if (param.getName().equals(paramName)) {
								foundParam = param;
							}
						}
					}
				}
			}
		}
		return foundParam;
	}

	/**
	 * <pre>
	 * Abstract Test 38: Validate that the coords query parameters are constructed correctly. (position)
	 * Abstract Test 54: Validate that the coords query parameters are constructed correctly. (area)
	 * Abstract Test 70: Validate that the coords query parameters are constructed correctly. (cube)
	 * Abstract Test 92: Validate that the coords query parameters are constructed correctly. (trajectory)
	 * Abstract Test 116: Validate that the coords query parameters are constructed correctly. (corridor)
	 * </pre>
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */

	public void coordsParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "coords";
		Parameter coords = findParameter(model.getPaths(), paramName, testPoint);

		if (!testPoint.getPath().endsWith("/locations")) {
			assertNotNull(coords, "Required " + paramName + " parameter for collections with path '"
					+ testPoint.getPath() + "'  in OpenAPI document is missing");

			if (coords != null) {
				String msg = "Expected property '%s' with value '%s' but was '%s'";
				assertEquals(coords.getName(), paramName, String.format(msg, "name", paramName, coords.getName()));
				assertEquals(coords.getIn(), "query", String.format(msg, "in", "query", coords.getIn()));
				assertTrue(isRequired(coords), String.format(msg, "required", "true", coords.getRequired()));
				// assertEquals( coords.getStyle(), "form", String.format( msg, "style","form",
				// coords.getStyle() ) ); //TODO SHOULD BE Enabled
				assertFalse(isExplode(coords), String.format(msg, "explode", "false", coords.getExplode()));
				Schema schema = coords.getSchema();
				assertEquals(schema.getType(), "string",
						String.format(msg, "schema -> type", "string", schema.getType()));
			}
		}
	}

	/**
	 * <pre>
	 * Abstract Test 42: Validate that the dateTime query parameters are constructed correctly. (position)
	 * Abstract Test 58: Validate that the dateTime query parameters are constructed correctly. (area)
	 * Abstract Test 74: Validate that the dateTime query parameters are constructed correctly. (cube)
	 * Abstract Test 139: Validate that the dateTime query parameters are constructed correctly. (instances)
	 * </pre>
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void dateTimeParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "datetime";
		Parameter datetime = findParameter(model.getPaths(), paramName, testPoint);

		if (datetime != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";

			assertEquals(datetime.getName(), paramName, String.format(msg, "name", paramName, datetime.getName()));
			assertEquals(datetime.getIn(), "query", String.format(msg, "in", "query", datetime.getIn()));
			assertFalse(isRequired(datetime), String.format(msg, "required", "false", datetime.getRequired()));
			assertEquals(datetime.getStyle(), "form", String.format(msg, "style", "form", datetime.getStyle()));
			assertFalse(isExplode(datetime), String.format(msg, "explode", "false", datetime.getExplode()));
		}
	}

	public boolean hasName(Parameter parameter) {
		try {
			 // we do this to check whether there is a name
			if( parameter.getName() != null) {
				return true;
			}
		} catch (Exception ee) {
		}

		return false;
	}

	/**
	 * Abstract Test 44: Validate that the parameter-name query parameters are
	 * processed correctly. (position)
	 * Abstract Test 60: Validate that the parameter-name query parameters are
	 * processed correctly. (area)
	 * Abstract Test 76: Validate that the parameter-name query parameters are
	 * processed correctly. (cube)
	 * Abstract Test 94: Validate that the parameter-name query parameters are
	 * processed correctly. (trajectory)
	 * Abstract Test 126: Validate that the parameter-name query parameters are
	 * processed correctly. (corridor)
	 * Abstract Test 141: Validate that the parameter-name query parameters are
	 * processed correctly. (locations)
	 * 
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */

	public void parameternameParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "parameter-name";
		Parameter parametername = findParameter(model.getPaths(), paramName, testPoint);

		if (parametername != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";

			assertNotNull(parametername, "Required " + paramName + " parameter for collections with path '"
					+ testPoint.getPath() + "'  in OpenAPI document is missing");
			assertEquals(parametername.getName(), paramName,
					String.format(msg, "name", paramName, parametername.getName()));
			assertEquals(parametername.getIn(), "query", String.format(msg, "in", "query", parametername.getIn()));
			assertFalse(isRequired(parametername),
					String.format(msg, "required", "false", parametername.getRequired()));
		}
	}

	/**
	 * Abstract Test 46: Validate that the crs query parameters are constructed
	 * correctly. (position)
	 * Abstract Test 62: Validate that the crs query parameters are constructed
	 * correctly. (area)
	 * Abstract Test 78: Validate that the crs query parameters are constructed
	 * correctly. (cube)
	 * Abstract Test 96: Validate that the crs query parameters are constructed
	 * correctly. (trajectory)
	 * Abstract Test 128: Validate that the crs query parameters are constructed
	 * correctly. (corridor)
	 * Abstract Test 143: Validate that the crs query parameters are constructed
	 * correctly. (locations)
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */

	public void crsParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "crs";
		try {
		Parameter crs = findParameter(model.getPaths(), paramName, testPoint);
		

		if (crs != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";

			assertEquals(crs.getName(), paramName, String.format(msg, "name", paramName, crs.getName()));
			assertEquals(crs.getIn(), "query", String.format(msg, "in", "query", crs.getIn()));
			assertFalse(isRequired(crs), String.format(msg, "required", "false", crs.getRequired()));
			assertEquals(crs.getStyle(), "form", String.format(msg, "style", "form", crs.getStyle()));
			assertFalse(isExplode(crs), String.format(msg, "explode", "false", crs.getExplode()));
		}
		} catch (Exception e) {
			System.err.println("crsParameterDefinition: " + e.getMessage());
      e.printStackTrace();
    }
	}

	/**
	 * Abstract Test 48: Validate that the f query parameter is constructed
	 * correctly. (position)
	 * Abstract Test 64: Validate that the f query parameter is constructed
	 * correctly. (area)
	 * Abstract Test 80: Validate that the f query parameter is constructed
	 * correctly. (cube)
	 * Abstract Test 98: Validate that the f query parameter is constructed
	 * correctly. (trajectory)
	 * Abstract Test 130: Validate that the f query parameter is constructed
	 * correctly. (corridor)
	 * Abstract Test 145: Validate that the f query parameter is constructed
	 * correctly. (locations)
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void fParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "f";
		Parameter f = findParameter(model.getPaths(), paramName, testPoint);

		if (f != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";

			assertEquals(f.getName(), paramName, String.format(msg, "name", paramName, f.getName()));
			assertEquals(f.getIn(), "query", String.format(msg, "in", "query", f.getIn()));
			assertFalse(isRequired(f), String.format(msg, "required", "false", f.getRequired()));
			assertEquals(f.getStyle(), "form", String.format(msg, "style", "form", f.getStyle()));
			assertFalse(isExplode(f), String.format(msg, "explode", "false", f.getExplode()));
		}
	}

	/**
	 * Abstract Test 40 (/conf/edr/rc-z-definition): Validate that the vertical
	 * level query parameters are constructed correctly. (position)
	 * Abstract Test 56 (/conf/edr/rc-z-definition): Validate that the vertical
	 * level query parameters are constructed correctly. (area)
	 *
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void zParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "z";
		Parameter z = findParameter(model.getPaths(), paramName, testPoint);

		if (z != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";
			assertEquals(z.getName(), paramName, String.format(msg, "name", paramName, z.getName()));
			assertEquals(z.getIn(), "query", String.format(msg, "in", "query", z.getIn()));
			assertTrue(isRequired(z), String.format(msg, "required", "true", z.getRequired()));
			assertEquals(z.getStyle(), "form", String.format(msg, "style", "form", z.getStyle()));
			assertFalse(isExplode(z), String.format(msg, "explode", "false", z.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Requirement A.21: /req/edr/within-definition Parameter within definition
	 * </pre>
	 * 
	 * NOTE: Not referenced by ATS
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void withinParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "within";
		Parameter within = findParameter(model.getPaths(), paramName, testPoint);

		if (within != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";
			assertEquals(within.getName(), paramName, String.format(msg, "name", paramName, within.getName()));
			assertEquals(within.getIn(), "query", String.format(msg, "in", "query", within.getIn()));
			assertFalse(isRequired(within), String.format(msg, "required", "false", within.getRequired()));
			assertEquals(within.getStyle(), "form", String.format(msg, "style", "form", within.getStyle()));
			assertFalse(isExplode(within), String.format(msg, "explode", "false", within.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Requirement A.23: /req/edr/within-units-definition Parameter withinUnits
	definition
	 * </pre>
	 * 
	 * NOTE: Not referenced by ATS
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void withinUnitsParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "within-units";
		Parameter withinUnits = findParameter(model.getPaths(), paramName, testPoint);

		if (withinUnits != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";
			assertEquals(withinUnits.getName(), paramName, String.format(msg, "name", paramName, withinUnits.getName()));
			assertEquals(withinUnits.getIn(), "query", String.format(msg, "in", "query", withinUnits.getIn()));
			assertFalse(isRequired(withinUnits), String.format(msg, "required", "false", withinUnits.getRequired()));
			assertEquals(withinUnits.getStyle(), "form", String.format(msg, "style", "form", withinUnits.getStyle()));
			assertFalse(isExplode(withinUnits), String.format(msg, "explode", "false", withinUnits.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Requirement A.25: /req/edr/resolution-x-definition Parameter resolution-x
	definition
	 * </pre>
	 * 
	 * NOTE: Not referenced by ATS
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void resolutionxParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "resolution-x";
		Parameter resolutionx = findParameter(model.getPaths(), paramName, testPoint);

		if (resolutionx != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";
			assertEquals(resolutionx.getName(), paramName, String.format(msg, "name", paramName, resolutionx.getName()));
			assertEquals(resolutionx.getIn(), "query", String.format(msg, "in", "query", resolutionx.getIn()));
			assertFalse(isRequired(resolutionx), String.format(msg, "required", "false", resolutionx.getRequired()));
			assertEquals(resolutionx.getStyle(), "form", String.format(msg, "style", "form", resolutionx.getStyle()));
			assertFalse(isExplode(resolutionx), String.format(msg, "explode", "false", resolutionx.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Requirement A.28: /req/edr/resolution-y-definition Parameter resolution-y
	definition
	 * </pre>
	 * 
	 * NOTE: Not referenced by ATS
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void resolutionyParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "resolution-y";
		Parameter resolutiony = findParameter(model.getPaths(), paramName, testPoint);

		if (resolutiony != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";
			assertEquals(resolutiony.getName(), paramName, String.format(msg, "name", paramName, resolutiony.getName()));
			assertEquals(resolutiony.getIn(), "query", String.format(msg, "in", "query", resolutiony.getIn()));
			assertFalse(isRequired(resolutiony), String.format(msg, "required", "false", resolutiony.getRequired()));
			assertEquals(resolutiony.getStyle(), "form", String.format(msg, "style", "form", resolutiony.getStyle()));
			assertFalse(isExplode(resolutiony), String.format(msg, "explode", "false", resolutiony.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Requirement A.30: /req/edr/resolution-z-definition Parameter resolution-z definition
	 * </pre>
	 * 
	 * NOTE: Not referenced by ATS
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void resolutionzParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "resolution-z";
		Parameter resolutionz = findParameter(model.getPaths(), paramName, testPoint);

		if (resolutionz != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";

			assertEquals(resolutionz.getName(), paramName, String.format(msg, "name", paramName, resolutionz.getName()));
			assertEquals(resolutionz.getIn(), "query", String.format(msg, "in", "query", resolutionz.getIn()));
			assertFalse(isRequired(resolutionz), String.format(msg, "required", "false", resolutionz.getRequired()));
			assertEquals(resolutionz.getStyle(), "form", String.format(msg, "style", "form", resolutionz.getStyle()));
			assertFalse(isExplode(resolutionz), String.format(msg, "explode", "false", resolutionz.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Abstract Test 120: Validate that the corridor-height query parameter is constructed correctly.
	 * </pre>
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void corridorHeightParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "corridor-height";
		Parameter corridorHeight = findParameter(model.getPaths(), paramName, testPoint);

		if (corridorHeight != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";
			assertEquals(corridorHeight.getName(), paramName,
					String.format(msg, "name", paramName, corridorHeight.getName()));
			assertEquals(corridorHeight.getIn(), "query", String.format(msg, "in", "query", corridorHeight.getIn()));
			assertTrue(isRequired(corridorHeight), String.format(msg, "required", "true", corridorHeight.getRequired()));
			assertEquals(corridorHeight.getStyle(), "form", String.format(msg, "style", "form", corridorHeight.getStyle()));
			assertFalse(isExplode(corridorHeight), String.format(msg, "explode", "false", corridorHeight.getExplode()));
		}
	}

	/**
	 * <pre>
	 * Abstract Test 118: Validate that the corridor-width query parameter is constructed correctly. (corridor)
	 * </pre>
	 * 
	 * NOTE: Not referenced by ATS
	 *
	 * @param testPoint the testPoint under test, never <code>null</code>
	 * @param model     api definition, never <code>null</code>
	 */
	public void corridorWidthParameterDefinition(TestPoint testPoint, OpenAPI model) {
		String paramName = "corridor-width";
		Parameter corridorWidth = findParameter(model.getPaths(), paramName, testPoint);

		if (corridorWidth != null) {
			String msg = "Expected property '%s' with value '%s' but was '%s'";

			assertEquals(corridorWidth.getName(), paramName, String.format(msg, "name", paramName, corridorWidth.getName()));
			assertEquals(corridorWidth.getIn(), "query", String.format(msg, "in", "query", corridorWidth.getIn()));
			assertTrue(isRequired(corridorWidth), String.format(msg, "required", "true", corridorWidth.getRequired()));
			assertEquals(corridorWidth.getStyle(), "form", String.format(msg, "style", "form", corridorWidth.getStyle()));
			assertFalse(isExplode(corridorWidth), String.format(msg, "explode", "false", corridorWidth.getExplode()));
		}
	}
}
