package com.github.atomerju.xml;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author atomer
 */
public class XmlSplitter {

    final XmlProcessFactory processFactory;
    final File inputFile;
    final File outputFile;
    final String splitTag;
    final long limit;

    public XmlSplitter() {
        // config
        processFactory = new XmlProcessFactory("woodstox");
        inputFile = new File("book2.xml");
        outputFile = new File("output.txt");
        //splitTag = "book";
        splitTag = "extra";
        limit = 0;
    }

    public XmlSplitter(String inputFileName, String outputFileName, String splitTag, String implName, long limit) {
        this.processFactory = new XmlProcessFactory(implName);
        this.inputFile = new File(inputFileName);
        this.outputFile = new File(outputFileName);
        this.splitTag = splitTag;
        this.limit = limit;
    }

    public void split() {
        try (
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            XMLStreamReader xsr = processFactory.createXMLStreamReader(inputStream);

            long count = 0;
            boolean isDone = false;
            while (xsr.hasNext() && !isDone) {
                int event = xsr.next();
                if (event == START_ELEMENT && xsr.getLocalName().equals(splitTag)) {
                    StringWriter stringWriter = new StringWriter();
                    XMLStreamWriter xsw = processFactory.createXMLStreamWriter(stringWriter);
                    while (!(event == END_ELEMENT && xsr.getLocalName().equals(splitTag))) {
                        switch (event) {
                            case START_ELEMENT -> {
                                xsw.writeStartElement(xsr.getLocalName());
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    xsw.writeAttribute(xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                                }
                            }
                            case CDATA -> xsw.writeCData(xsr.getText());
                            case CHARACTERS -> xsw.writeCharacters(xsr.getText().trim());
                            case END_ELEMENT -> xsw.writeEndElement();
                        }
                        event = xsr.next();
                        if (event == END_ELEMENT && xsr.getLocalName().equals(splitTag)) {
                            xsw.writeEndElement();
                        }
                    }
                    xsw.close(); // flush into the string writer
                    String output = stringWriter.toString().replace("\n", "");
                    writer.write(output);
                    writer.write("\n");

                    count += 1;
                    if (limit > 0 && count >= limit) {
                        isDone = true;
                    }
                }
            }

            xsr.close();
        } catch (IOException e) {
            System.out.println("Failed to read file");
        } catch (XMLStreamException e) {
            System.out.println("Failed to process XML");
        }
    }

    public static void printHelp(Options options) {
        try {
            String usage = "java -cp xml-split-jar-with-dependencies.jar ";
            String header = "Split XML by the given tag";
            String footer = "";

            HelpFormatter formatter = HelpFormatter.builder().setShowSince(false).get();
            formatter.printHelp(usage, header, options, footer, true);
        } catch (IOException e) {
            System.out.println("Failed to print help msg: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // handle CLI options
        Options cliOpts = new Options()
            .addOption("h", "help", false, "Print help message")
            .addOption(Option.builder().option("i").longOpt("input")
                .argName("file")
                .hasArg()
                //.required()
                .desc("The input file name")
                .get())
            .addOption(Option.builder().option("o").longOpt("output")
                .argName("file")
                .hasArg()
                .desc("The output file name")
                .get())
            .addOption(Option.builder().option("t").longOpt("tag")
                .argName("tag")
                .hasArg()
                //.required()
                .desc("The target XML tag to split the input file")
                .get())
            .addOption(Option.builder().option("I").longOpt("impl")
                .hasArg()
                .desc("The backend implementation: jvm or woodstox (default: woodstox)")
                .get())
            .addOption(Option.builder().option("l").longOpt("limit")
                .argName("num")
                .hasArg()
                .type(Long.class)
                .desc("The max number of split output")
                .get());

        //String[] args2 = "-i input.s4m.xml -o output.s4m.txt -t extra -I jvm".split(" ");
        //args = args2;

        CommandLineParser parser = new DefaultParser();
        String inputFile = null;
        String outputFile = null;
        String factoryImpl = null;
        String tagName = null;
        Long limit = null;
        try {
            CommandLine line = parser.parse(cliOpts, args);
            if (line.hasOption("help")) {
                XmlSplitter.printHelp(cliOpts);
                System.exit(0);
            }

            inputFile = line.hasOption("input") ?
                line.getOptionValue("input") : "book2.xml";
            //System.out.println("in: " + inputFile);
            outputFile = line.hasOption("output") ?
                line.getOptionValue("output") : "output100.txt";
            tagName = line.hasOption("tag") ?
                line.getOptionValue("tag") : "book";
            factoryImpl = line.hasOption("impl") ?
                line.getOptionValue("impl").toLowerCase() : "";
            factoryImpl = XmlProcessFactory.AVAILABLE_IMPLEMENTS.contains(factoryImpl) ?
                factoryImpl : XmlProcessFactory.DEFAULT_IMPLEMENT;
            limit = line.hasOption("limit") ?
                line.getParsedOptionValue("limit") : 0L;
            limit = (limit != null && limit > 0L) ? limit : 0L;
        } catch (ParseException e) {
            System.out.println("CLI option failures: " + e.getMessage());
            XmlSplitter.printHelp(cliOpts);
            System.exit(1);
        }
        //XmlSplitter.printHelp(cliOpts);
        //System.exit(0);


        //XmlSplitter splitter = new XmlSplitter();
        /**/
        XmlSplitter splitter = new XmlSplitter(
            inputFile,
            outputFile,
            tagName,
            factoryImpl,
            limit
        );

         /**/
        splitter.split();
    }
}
