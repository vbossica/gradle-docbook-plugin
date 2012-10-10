gradle-docbook-plugin
=====================

Plugin for generation of PDF documents based on Docbook.

Local Installation
------------------

To install the plugin on your local Maven repository:

  gradle clean install

Usage
-----

  # build.gradle
  
  buildscript {
    repositories {
      mavenCentral()
      mavenLocal()
    }
  
    dependencies {
      classpath "org.vbossica:gradle-docbook-plugin:1.0-SNAPSHOT"
    }
  }
  
  docbook {
    userConfig = file([location of fop.xconf file")
    classpath = buildscript.configurations.classpath
  }
  
  task generateArticle(type: org.vbossica.gradle.DocbookPdfTask) {
    stylesheet = file([location of xsl stylesheet file])
    sourceFileName = '[filename]'
  }

The top level docbook file must be located in the folder `src/docs`
