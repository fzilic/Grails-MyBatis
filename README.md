Grails-MyBatis
==============

[![Build Status](https://travis-ci.org/fzilic/Grails-MyBatis.svg?branch=master)](https://travis-ci.org/fzilic/Grails-MyBatis)

MyBatis plugin for Grails framework

Based on original Grails-iBatis plugin by: Brian Sanders (http://grails.org/plugin/ibatis)

Grails plugin home: http://grails.org/plugin/mybatis

Modifications:
 - Some changes in name conventions
 - Added full support for multiple datasources
 - Plugin refactor for Grails 2.0
 - Minimum Grails dependency 2.0.3
 - Added support for optional optimistic locking - based on MyBatis Optimist plugin (http://code.google.com/p/optimist/) while avoiding Java annotations (convention over configuration)
 - Added support for custom Enum persistance (based on enum property value - to make DBA-s happy)

Documenation: http://fzilic.github.com/Grails-MyBatis/
- documentation review needed
- Also check the notes of arief-hidayat http://www.ariefhidayat.com/grails-mybatis/
