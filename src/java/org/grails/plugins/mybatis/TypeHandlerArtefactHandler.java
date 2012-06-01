package org.grails.plugins.mybatis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
import org.codehaus.groovy.grails.commons.GrailsClass;

public class TypeHandlerArtefactHandler extends ArtefactHandlerAdapter {
  public static final String TYPE = "TypeHandler";
  private static Log log = LogFactory.getLog(TypeHandlerArtefactHandler.class);

  public TypeHandlerArtefactHandler() {
    super(TYPE, GrailsClass.class, DefaultGrailsTypeHandlerClass.class, null);
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
