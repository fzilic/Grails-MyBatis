package org.grails.plugins.mybatis.locking

class OptimisticLockingException extends RuntimeException {

  public OptimisticLockingException(String cause) {
    super(cause)
  }
}
