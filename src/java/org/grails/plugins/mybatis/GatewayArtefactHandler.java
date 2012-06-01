package org.grails.plugins.mybatis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
import org.codehaus.groovy.grails.commons.GrailsClass;

public class GatewayArtefactHandler extends ArtefactHandlerAdapter {
  public static final String TYPE = "Gateway";
  private static Log log = LogFactory.getLog(GatewayArtefactHandler.class);

  public GatewayArtefactHandler() {
    super(TYPE, GrailsClass.class, DefaultGrailsGatewayClass.class, null);
    log.debug("Created instance");
  }

  @Override
  @SuppressWarnings("rawtypes")
  public boolean isArtefactClass(Class clazz) {
    log.debug("Checking potential artefact class " + clazz);
    return clazz != null && clazz.getName().endsWith(TYPE);
  }

  @Override
  public boolean isArtefactGrailsClass(GrailsClass grailsClass) {
    log.debug("Checking potential artefact class " + grailsClass + " within isArtefactGrailsClass");
    return super.isArtefactGrailsClass(grailsClass);
  }
}
