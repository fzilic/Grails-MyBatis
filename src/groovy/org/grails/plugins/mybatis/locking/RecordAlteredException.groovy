package org.grails.plugins.mybatis.locking

class RecordAlteredException extends OptimisticLockingException {

  public RecordAlteredException (String message) {
    super(message)
  }
}
