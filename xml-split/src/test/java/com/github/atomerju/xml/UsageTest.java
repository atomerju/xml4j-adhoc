package com.github.atomerju.xml;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Random;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

/**
 * @author atomer
 */
public class UsageTest {

    static WstxInputFactory wstxInputFactory;
    static WstxOutputFactory wstxOutputFactory;

    @BeforeEach
    void setUp() {
        wstxInputFactory = new WstxInputFactory();

        wstxInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
        wstxInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
        wstxInputFactory.setProperty(XMLInputFactory.IS_COALESCING,Boolean.FALSE);
        wstxInputFactory.configureForSpeed();

        wstxOutputFactory = new WstxOutputFactory();
    }

    static boolean isSameStartTag(String targetTag, XMLEvent event) {
        return targetTag.equals(event.asStartElement().getName().getLocalPart());
    }

    static boolean isSameEndTag(String targetTag, XMLEvent event) {
        return targetTag.equals(event.asEndElement().getName().getLocalPart());
    }

    @Test
    void testXMLEvent() throws Exception {
        //https://github.com/TomHamm/XML-File-Splitter/blob/master/src/main/java/com/sidero/split_large_xml/XML_Split.java

        // prepare input
        String testFile = "books.xml";
        String splitTag = "book";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(testFile);
        XMLEventReader xer = wstxInputFactory.createXMLEventReader(is);

        // prepare output
        String outFile = "output.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        //XMLEventWriter xew = wstxOutputFactory.createXMLEventWriter(writer);

        while (xer.hasNext()) {
            XMLEvent event = xer.nextEvent();
            if (event.isStartElement() && isSameStartTag(splitTag, event)) {
                StringWriter stringWriter = new StringWriter();
                XMLEventWriter xew = wstxOutputFactory.createXMLEventWriter(stringWriter);

                while (!(event.isEndElement() && isSameEndTag(splitTag, event))) {
                    xew.add(event);
                    event = xer.nextEvent();
                    if (event.isEndElement() && isSameEndTag(splitTag, event)) {
                        xew.add(event);
                    }

                }
                xew.close();
                stringWriter.write("\n");
                String output = stringWriter.toString().replace("\n", "");
                stringWriter.close();
                System.out.println("out: |" + output + "|");
                writer.write(stringWriter.toString());
            }
        }
        xer.close();
        writer.close();
    }

    @Test
    void testXmlStream() throws Exception {
        // prepare input
        String testFile = "books.xml";
        String splitTag = "book";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(testFile);
        XMLStreamReader xsr = wstxInputFactory.createXMLStreamReader(is);

        // prepare output
        String outFile = "output.stream.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        while (xsr.hasNext()) {
            int event = xsr.next();
            if (event == START_ELEMENT && xsr.getLocalName().equals(splitTag)) {
                StringWriter stringWriter = new StringWriter();
                XMLStreamWriter xsw = wstxOutputFactory.createXMLStreamWriter(stringWriter);
                while (!(event == END_ELEMENT && xsr.getLocalName().equals(splitTag))) {
                    switch (event) {
                        case START_ELEMENT -> {
                            //System.out.println("local: |" + xsr.getLocalName() + "|");
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
                xsw.close();
                String output = stringWriter.toString().replace("\n", "");
                System.out.println("output: " + output);
                writer.write(output);
                writer.write("\n");
            }
        }
        xsr.close();
        writer.close();
    }

    @Disabled("This is used to generate sample manually")
    @Test
    void testCreateSample() throws Exception {
        String outFile = "input.s4m.xml";
        int maxNum = 4_000_000; // 2m -> 570MB, 4m -> 1.2GB
        Random rand = new Random();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("<books>\n"); // header
            for (int i = 0; i < maxNum; i++) {
                String book =
                """
                <book attr1="val1" attr2="val2">
                    <name>book %d</name>
                    <published>%d</published>
                    <title>title %d</title>
                    <extra>
                        <pages>100</pages>
                        <summary>this is the short summary</summary>
                        <desc><![CDATA[this is the cdata section]]></desc>
                    </extra>
                </book>
                """.formatted(
                    i + 1,
                    rand.nextInt((2000 - 500) + 1) + 500, // year
                    i + 1,
                    rand.nextInt(500 - 100 + 1) + 100 // pages
                    ).trim();
                writer.write(book);
                writer.write('\n');
            }
            writer.write("</books>\n"); // footer
        }

    }
}
