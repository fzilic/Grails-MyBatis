package org.grails.plugins.mybatis.locking

class OptimisticLockingException extends RuntimeException {

  public OptimisticLockingException(String message) {
    super(message)
  }
}
