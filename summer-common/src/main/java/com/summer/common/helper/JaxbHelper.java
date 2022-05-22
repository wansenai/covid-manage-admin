package com.summer.common.helper;

import com.google.common.collect.Maps;
import javafx.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * XML处理类
 **/
public final class JaxbHelper {
    private static final Map<Class<?>, Pair<XmlRootElement, JAXBContext>> jaxbContextMap = Maps.newHashMap();

    private JaxbHelper() {
    }

    /**
     * JAVA类生成 xml
     **/
    public static <T> String marshal(final T t, boolean pretty) {
        CDataXMLSerializer.CDataXMLStreamWriter writer = null;
        try {
            Class<?> clazzT = t.getClass();
            Pair<XmlRootElement, JAXBContext> pair = jaxbPair(clazzT);
            Marshaller jaxbMarshaller = pair.getValue().createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, pretty);
            writer = CDataXMLSerializer.createCDataXMLStreamWriter();
            jaxbMarshaller.marshal(t, writer);
            return writer.toXml();
        } catch (Exception e) {
            throw new RuntimeException("jaxb marshal error...", e);
        } finally {
            CDataXMLSerializer.close(writer);
        }
    }

    /**
     * xml string 转 class
     **/
    public static <T> T unmarshal(String xml, final Class<T> clazz) {
        try {
            return (T) (getUnmarshaller(clazz, null).unmarshal(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException("jaxb unmarshal error...", e);
        }
    }

    /**
     * xml string 转 class
     **/
    public static <T> T unmarshal(String xml, final Class<T> clazz, final ValidationEventHandler handler) {
        try {
            return (T) (getUnmarshaller(clazz, handler).unmarshal(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException("jaxb unmarshal error...", e);
        }
    }

    /**
     * xml stream 转 class
     **/
    public static <T> T unmarshal(final InputStream is, final Class<T> clazz) {
        try {
            return (T) (getUnmarshaller(clazz, null).unmarshal(is));
        } catch (Exception e) {
            throw new RuntimeException("jaxb unmarshal error...", e);
        }
    }

    /**
     * xml stream 转 class
     **/
    public static <T> T unmarshal(final InputStream is, final Class<T> clazz, final ValidationEventHandler handler) {
        try {
            return (T) (getUnmarshaller(clazz, handler).unmarshal(is));
        } catch (Exception e) {
            throw new RuntimeException("jaxb unmarshal error...", e);
        }
    }

    private static <T> Unmarshaller getUnmarshaller(final Class<T> clazz, final ValidationEventHandler handler) throws Exception {
        Unmarshaller jaxbUnmarshaller = jaxbPair(clazz).getValue().createUnmarshaller();
        if (handler != null) {
            jaxbUnmarshaller.setEventHandler(handler);
        }
        return jaxbUnmarshaller;
    }

    private static <T> Pair<XmlRootElement, JAXBContext> jaxbPair(Class<T> clazz) throws JAXBException {
        Pair<XmlRootElement, JAXBContext> pair = jaxbContextMap.get(clazz);
        if (null == pair) synchronized (jaxbContextMap) {
            XmlRootElement root = clazz.getAnnotation(XmlRootElement.class);
            JAXBContext context = JAXBContext.newInstance(clazz);
            pair = new Pair<>(root, context);
            jaxbContextMap.put(clazz, pair);
        }
        return pair;
    }

    static class CDataXMLSerializer {
        static CDataXMLStreamWriter createCDataXMLStreamWriter() throws XMLStreamException {
            return new CDataXMLStreamWriter(new StringWriter());
        }

        static void close(CDataXMLStreamWriter writer) {
            try {
                if (null != writer) {
                    writer.close();
                }
            } catch (Exception e) {
                throw new RuntimeException("close jaxb xml writer error: ", e);
            }
        }

        public static class CDataXMLStreamWriter extends DelegatingXMLStreamWriter {
            private static final String EMPTY = "";
            private final StringWriter stringWriter;

            private CDataXMLStreamWriter(StringWriter writer) throws XMLStreamException {
                super(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
                this.stringWriter = writer;
            }

            @Override
            public void writeCharacters(String text) throws XMLStreamException {
                if (null == text || EMPTY.equals(text.trim())) {
                    super.writeCharacters(text);
                } else {
                    super.writeCData(text);
                }
            }

            String toXml() throws XMLStreamException {
                this.flush();
                return this.stringWriter.getBuffer().toString();
            }
        }

        abstract static class DelegatingXMLStreamWriter implements XMLStreamWriter {
            private final XMLStreamWriter writer;

            public DelegatingXMLStreamWriter(XMLStreamWriter writer) {
                this.writer = writer;
            }

            public void writeStartElement(String localName) throws XMLStreamException {
                writer.writeStartElement(localName);
            }

            public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
                writer.writeStartElement(namespaceURI, localName);
            }

            public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
                writer.writeStartElement(prefix, localName, namespaceURI);
            }

            public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
                writer.writeEmptyElement(namespaceURI, localName);
            }

            public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
                writer.writeEmptyElement(prefix, localName, namespaceURI);
            }

            public void writeEmptyElement(String localName) throws XMLStreamException {
                writer.writeEmptyElement(localName);
            }

            public void writeEndElement() throws XMLStreamException {
                writer.writeEndElement();
            }

            public void writeEndDocument() throws XMLStreamException {
                writer.writeEndDocument();
            }

            public void close() throws XMLStreamException {
                writer.close();
            }

            public void flush() throws XMLStreamException {
                writer.flush();
            }

            public void writeAttribute(String localName, String value) throws XMLStreamException {
                writer.writeAttribute(localName, value);
            }

            public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
                writer.writeAttribute(prefix, namespaceURI, localName, value);
            }

            public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
                writer.writeAttribute(namespaceURI, localName, value);
            }

            public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
                writer.writeNamespace(prefix, namespaceURI);
            }

            public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
                writer.writeDefaultNamespace(namespaceURI);
            }

            public void writeComment(String data) throws XMLStreamException {
                writer.writeComment(data);
            }

            public void writeProcessingInstruction(String target) throws XMLStreamException {
                writer.writeProcessingInstruction(target);
            }

            public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
                writer.writeProcessingInstruction(target, data);
            }

            public void writeCData(String data) throws XMLStreamException {
                writer.writeCData(data);
            }

            public void writeDTD(String dtd) throws XMLStreamException {
                writer.writeDTD(dtd);
            }

            public void writeEntityRef(String name) throws XMLStreamException {
                writer.writeEntityRef(name);
            }

            public void writeStartDocument() throws XMLStreamException {
                writer.writeStartDocument();
            }

            public void writeStartDocument(String version) throws XMLStreamException {
                writer.writeStartDocument(version);
            }

            public void writeStartDocument(String encoding, String version) throws XMLStreamException {
                writer.writeStartDocument(encoding, version);
            }

            public void writeCharacters(String text) throws XMLStreamException {
                writer.writeCharacters(text);
            }

            public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
                writer.writeCharacters(text, start, len);
            }

            public String getPrefix(String uri) throws XMLStreamException {
                return writer.getPrefix(uri);
            }

            public void setPrefix(String prefix, String uri) throws XMLStreamException {
                writer.setPrefix(prefix, uri);
            }

            public void setDefaultNamespace(String uri) throws XMLStreamException {
                writer.setDefaultNamespace(uri);
            }

            public NamespaceContext getNamespaceContext() {
                return writer.getNamespaceContext();
            }

            public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
                writer.setNamespaceContext(context);
            }

            public Object getProperty(String name) throws IllegalArgumentException {
                return writer.getProperty(name);
            }
        }
    }
}
