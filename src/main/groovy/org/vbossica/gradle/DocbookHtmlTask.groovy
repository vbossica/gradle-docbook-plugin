package org.vbossica.gradle

import javax.xml.transform.Transformer

class DocbookHtmlTask extends AbstractDocbookTask {

  @Override
  protected void init(DocbookPluginConvention convention) {

  }

  @Override
  protected String getExtension() {
    return 'html'
  }

  @Override
  protected String getXDir() {
    return 'html'
  }

  @Override
  protected void preTransform(Transformer transformer, File sourceFile, File outputFile) {
    String rootFilename = outputFile.getName()
    rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'))
    transformer.setParameter('root.filename', rootFilename)
    transformer.setParameter('base.dir', outputFile.getParent() + File.separator)
  }

}
