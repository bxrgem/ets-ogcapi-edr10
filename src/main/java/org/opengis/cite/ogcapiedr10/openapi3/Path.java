package org.opengis.cite.ogcapiedr10.openapi3;

import java.util.List;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;

public class Path {
  private final String path;
  private final PathItem pathItem;

  Path(String path, PathItem pathItem) {
    this.path = path;
    this.pathItem = pathItem;
  }

  public String path() {
    return path;
  }

  public PathItem pathItem() {
    return pathItem;
  }

  public String getPathString() {
    return path;
  }

  public List<Operation> getOperations() {
    return pathItem.readOperations();
  }
}
