package org.grails.plugins.mybatis.locking

class RecordDeletedException extends OptimisticLockingException {

  public RecordDeletedException(String message) {
    super(message)
  }
}

