package org.vbossica.gradle

import com.icl.saxon.TransformerFactoryImpl
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

/**
 * Gradle Docbook plugin implementation.
 *
 * @author ltaylor
 */
public abstract class AbstractDocbookTask extends DefaultTask {

  @Input
  boolean XIncludeAware = true;

  @Input
  boolean highlightingEnabled = false;

  String admonGraphicsPath;

  File sourceDirectory = new File(project.getProjectDir(), "src/docs");

  @Input
  String sourceFileName;

  @InputFile
  File stylesheet;

  @OutputDirectory
  File docsDir = new File(project.getBuildDir(), "docs");

  Configuration classpath

  @TaskAction
  public final void transform() {
    // initialization
    DocbookPluginConvention convention = project.convention.plugins.docbook

    if (convention == null) {
      throw new IllegalStateException("no 'docbook' convention was defined")
    }

    if (stylesheet == null) {
      stylesheet = convention.stylesheet
    }
    if (convention.sourceDirectory != null) {
      sourceDirectory = convention.sourceDirectory
    }
    if (convention.classpath != null) {
      classpath = convention.classpath
    }
    init(convention)

    // the docbook tasks issue spurious content to the console. redirect to INFO level
    // so it doesn't show up in the default log level of LIFECYCLE unless the user has
    // run gradle with '-d' or '-i' switches -- in that case show them everything
    switch (project.gradle.startParameter.logLevel) {
      case LogLevel.DEBUG:
      case LogLevel.INFO:
        break;
      default:
        logging.captureStandardOutput(LogLevel.INFO)
        logging.captureStandardError(LogLevel.INFO)
    }

    SAXParserFactory factory = new org.apache.xerces.jaxp.SAXParserFactoryImpl();
    factory.setXIncludeAware(XIncludeAware);
    docsDir.mkdirs();

    File srcFile = new File(sourceDirectory, sourceFileName);
    String outputFilename = srcFile.getName().substring(0, srcFile.getName().lastIndexOf('.') + 1) + getExtension();

    File oDir = new File(getDocsDir(), getXDir())
    File outputFile = new File(oDir, outputFilename);

    Result result = new StreamResult(outputFile.getAbsolutePath());
    InputSource inputSource = new InputSource(srcFile.getAbsolutePath());

    XMLReader reader = factory.newSAXParser().getXMLReader();
    TransformerFactory transformerFactory = new TransformerFactoryImpl();
    URL url = stylesheet.toURL();
    Source source = new StreamSource(url.openStream(), url.toExternalForm());
    Transformer transformer = transformerFactory.newTransformer(source);

    if (highlightingEnabled) {
      File highlightingDir = new File(getProject().getBuildDir(), "highlighting");
      transformer.setParameter("highlight.xslthl.config", new File(highlightingDir, "xslthl-config.xml").toURI().toURL());
    }

    if (admonGraphicsPath != null) {
      transformer.setParameter("admon.graphics", "1");
      transformer.setParameter("admon.graphics.path", admonGraphicsPath);
    }

    preTransform(transformer, srcFile, outputFile);
    transformer.transform(new SAXSource(reader, inputSource), result);
    postTransform(outputFile);
  }

  protected abstract void init(DocbookPluginConvention convention)

  protected abstract String getExtension()

  protected abstract String getXDir()

  protected void preTransform(Transformer transformer, File sourceFile, File outputFile) {
  }

  protected void postTransform(File outputFile) {
  }

}
