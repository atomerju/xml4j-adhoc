package com.github.atomerju.xml;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author atomer
 */
public class XmlProcessFactory {

    public final static String[] SUPPORTED_IMPLEMENTS = {"woodstox", "jvm"};
    public final static Set<String> AVAILABLE_IMPLEMENTS = Set.of(SUPPORTED_IMPLEMENTS);
    public final static String DEFAULT_IMPLEMENT = SUPPORTED_IMPLEMENTS[0];

    private final static Map<String, Supplier<XMLInputFactory>> inputFactoryMap = Map.of(
        "jvm", XmlProcessFactory::createXMLInputFactory,
        "woodstox", XmlProcessFactory::createWstxInputFactory
    );

    private final static Map<String, Supplier<XMLOutputFactory>> outputFactoryMap = Map.of(
        "jvm", XMLOutputFactory::newFactory,
        "woodstox", WstxOutputFactory::new
    );

    private final XMLInputFactory inputFactory;
    private final XMLOutputFactory outputFactory;

    public XmlProcessFactory() {
        inputFactory = inputFactoryMap.get(DEFAULT_IMPLEMENT).get();
        outputFactory = outputFactoryMap.get(DEFAULT_IMPLEMENT).get();
    };

    public XmlProcessFactory(String target) {
        String needle = inputFactoryMap.containsKey(target) ? target : DEFAULT_IMPLEMENT;
        inputFactory = inputFactoryMap.get(needle).get();
        outputFactory = outputFactoryMap.get(needle).get();
    }

    private static XMLInputFactory createXMLInputFactory() {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        configXMLInputFactory(inputFactory);
        return inputFactory;
    }

    private static XMLInputFactory createWstxInputFactory() {
        // woodstox lib
        WstxInputFactory wstxInputFactory = new WstxInputFactory();
        //wstxInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
        //wstxInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
        //wstxInputFactory.setProperty(XMLInputFactory.IS_COALESCING,Boolean.FALSE);
        configXMLInputFactory(wstxInputFactory);
        wstxInputFactory.configureForSpeed();
        return wstxInputFactory;
    }

    private static void configXMLInputFactory(XMLInputFactory inputFactory) {
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING,Boolean.FALSE);
    }

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        return inputFactory.createXMLStreamReader(reader);
    }

    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
        return inputFactory.createXMLStreamReader(stream);
    }

    public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return outputFactory.createXMLStreamWriter(writer);
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
        return outputFactory.createXMLStreamWriter(stream);
    }
}
