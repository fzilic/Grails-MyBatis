dataSource {
  pooled = true
  driverClassName = "org.h2.Driver"
  username = "sa"
  password = ""
}

mybatis.dataSourceNames = ['dataSource', 'dataSource_alternate']

// environment specific settings
environments {
  development {
    dataSource {
      dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
      url = "jdbc:h2:mem:devDb;MVCC=TRUE"
    }

    dataSource_alternate {
      dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
      url = "jdbc:h2:mem:devAlternateDb;MVCC=TRUE"
    }

  }
  test {
    dataSource {
      dbCreate = "create-drop"
      url = "jdbc:h2:mem:testDb;MVCC=TRUE"
    }

    dataSource_alternate {
      dbCreate = "create-drop"
      url = "jdbc:h2:mem:testAlternateDb;MVCC=TRUE"
    }
  }
}
