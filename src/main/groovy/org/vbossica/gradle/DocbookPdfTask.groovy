/*
 * Copyright 2011 Vladimir Ritz Bossicard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vbossica.gradle

import org.apache.fop.apps.FOUserAgent
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.MimeConstants
import org.apache.fop.cli.InputHandler
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

/**
 * <a href="http://xmlgraphics.apache.org/fop/0.95/embedding.html#render">From the FOP usage guide</a>
 */
class DocbookPdfTask extends AbstractDocbookTask {

  File userConfig = null

  @Override
  protected void init(DocbookPluginConvention convention) {
    if (userConfig == null) {
      userConfig = convention.userConfig
    }
  }

  @Override
  protected String getExtension() {
    return 'fo'
  }

  @Override
  protected String getXDir() {
    return 'pdf'
  }

  @Override
  protected void postTransform(File foFile) {
    FopFactory fopFactory = FopFactory.newInstance();

    OutputStream out = null;
    final File pdfFile = getPdfOutputFile(foFile);
    logger.info("Transforming 'fo' file " + foFile + " to PDF: " + pdfFile);

    try {
      switch (project.gradle.startParameter.logLevel) {
        case LogLevel.DEBUG:
        case LogLevel.INFO:
          break;
        default:
          // only show verbose fop output if the user has specified 'gradle -d' or 'gradle -i'
          // LoggerFactory.getILoggerFactory().getLogger('org.apache.fop').level = Level.ERROR
          break;
      }

      out = new BufferedOutputStream(new FileOutputStream(pdfFile));
      InputHandler inputHandler = new InputHandler(foFile);

      if (userConfig != null) {
        fopFactory.setUserConfig(userConfig);
      }
      FOUserAgent userAgent = fopFactory.newFOUserAgent();
      if (sourceDirectory != null) {
        userAgent.setBaseURL(sourceDirectory.toURI().toURL().toExternalForm());
      }

      inputHandler.renderTo(userAgent, MimeConstants.MIME_PDF, out);
    } finally {
      if (out != null) {
        out.close();
      }
    }

    if (!foFile.delete()) {
      logger.warn("Failed to delete 'fo' file " + foFile);
    }
  }

  private File getPdfOutputFile(File foFile) {
    String name = foFile.getAbsolutePath();
    return new File(name.substring(0, name.length() - 2) + "pdf");
  }

}
